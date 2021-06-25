package cn.rongcloud.mic.room.service;

import static java.util.stream.Collectors.toList;

import cn.rongcloud.common.im.ChrmEntrySetInfo;
import cn.rongcloud.common.im.IMHelper;
import cn.rongcloud.common.im.pojos.IMApiResultInfo;
import cn.rongcloud.common.im.pojos.IMChatRoomUserResult;
import cn.rongcloud.common.im.pojos.IMChrmWhiteListResult;
import cn.rongcloud.common.im.pojos.IMIsInChrmResult;
import cn.rongcloud.common.im.pojos.IMRoomUserInfo;
import cn.rongcloud.common.utils.IMSignUtil;
import cn.rongcloud.mic.common.redis.RedisLockService;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.common.rest.RestResultCode;
import cn.rongcloud.common.utils.DateTimeUtils;
import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.common.utils.IdentifierUtils;
import cn.rongcloud.mic.constant.CustomerConstant;
import cn.rongcloud.mic.jwt.JwtUser;
import cn.rongcloud.mic.room.dao.RoomDao;
import cn.rongcloud.mic.room.dao.RoomMusicDao;
import cn.rongcloud.mic.room.enums.*;
import cn.rongcloud.mic.room.model.TRoomMusic;
import cn.rongcloud.mic.room.pojos.*;
import cn.rongcloud.mic.user.enums.UserType;
import cn.rongcloud.mic.room.model.TRoom;
import cn.rongcloud.mic.user.model.TUser;
import cn.rongcloud.mic.user.service.UserService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    RoomDao roomDao;

    @Autowired
    RoomMusicDao roomMusicDao;

    @Autowired
    UserService userService;

    @Value("${rongrtc.filepath}")
    private String filepath;

    @Autowired
    IMHelper imHelper;

    @Autowired
    IMSignUtil imSignUtil;

    @Autowired
    FileService fileService;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "roomRedisTemplate")
    private HashOperations<String, String, TRoom> roomHashOperations;

    @Resource(name = "roomRedisTemplate")
    private HashOperations<String, String, Object> redisHashOperations;

    @Resource(name = "roomRedisTemplate")
    private HashOperations<String, String, HashMap<String, Long>> roomHashGifts;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOperations;

    @Value("${rongrtc.domain}")
    private String domain;

    @Value("${rongrtc.mic.transfer_host_expire}")
    private Long transferHostExpire;

    @Value("${rongrtc.mic.takeover_host_expire}")
    private Long takeoverHostExpire;

    @Value("${rongrtc.room.expire}")
    private Integer roomExpire;

    @Override
    public RestResult createRoom(ReqRoomCreate data, JwtUser jwtUser) throws Exception {
        //只有注册用户可操作
        if (!jwtUser.getType().equals(UserType.USER.getValue())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //判断用户是否存在
        TUser tUser = userService.getUserInfo(jwtUser.getUserId());
        if (tUser == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_EXIST);
        }

        TRoom roomInfo = roomDao.findTRoomByUserIdEquals(jwtUser.getUserId());
        if (roomInfo != null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_IS_EXIST, build(roomInfo));
        }

        //创建 IM 聊天室
        String chatRoomId = IdentifierUtils.uuid();
        IMApiResultInfo resultInfo = imHelper.createChatRoom(chatRoomId, data.getName());
        if (!resultInfo.isSuccess()) {
            log.error("create chatRoom error: {}, {}", jwtUser, resultInfo.getErrorMessage());
            return RestResult.generic(RestResultCode.ERR_ROOM_CREATE_ROOM_ERROR, resultInfo.getErrorMessage());
        }

        //将房间信息保存至数据库
        Date date = DateTimeUtils.currentUTC();
        TRoom room = new TRoom();
        room.setUid(chatRoomId);
        room.setName(data.getName());
        room.setBackgroundUrl(data.getBackgroundUrl());
        room.setThemePictureUrl(data.getThemePictureUrl());
        room.setType(RoomType.CUSTOM.getValue());
        room.setAllowedJoinRoom(true);
        room.setAllowedFreeJoinMic(true);
        room.setIsPrivate(data.getIsPrivate());
        room.setPassword(data.getPassword());
        room.setUserId(jwtUser.getUserId());
        room.setCreateDt(date);
        room.setUpdateDt(date);
        roomDao.save(room);

        //kv 保存IM server todo
        initMicRoomKv(chatRoomId, data.getKv(), jwtUser.getUserId());

        //将房间信息缓存至 Redis Hash
        updateRoomCache(room);

        //将房间 Id 缓存至 Redis Zset，房间分页查询数据源
        zSetOperations.add(getRedisRoomIdsKey(), chatRoomId, date.getTime());
        return RestResult.success(build(room));
    }

    @Override
    public RestResult getRoomDetail(String roomId) {
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        return RestResult.success(build(room));
    }

    @Override
    public RestResult getRoomList(Integer page, Integer size, HttpServletRequest request) {

        Long from = 0L;
        if (page < 1) {
            page = 1;
        }
        int start = (page - 1) * size;
        from = Long.valueOf(start);

        ResRoomList resRoomList = new ResRoomList();
        //从 redis 查询房间总数
        Long totalCount = zSetOperations.size(getRedisRoomIdsKey());
        resRoomList.setTotalCount(totalCount);

        String path = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        List<String> images = new ArrayList<>();
        images.add(path + "/static/room/1.gif");
        images.add(path + "/static/room/2.gif");
        images.add(path + "/static/room/3.jpg");
        images.add(path + "/static/room/4.jpg");
        resRoomList.setImages(images);

        List<ResRoomInfo> roomInfos = new ArrayList<>();
        //从 redis 分页查询
        Set<String> roomIds = zSetOperations.reverseRange(getRedisRoomIdsKey(), from, (from + size - 1));
        if (roomIds == null || roomIds.isEmpty()) {
            resRoomList.setRooms(roomInfos);
            return RestResult.success(resRoomList);
        }

        //批量从 Redis 查询房间数据
        Map<String, TRoom> roomMap = hmget(getRedisRoomInfosKey(), new ArrayList<>(roomIds));
        for (String roomId : roomIds) {
            if (roomMap.containsKey(roomId)) {
                roomInfos.add(build(roomMap.get(roomId)));
            }
        }
        resRoomList.setRooms(roomInfos);


        return RestResult.success(resRoomList);
    }

    @Override
    public List<TRoom> getRoomListAll() {
        //从 redis 分页查询
        Long size = zSetOperations.size(getRedisRoomIdsKey());
        Set<String> roomIds = zSetOperations.reverseRange(getRedisRoomIdsKey(), 0, size);
        if (roomIds == null || roomIds.isEmpty()) {
            return null;
        }

        //批量从 Redis 查询房间数据
        List<TRoom> list = new ArrayList<>();
        Map<String, TRoom> roomMap = hmget(getRedisRoomInfosKey(), new ArrayList<>(roomIds));
        for (String roomId : roomIds) {
            if (roomMap.containsKey(roomId)) {
                list.add(roomMap.get(roomId));
            }
        }
        return list;
    }

    @Override
    public RestResult roomSetting(ReqRoomSetting data, JwtUser jwtUser) {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        room.setApplyAllLockMic(data.getApplyAllLockMic());
        room.setApplyAllLockSeat(data.getApplyAllLockSeat());
        room.setApplyOnMic(data.getApplyOnMic());
        room.setSetMute(data.getSetMute());
        room.setSetSeatNumber(data.getSetSeatNumber());
        //更新redis
        updateRoomCache(room);

        //是否需要更新数据库
//        boolean needUpdate = false;

        //设置房间是否允许观众加入
//        if (data.getAllowedJoinRoom() != null && room.isAllowedJoinRoom() != data.getAllowedJoinRoom()) {
//            room.setAllowedJoinRoom(data.getAllowedJoinRoom());
//            needUpdate = true;
//        }
//        //设置是否允许观众自由上麦
//        if (data.getAllowedFreeJoinMic() != null && room.isAllowedFreeJoinMic() != data.getAllowedFreeJoinMic()) {
//            room.setAllowedFreeJoinMic(data.getAllowedFreeJoinMic());
//            needUpdate = true;
//        }

//        if (needUpdate) {
//            //更新数据库
//            roomDao.updateRoomSetting(room.getUid(), room.isAllowedJoinRoom(), room.isAllowedFreeJoinMic());
//            //更新 Redis
//            updateRoomCache(room);
//        }

        return RestResult.success();
    }


    @Override
    public RestResult getRoomSetting(String roomId, JwtUser jwtUser) {
        if (StringUtils.isBlank(roomId)) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        ReqRoomSetting response = new ReqRoomSetting();
        response.setRoomId(room.getUid());
        response.setSetMute(room.getSetMute());
        response.setApplyAllLockMic(room.getApplyAllLockMic());
        response.setSetSeatNumber(room.getSetSeatNumber());
        response.setApplyAllLockSeat(room.getApplyAllLockSeat());
        response.setApplyOnMic(room.getApplyOnMic());

        return RestResult.success(response);
    }

    @Override
    public RestResult musicMove(ReqRoomMusicMove data, JwtUser jwtUser) {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        TRoomMusic one = roomMusicDao.getOne(data.getToId());
        Integer updateSort = one.getSort() + 1;
        roomMusicDao.updateRoomMusicBySort(data.getRoomId(), one.getSort());
        roomMusicDao.updateRoomMusic(data.getFromId(), updateSort);
        return RestResult.success();
    }

    @Override
    public RestResult roomPrivate(ReqRoomPrivate data, JwtUser jwtUser) {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        room.setIsPrivate(data.getIsPrivate());
        room.setPassword(data.getPassword());
        //更新数据库
        roomDao.updateRoomPrivate(room.getUid(), room.getIsPrivate(), room.getPassword());
        //更新 Redis
        updateRoomCache(room);

        return RestResult.success();
    }

    @Override
    public RestResult roomName(ReqRoomName data, JwtUser jwtUser) {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        room.setName(data.getName());
        //更新数据库
        roomDao.updateRoomName(room.getUid(), room.getName());
        //更新 Redis
        updateRoomCache(room);

        return RestResult.success();
    }

    @Override
    public RestResult roomManage(ReqRoomMicManage data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查用户是否在房间
        if (!isInChrm(data.getRoomId(), data.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_IN);
        }
        if (!data.getIsManage()) {
            setOperations.remove(getRedisRoomManageUserKey(data.getRoomId()), data.getUserId());
        } else {
            setOperations.add(getRedisRoomManageUserKey(data.getRoomId()), data.getUserId());
        }
        return RestResult.success();
    }

    @Override
    public RestResult roomManageList(String roomId, JwtUser jwtUser) {
        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        List<RespRoomUser> outs = new ArrayList<>();
        //查询管理员列表
        Set<String> manageUserIds = setOperations.members(getRedisRoomManageUserKey(roomId));
        if (manageUserIds == null || manageUserIds.isEmpty()) {
            return RestResult.success(outs);
        }

        for (String manageUserId : manageUserIds) {
            RespRoomUser out = new RespRoomUser();
            TUser user = userService.getUserInfo(manageUserId);
            if (user != null) {
                out.setUserId(user.getUid());
                out.setUserName(user.getName());
                out.setPortrait(user.getPortrait());
                outs.add(out);
            }
        }

        return RestResult.success(outs);
    }

    @Override
    public RestResult chrmDelete(String roomId) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //删除服务器房间
        List<String> deleteRoomIds = new ArrayList<String>() {{
            add(roomId);
        }};

        IMApiResultInfo resultInfo = imHelper.deleteChrm(deleteRoomIds);
        if (!resultInfo.isSuccess()) {
            return RestResult.generic(RestResultCode.ERR_ROOM_ADD_GAG_USER_ERROR);
        }
        //调用删除房间
        ReqRoomStatusSync status = new ReqRoomStatusSync();
        status.setChatRoomId(room.getUid());
        status.setType(3);
        dealChrm(room, status);

        return RestResult.success();
    }

    @Override
    public RestResult roomBackground(ReqRoomBackground data, JwtUser jwtUser) {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        room.setBackgroundUrl(data.getBackgroundUrl());
        //更新数据库
        roomDao.updateRoomBackground(room.getUid(), room.getBackgroundUrl());
        //更新 Redis
        updateRoomCache(room);
        return RestResult.success();
    }

    @Override
    public RestResult musicList(ReqRoomMusicList data) {
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        List<TRoomMusic> list;
        if (data.getType() == ChrmMusicType.OFFICIAL.getValue()) {
            list = roomMusicDao.findTRoomMusicByTypeEqualsOrderBySortAsc(ChrmMusicType.OFFICIAL.getValue());
        } else {
            //应该显示的是 非官方音乐的所有音乐
            list = roomMusicDao.findTRoomMusicByTypeGreaterThanAndRoomIdEqualsOrderBySortAsc(ChrmMusicType.OFFICIAL.getValue(), data.getRoomId());
        }

        return RestResult.success(list);

    }

    @Override
    public RestResult addMusic(ReqRoomMusicAdd data, JwtUser jwtUser) {
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        TRoomMusic music = new TRoomMusic();
        Date date = DateTimeUtils.currentUTC();
        music.setCreateDt(date);
        music.setUpdateDt(date);
        music.setName(data.getName());
        music.setRoomId(data.getRoomId());
        music.setAuthor(data.getAuthor());
        music.setUrl(data.getUrl());
        music.setType(data.getType());
        if (data.getSize() != null) {
            music.setSize(String.format("%.1f", ((float) data.getSize() / 1024)));
        }
        //获取sort最大的数据+1
        Integer maxSort = roomMusicDao.getMaxSort(data.getRoomId());
        if (maxSort == null) {
            maxSort = 0;
        }
        music.setSort(maxSort + 1);
        //end
        int size = 0;
        if (data.getType() == ChrmMusicType.CUSTOM.getValue()) {
            if (music.getSize() == null) {
                try {
                    size = fileService.getFileSize(filepath + jwtUser.getUserId() + "/" + music.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                double f = size / 1024 / 1024;
                music.setSize(String.format("%.1f", f));
            }
        } else {//官方需要从库中获取
            TRoomMusic musicInfo = roomMusicDao.findTRoomMusicByTypeEqualsAndNameEquals(ChrmMusicType.OFFICIAL.getValue(), data.getName());
            music.setSize(musicInfo.getSize());
            music.setUrl(musicInfo.getUrl());
        }
        roomMusicDao.save(music);
        return RestResult.success();
    }

    @Override
    public RestResult musicDelete(ReqRoomMusicDelete data) {

        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        roomMusicDao.deleteTRoomMusicByIdEquals(data.getId());
        return RestResult.success();
    }

    @Override
    public RestResult roomCreateCheck(JwtUser jwtUser) {
        TRoom roomInfo = roomDao.findTRoomByUserIdEquals(jwtUser.getUserId());
        if (roomInfo != null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_IS_EXIST, build(roomInfo));
        }
        return RestResult.success();
    }

    @Override
    public RestResult addGift(ReqRoomGiftAdd data, JwtUser jwtUser) {
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        HashMap<String, Long> userGiftTotalValueList = new HashMap<>();
        //获取当前的礼物总价值
        Long currentValue = Long.valueOf(ChartRoomGift.getChartRoomGiftById(data.getGiftId()).getValue() * data.getNum());
        HashMap<String, Long> userTotalValue;
        userTotalValue = roomHashGifts.get(getRedisRoomGiftKey(data.getRoomId()), data.getToUid());
        log.info("gift log data:{}", userTotalValue);
        if (userTotalValue != null) {
            currentValue = currentValue + userTotalValue.get(data.getToUid());
        }
        userGiftTotalValueList.put(data.getToUid(), currentValue);
        roomHashGifts.put(getRedisRoomGiftKey(data.getRoomId()), data.getToUid(), userGiftTotalValueList);
        return RestResult.success();
    }

    @Override
    public RestResult giftList(String roomId, JwtUser jwtUser) {
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        List<HashMap<String, Long>> list = roomHashGifts.values(getRedisRoomGiftKey(roomId));
        return RestResult.success().setResult(list);
    }

    @Override
    public void removeRoom(TRoom room) throws Exception {
        if (room == null) {
            return;
        }
        //判断是否到达3个小时
        if (DateUtils.addHours(room.getUpdateDt(), roomExpire).compareTo(new Date()) > 0) {
            return;
        }
        //判断房间用户是否为0
        Integer total = this.getRoomUserTotal(room.getUid());
        if (total.equals(0)) {
            //删除 房间
            this.chrmDelete(room.getUid());
        }
    }

    @Override
    public RestResult roomUserGag(ReqRoomUserGag data, JwtUser jwtUser) throws Exception {

        if (data.getUserIds().isEmpty()) {
            return RestResult.generic(RestResultCode.ERR_REQUEST_PARA_ERR);
        }
        //每次最多只能设置 20 个用户
        if (data.getUserIds().size() > 20) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IDS_SIZE_EXCEED);
        }
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //IM 设置用户禁言
        IMApiResultInfo resultInfo;
        if (data.getOperation().equals(RoomUserGagOperation.ADD.getValue())) {
            resultInfo = imHelper.addGagChatroomUser(data.getRoomId(), data.getUserIds(), null);
        } else if (data.getOperation().equals(RoomUserGagOperation.REMOVE.getValue())) {
            resultInfo = imHelper.removeGagChatroomUser(data.getRoomId(), data.getUserIds());
        } else {
            return RestResult.generic(RestResultCode.ERR_REQUEST_PARA_ERR);
        }
        if (!resultInfo.isSuccess()) {
            return RestResult.generic(RestResultCode.ERR_ROOM_ADD_GAG_USER_ERROR);
        }

        //将禁言用户信息保存至 Redis
        if (data.getOperation().equals(RoomUserGagOperation.ADD.getValue())) {
            setOperations.add(getRedisRoomGagUserKey(data.getRoomId()), data.getUserIds().toArray(new String[0]));
        } else if (data.getOperation().equals(RoomUserGagOperation.REMOVE.getValue())) {
            setOperations.remove(getRedisRoomGagUserKey(data.getRoomId()), data.getUserIds().toArray(new String[0]));
        }

        return RestResult.success();
    }

    @Override
    public RestResult getRoomMembers(String roomId) throws Exception {
        List<RespRoomUser> outs = new ArrayList<>();
        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //从 IM 聊天室获取成员
        IMChatRoomUserResult result = imHelper.queryChatroomUser(roomId, 50, 1);
        if (result == null || !result.isSuccess() || result.getUsers() == null || result.getUsers().isEmpty()) {
            return RestResult.success(outs);
        }
        List<IMRoomUserInfo> userInfos = result.getUsers();
        for (IMRoomUserInfo roomUserInfo : userInfos) {
            TUser user = userService.getUserInfo(roomUserInfo.getId());
            if (user != null) {
                RespRoomUser out = new RespRoomUser();
                out.setUserId(user.getUid());
                out.setUserName(user.getName());
                out.setPortrait(user.getPortrait());
                outs.add(out);
            }
        }
        //同步缓存人员
        this.updateRoomUserTotal(roomId, outs.size());
        return RestResult.success(outs);
    }

    @Override
    public RestResult queryGagRoomUsers(String roomId, JwtUser jwtUser) {

        List<RespRoomUser> outs = new ArrayList<>();

        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        //查询禁言用户
        Set<String> gagUserIds = setOperations.members(getRedisRoomGagUserKey(roomId));
        if (gagUserIds == null || gagUserIds.isEmpty()) {
            return RestResult.success(outs);
        }

        for (String gagUserId : gagUserIds) {
            RespRoomUser out = new RespRoomUser();
            TUser user = userService.getUserInfo(gagUserId);
            if (user != null) {
                out.setUserId(user.getUid());
                out.setUserName(user.getName());
                out.setPortrait(user.getPortrait());
                outs.add(out);
            }
        }

        return RestResult.success(outs);
    }

    @Async
    @Override
    public void chrmStatusSync(List<ReqRoomStatusSync> data, Long signTimestamp, String nonce, String signature) {

        //校验签名是否正确
        if (!imSignUtil.validSign(nonce, signTimestamp.toString(), signature)) {
            log.info("Access denied: signature error, signTimestamp:{}, nonce:{}, signature:{}", signTimestamp, nonce, signature);
            return;
        }

        if (data == null || data.isEmpty()) {
            return;
        }

        //同步聊天室状态
        for (ReqRoomStatusSync status : data) {
            TRoom room = getRoomInfo(status.getChatRoomId());
            if (room == null) {
                continue;
            }
            dealChrm(room, status);
        }
    }


    private void dealChrm(TRoom room, ReqRoomStatusSync status) {
        if (status.getType() == ChartRoomSyncType.DESTORY.getValue()) {
            if (room.getType() == RoomType.OFFICIAL.getValue()) {
                //重置房间属性
                room.setAllowedJoinRoom(true);
                room.setAllowedFreeJoinMic(true);
                roomDao.updateRoomSetting(status.getChatRoomId(), true, false);
                updateRoomCache(room);

            } else if (room.getType() == RoomType.CUSTOM.getValue()) {
                //从缓存中删除房间对应的 Key
                redisTemplate.opsForHash().delete(getRedisRoomInfosKey(), status.getChatRoomId());
                redisTemplate.opsForZSet().remove(getRedisRoomIdsKey(), status.getChatRoomId());
                //从数据库中删除房间
                roomDao.deleteTRoomByUidEquals(room.getUid());
            }
            //删除房间用户统计
            redisHashOperations.delete(REDIS_ROOM_USER_TOTAL_KEY, status.getChatRoomId());
            //删除禁言用户列表
            redisTemplate.delete(getRedisRoomGagUserKey(status.getChatRoomId()));
            //删除管理员列表
            redisTemplate.delete(getRedisRoomManageUserKey(status.getChatRoomId()));
            //删除礼物列表
            Map<String, HashMap<String, Long>> entries = roomHashGifts.entries(getRedisRoomGiftKey(status.getChatRoomId()));
            entries.forEach((key, value) -> {
                roomHashGifts.delete(getRedisRoomGiftKey(status.getChatRoomId()), key);
            });

        } else if (status.getType() == ChartRoomSyncType.JOIN.getValue()) {
            //加入聊天室
            this.plusRoomUserTotal(status.getChatRoomId(), status.getUserIds() == null ? 0 : status.getUserIds().size());

            //同步更新redis 房间修改时间
            this.syncRoomUpdate(room);
        } else if (status.getType() == ChartRoomSyncType.QUIT.getValue()) {
            //退出聊天室
            this.reduceRoomUserTotal(status.getChatRoomId(), status.getUserIds() == null ? 0 : status.getUserIds().size());

            //同步更新redis 房间修改时间
            this.syncRoomUpdate(room);
        }
    }

    private void syncRoomUpdate(TRoom room) {
        room.setUpdateDt(new Date());
        this.updateRoomCache(room);
    }

    @Override
    public RestResult messageBroadcast(ReqBroadcastMessage data, JwtUser jwtUser)
            throws Exception {
        if (StringUtils.isEmpty(data.getFromUserId())) {
            data.setFromUserId(CustomerConstant.SYSTEM_USER_ID);
        }

        imHelper.publishBroadcastMessage(data.getFromUserId(), data.getObjectName(), data.getContent());

        return RestResult.success();
    }

    /**
     * 聊天室创建时，向 IM 聊天室初始化端上传的kv
     *
     * @param roomId 聊天室 ID
     * @throws Exception
     */
    private void initMicRoomKv(String roomId, List<Map<String, String>> kvs, String userId) throws Exception {
        log.info("kv info {}", GsonUtil.toJson(kvs));
        for (Map<String, String> item : kvs) {
            String kvKey = item.get("key");
            String kvValue = item.get("value");
            log.info("create room, key:{}, value:{}", kvKey, kvValue);
            ChrmEntrySetInfo entrySetInfo = new ChrmEntrySetInfo();
            entrySetInfo.setChrmId(roomId);
            entrySetInfo.setUserId(userId);
            entrySetInfo.setKey(kvKey);
            entrySetInfo.setValue(kvValue);
            entrySetInfo.setAutoDelete(false);
            imHelper.entrySet(entrySetInfo, 5);
        }
    }

    //============= Room 相关 ==================
    private boolean isInChrm(String roomId, String userId) throws Exception {
        // 从 IM 聊天室查询用户是否存在
        IMIsInChrmResult result = imHelper.isInChartRoom(roomId, userId);
        return result.isSuccess() && result.getIsInChrm();
    }

    private void updateRoomCache(TRoom room) {
        roomHashOperations.put(getRedisRoomInfosKey(), room.getUid(), room);
    }

    /**
     * @description: 删除房间redis
     * @date: 2021/6/2 10:14
     * @author: by zxw
     **/
    private void removeRoomCache(String roomId) {
        roomHashOperations.delete(getRedisRoomInfosKey(), roomId);
    }


    private TRoom getRoomInfo(String roomId) {
        //先从缓存查询，不存在则从数据库查询
        TRoom room = roomHashOperations.get(getRedisRoomInfosKey(), roomId);
        if (room != null) {
            return room;
        }
        log.info("get room info from db, roomId:{}", roomId);
        room = roomDao.findTRoomByUidEquals(roomId);
        if (room != null) {
            updateRoomCache(room);
        }
        return room;
    }

    public Map<String, TRoom> hmget(String key, List<String> fields) {
        List<TRoom> result = roomHashOperations.multiGet(key, fields);
        Map<String, TRoom> ans = new HashMap<>(fields.size());
        int index = 0;
        for (String field : fields) {
            if (result.get(index) == null) {
                continue;
            }
            ans.put(field, result.get(index));
            index++;
        }
        return ans;
    }

    private ResRoomInfo build(TRoom room) {
        ResRoomInfo roomInfo = new ResRoomInfo();
        roomInfo.setId(room.getId() + 10000);
        roomInfo.setRoomId(room.getUid());
        roomInfo.setRoomName(room.getName());
        roomInfo.setThemePictureUrl(room.getThemePictureUrl());
        roomInfo.setBackgroundUrl(room.getBackgroundUrl());
        roomInfo.setIsPrivate(room.getIsPrivate());
        roomInfo.setUserId(room.getUserId());
        roomInfo.setPassword(room.getPassword());
        roomInfo.setUpdateDt(room.getUpdateDt());

        TUser userInfo = userService.getUserInfo(room.getUserId());
        if (userInfo != null) {
            RespRoomUser roomUser = new RespRoomUser();
            roomUser.setPortrait(userInfo.getPortrait());
            roomUser.setUserId(userInfo.getUid());
            roomUser.setUserName(userInfo.getName());

            roomInfo.setCreateUser(roomUser);
        } else {
            // 删除 房间
            try {
                this.chrmDelete(room.getUid());
            } catch (Exception e) {
                log.error("[room list][user is null][delete err:{}]", e.getMessage());
            }
        }

        //添加用户统计数量
        roomInfo.setUserTotal(this.getRoomUserTotal(room.getUid()));

        return roomInfo;
    }

    private Integer getRoomUserTotal(String roomId) {
        Object total = roomHashOperations.get(REDIS_ROOM_USER_TOTAL_KEY, roomId);
        if (total == null) {
            return 0;
        }
        log.info("[room:" + roomId + "] is count:" + total);
        return (Integer) total;
    }


    /**
     * @description: 统计房间人员 加
     * @date: 2021/6/4 9:49
     * @author: by zxw
     **/
    private void plusRoomUserTotal(String roomId, Integer num) {
        Integer total = this.getRoomUserTotal(roomId);
        log.info("[room:" + roomId + "]  plus is " + num);
        this.updateRoomUserTotal(roomId, (total + num));
    }

    //统计房间人员  减
    private void reduceRoomUserTotal(String roomId, Integer num) {
        Integer total = this.getRoomUserTotal(roomId);
        Integer currentTotal = total - num;
        if (currentTotal < 0) {
            currentTotal = 0;
        }
        log.info("[room:" + roomId + "] reduce is " + num);
        this.updateRoomUserTotal(roomId, currentTotal);
    }


    private void updateRoomUserTotal(String roomId, Integer num) {
        log.info("[room:" + roomId + "] is user count:" + num);
        redisHashOperations.put(REDIS_ROOM_USER_TOTAL_KEY, roomId, num);
    }


    private String getRedisRoomIdsKey() {
        return CustomerConstant.SERVICENAME + "|room_ids";
    }

    /**
     * @description: 房间人员统计
     * @date: 2021/6/4 9:35
     * @author: by zxw
     **/
    private String REDIS_ROOM_USER_TOTAL_KEY = CustomerConstant.SERVICENAME + "|room_user_total";

    private String getRedisRoomInfosKey() {
        return CustomerConstant.SERVICENAME + "|rooms_info";
    }

    private String getRedisRoomGagUserKey(String roomId) {
        return CustomerConstant.SERVICENAME + "|gag_user|" + roomId;
    }

    private String getRedisRoomManageUserKey(String roomId) {
        return CustomerConstant.SERVICENAME + "|manage_user|" + roomId;
    }

    private String getRedisRoomGiftKey(String roomId) {
        return CustomerConstant.SERVICENAME + "|user_gift|" + roomId;
    }


    @PostConstruct
    private void init() throws Exception {
//       initChrmWhiteList();
    }

    /**
     * 添加聊天室消息白名单
     */
    private void initChrmWhiteList() throws Exception {

        List<String> chrmWhiteList = new ArrayList<>(
                Arrays.asList("RCMic:transferHostMsg", "RCMic:takeOverHostMsg", "RC:chrmKVNotiMsg", "RCMic:chrmSysMsg", "RCMic:gift", "RCMic:broadcastGift", "RCMic:chrmMsg"));

        //查询 IM 添加的消息白名单

        IMChrmWhiteListResult result = imHelper.chrmWhitelistQuery();
        if (!result.isSuccess()) {
            return;
        }
        List<String> imChrmWhiteList = result.getWhitlistMsgType();
        if (imChrmWhiteList == null) {
            imChrmWhiteList = new ArrayList<>();
        }
        imChrmWhiteList.remove("");
        List<String> finalImChrmWhiteList = imChrmWhiteList;

        List<String> needAdds = chrmWhiteList.stream().filter(item -> !finalImChrmWhiteList.contains(item)).collect(toList());
        List<String> needDeletes = finalImChrmWhiteList.stream().filter(item -> !chrmWhiteList.contains(item)).collect(toList());

        if (!needDeletes.isEmpty()) {
            imHelper.chrmWhitelistDelete(needDeletes);
        }

        if (!needAdds.isEmpty()) {
            imHelper.chrmWhitelistAdd(needAdds);
        }
    }

}
