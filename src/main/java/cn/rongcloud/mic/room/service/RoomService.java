package cn.rongcloud.mic.room.service;

import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.jwt.JwtUser;
import cn.rongcloud.mic.room.model.TRoom;
import cn.rongcloud.mic.room.pojos.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by sunyinglong on 2020/6/3
 */
public interface RoomService {

    RestResult createRoom(ReqRoomCreate data, JwtUser jwtUser) throws Exception;

    RestResult getRoomDetail(String roomId);

    RestResult getRoomList(Integer page, Integer size, HttpServletRequest request);

    List<TRoom> getRoomListAll();

    void removeRoom(TRoom room) throws Exception;

    RestResult roomSetting(ReqRoomSetting data, JwtUser jwtUser);

    RestResult roomUserGag(ReqRoomUserGag data, JwtUser jwtUser) throws Exception;

    RestResult getRoomMembers(String roomId) throws Exception;

    RestResult queryGagRoomUsers(String roomId, JwtUser jwtUser);

    void chrmStatusSync(List<ReqRoomStatusSync> data, Long signTimestamp, String nonce, String signature);

    RestResult messageBroadcast(ReqBroadcastMessage data, JwtUser jwtUser) throws Exception;

    RestResult roomPrivate(ReqRoomPrivate data, JwtUser jwtUser);

    RestResult roomName(ReqRoomName data, JwtUser jwtUser);

    RestResult roomManage(ReqRoomMicManage data, JwtUser jwtUser) throws Exception;

    RestResult roomManageList(String roomId, JwtUser jwtUser);

    RestResult chrmDelete(String roomId) throws Exception;

    RestResult roomBackground(ReqRoomBackground data, JwtUser jwtUser);

    RestResult musicList(ReqRoomMusicList data);

    RestResult addMusic(ReqRoomMusicAdd data, JwtUser jwtUser);

    RestResult musicDelete(ReqRoomMusicDelete data);

    RestResult roomCreateCheck(JwtUser jwtUser);

    RestResult addGift(ReqRoomGiftAdd data, JwtUser jwtUser);

    RestResult giftList(String roomId, JwtUser jwtUser);

    RestResult getRoomSetting(String roomId, JwtUser jwtUser);

    RestResult musicMove(ReqRoomMusicMove data, JwtUser jwtUser);

}
