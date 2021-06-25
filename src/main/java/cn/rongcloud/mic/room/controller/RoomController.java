package cn.rongcloud.mic.room.controller;

import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.jwt.JwtUser;
import cn.rongcloud.mic.jwt.filter.JwtFilter;
import cn.rongcloud.mic.room.pojos.*;
import cn.rongcloud.mic.room.service.RoomService;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/mic/room")
@Slf4j
public class RoomController {

    @Autowired
    RoomService roomService;

    /**
     * @api {post}  /mic/room/create 创建房间
     * @apiDescription 访问权限: 只有登录用户才可访问，游客无权限访问该接口
     * @apiVersion 1.0.0
     * @apiName createRoom
     * @apiGroup 房间模块
     * @apiParam {String} name 房间名称
     * @apiParam {String} themePictureUrl 房间主题图片地址
     * @apiParam {Int} isPrivate 是否私密房间  0 否 1 是
     * @apiParam {String} password 私密房间密码  MD5加密
     * @apiParam {String} backgroundUrl 房间背景图片地址
     * @apiParam {Array}  kv 房间kv列表
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/create HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "name": "test",
     * "themePictureUrl": "xxxx",
     * "isPrivate": 0,
     * "password": "md5(aaaaaaa)",
     * "kv": [{
     * "key": "key1",
     * "value": "val1"
     * },{
     * "key": "key2",
     * "value": "val2"
     * }]
     * }
     * @apiSuccess (返回结果) {Inter} code 10000 成功  30016 用户已创建房间
     * @apiSuccess (返回结果) {String} roomId 房间id
     * @apiSuccess (返回结果) {String} roomName 房间名称
     * @apiSuccess (返回结果) {String} themePictureUrl 房间主题图片地址
     * @apiSuccess (返回结果) {String} backgroundUrl 房间背景图
     * @apiSuccess (返回结果) {Timestamp} updateDt 更新时间
     * @apiSuccess (返回结果) {String} userId 创建人
     * @apiSuccess (返回结果) {Int} isPrivate 是否私有
     * @apiSuccess (返回结果) {String} password 私有密码
     * @apiSuccess (返回结果) {String} userName 创建人名称
     * @apiSuccess (返回结果) {String} portrait 创建人头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data": {
     * "id":10125,
     * "roomId": "2akJS6N5QOYsCKf5LhpgqY",
     * "roomName": "名称1",
     * "themePictureUrl": "xxxxxx",
     * "backgroundUrl": "",
     * "isPrivate": 0,
     * "password": "",
     * "userId": "xxxxxxxx",
     * "updateDt": 1555406087939,
     * "createUser" : {
     * "userId" : "98bde6fe-0f8d-457c-ad81-a2329be00894",
     * "userName" : "融云用户3527",
     * "portrait" : ""
     * }
     * }
     * }
     */
    @PostMapping(value = "/create")
    public RestResult createRoom(@RequestBody ReqRoomCreate data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("create room, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.createRoom(data, jwtUser);
    }

    /**
     * @api {get} /mic/room/list 获取房间列表
     * @apiVersion 1.0.0
     * @apiName getRoomList
     * @apiGroup 房间模块
     * @apiParam {Int} page 页码
     * @apiParam {Int} size 返回记录数(Query 参数，需拼接到 url 之后)
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/list?page=1&size=10 HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {Int} totalCount 总记录数
     * @apiSuccess (返回结果) {Int} id 房间号
     * @apiSuccess (返回结果) {String} roomId 房间id
     * @apiSuccess (返回结果) {String} roomName 房间名称
     * @apiSuccess (返回结果) {String} themePictureUrl 房间主题图片地址
     * @apiSuccess (返回结果) {String} backgroundUrl 房间背景图
     * @apiSuccess (返回结果) {Timestamp} updateDt 更新时间
     * @apiSuccess (返回结果) {String} userId 创建人
     * @apiSuccess (返回结果) {Int} isPrivate 是否私有
     * @apiSuccess (返回结果) {String} password 私有密码
     * @apiSuccess (返回结果) {String} userName 创建人名称
     * @apiSuccess (返回结果) {String} portrait 创建人头像
     * @apiSuccess (返回结果) {Integer} userTotal 房间统计用户数量
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data": {
     * "totalCount": 2,
     * "rooms": [{
     * "id":10125,
     * "roomId": "2akJS6N5QOYsCKf5LhpgqY",
     * "roomName": "名称1",
     * "themePictureUrl": "xxxxxx",
     * "backgroundUrl": "",
     * "isPrivate": 0,
     * "password": "",
     * "userId": "xxxxxxxx",
     * "updateDt": 1555406087939,
     * "createUser" : {
     * "userId" : "98bde6fe-0f8d-457c-ad81-a2329be00894",
     * "userName" : "融云用户3527",
     * "portrait" : ""
     * },
     * "userTotal" : 100
     * }, {
     * "id":10126,
     * "roomId": "3saDkSLFMdnsseOksdakJS6",
     * "roomName": "名称2",
     * "themePictureUrl": "xxxxxx",
     * "backgroundUrl": "",
     * "isPrivate": 0,
     * "password": "",
     * "userId": "xxxxxxxx",
     * "updateDt": 1555406087939,
     * "createUser" : {
     * "userId" : "98bde6fe-0f8d-457c-ad81-a2329be00894",
     * "userName" : "融云用户3527",
     * "portrait" : ""
     * },
     * "userTotal" : 100
     * <p>
     * }]
     * },
     * "images":["aaa.png","bbb.png"]
     * }
     */
    @GetMapping(value = "/list")
    public RestResult getRoomList(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser,
                                  @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size, HttpServletRequest request) {
        log.info("get room list, operator:{}, page:{}, size:{}", jwtUser.getUserId(), page, size);
        return roomService.getRoomList(page, size, request);
    }

    /**
     * @description: 清空所有房间
     * @date: 2021/6/10 13:31
     * @author: by zxw
     **/
    @GetMapping(value = "delete/all")
    public RestResult batchDel(HttpServletRequest request, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        RestResult result = roomService.getRoomList(1, 999, request);
        int success = 0;
        int fail = 0;
        if (result.isSuccess()) {
            ResRoomList resRoomList = (ResRoomList) result.getResult();
            if (resRoomList.getRooms() != null && resRoomList.getRooms().size() > 0) {
                for (ResRoomInfo info : resRoomList.getRooms()) {
                    try {
                        RestResult restResult = roomService.chrmDelete(info.getRoomId());
                        if (restResult.isSuccess()) {
                            success++;
                        } else {
                            fail++;
                        }
                    } catch (Exception e) {
                        log.error("[room][delete is error][roomId:" + info.getRoomId() + "]");
                    }
                }
            }
        }
        return RestResult.success("执行完成，成功" + success + "条，失败" + fail + "条！");
    }

    /**
     * @api {get} /mic/room/{id} 获取房间信息
     * @apiVersion 1.0.0
     * @apiName getRoomDetail
     * @apiGroup 房间模块
     * @apiParam {Int} roomId 房间id (Path 参数，需替换 url 地址中的 {roomId} 变量)
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/xxxxxx HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {String} roomId 房间id
     * @apiSuccess (返回结果) {Int} id 房间号
     * @apiSuccess (返回结果) {String} roomName 房间名称
     * @apiSuccess (返回结果) {String} themePictureUrl 房间主题图片地址
     * @apiSuccess (返回结果) {String} backgroundUrl 房间背景图
     * @apiSuccess (返回结果) {Timestamp} updateDt 更新时间
     * @apiSuccess (返回结果) {String} userId 创建人
     * @apiSuccess (返回结果) {Int} isPrivate 是否私有
     * @apiSuccess (返回结果) {String} password 私有密码
     * @apiSuccess (返回结果) {String} userName 创建人名称
     * @apiSuccess (返回结果) {String} portrait 创建人头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data": {
     * "id":23232
     * "roomId": "3saDkSLFMdnsseOksdakJS6",
     * "roomName": "名称2",
     * "themePictureUrl": "xxxxxx",
     * "backgroundUrl": "",
     * "isPrivate": 0,
     * "password": "",
     * "userId": "xxxxxxxx",
     * "updateDt": 1555406087939,
     * "createUser" : {
     * "userId" : "98bde6fe-0f8d-457c-ad81-a2329be00894",
     * "userName" : "融云用户3527",
     * "portrait" : ""
     * }
     * }
     * }
     */
    @GetMapping(value = "/{roomId}")
    public RestResult getRoomDetail(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("get room info, operator:{}, roomId:{}", jwtUser.getUserId(), roomId);
        return roomService.getRoomDetail(roomId);
    }

    /**
     * @api {put} /mic/room/setting 房间设置
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomSetting
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiParam {boolean} applyOnMic 申请上麦模式
     * @apiParam {boolean} applyAllLockMic 全麦锁麦
     * @apiParam {boolean} applyAllLockSeat 全麦锁座
     * @apiParam {boolean} setMute 设置静音
     * @apiParam {integer} setSeatNumber 设置4座，默认8座
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * PUT /mic/room/setting HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId": "xxxxxxx",
     * "applyOnMic": true,   //申请上麦模式
     * "applyAllLockMic": true,   //全麦锁麦
     * "applyAllLockSeat": true,  //全麦锁座
     * "setMute": true,    //静音
     * "setSeatNumber": 8  //设置4座、8座
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PutMapping(value = "/setting")
    public RestResult roomSetting(@RequestBody ReqRoomSetting data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("room setting, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomSetting(data, jwtUser);
    }


    /**
     * @api {get} /mic/room/{roomId}/setting 获取房间设置
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName getRoomSetting
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/{roomId}/setting HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"，
     * "result": {
     * "roomId": "xxxxxxx",
     * "applyOnMic": true,   //申请上麦模式
     * "applyAllLockMic": true,   //全麦锁麦
     * "applyAllLockSeat": true,  //全麦锁座
     * "setMute": true,    //静音
     * "setSeatNumber": 8  //设置4座、8座
     * }
     * }
     */
    @GetMapping(value = "/{roomId}/setting")
    public RestResult getRoomSetting(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("get room setting, operator:{}, roomId:{}", jwtUser.getUserId(), roomId);
        return roomService.getRoomSetting(roomId, jwtUser);
    }

    /**
     * @api {put} /mic/room/private 房间上锁解锁
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomPrivate
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiParam {Int} isPrivate 是否上锁
     * @apiParam {String}  password 房间密码
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * PUT /mic/room/private HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId": "xxxxxxx",
     * "isPrivate": 1,
     * "password": "xxxxxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PutMapping(value = "/private")
    public RestResult roomPrivate(@RequestBody ReqRoomPrivate data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("room private, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomPrivate(data, jwtUser);
    }

    /**
     * @api {get} /mic/room/create/check 用户创建房间验证
     * @apiVersion 1.0.0
     * @apiName createCheck
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/{roomId}/create/check HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。30016 未已创建
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PutMapping(value = "/create/check")
    public RestResult createCheck(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("room create check, operator:{}, data:{}", jwtUser.getUserId());
        return roomService.roomCreateCheck(jwtUser);
    }

    /**
     * @api {put} /mic/room/manage 房间管理员设置
     * @apiVersion 1.0.0
     * @apiName roomManage
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiParam {String} userId 人员id
     * @apiParam {Boolean} isManage 是否管理员
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * PUT /mic/room/manage HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId": "xxxxxxx",
     * "userId": "xxxx",
     * "isManage": true
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PutMapping(value = "/manage")
    public RestResult roomManage(@RequestBody ReqRoomMicManage data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        log.info("room manage, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomManage(data, jwtUser);
    }

    /**
     * @api {get} /mic/room/{roomId}/manage/list 房间管理员列表
     * @apiVersion 1.0.0
     * @apiName roomManageList
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/{roomId}/manage/list HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * "data": [{
     * "userId": "2akJS6N5QOYsCKf5LhpgqY",
     * "userName": "李晓明",
     * "portrait": "xxxxxxxx"
     * },
     * {
     * "userId": "sIl1nG5AQD8h-O7A2zlN5Q",
     * "userName": "张三",
     * "portrait": "xxxxx"
     * }
     * ]
     * }
     */
    @GetMapping(value = "/{roomId}/manage/list")
    public RestResult roomManageList(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("room manage list, operator:{}, data:{}", jwtUser.getUserId(), roomId);
        return roomService.roomManageList(roomId, jwtUser);
    }


    /**
     * @api {put} /mic/room/name 房间名称修改
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomName
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiParam {String}  name 房间名称
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * PUT /mic/room/name HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId": "xxxxxxx",
     * "name": "xxxxxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PutMapping(value = "/name")
    public RestResult roomName(@RequestBody ReqRoomName data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("room name, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomName(data, jwtUser);
    }

    /**
     * @api {put} /mic/room/background 房间背景修改
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomBackground
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiParam {String}  backgroundUrl 房间背景图
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * PUT /mic/room/background HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId": "xxxxxxx",
     * "backgroundUrl": "xxxxxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PutMapping(value = "/background")
    public RestResult roomBackground(@RequestBody ReqRoomBackground data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("room background, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomBackground(data, jwtUser);
    }

    /**
     * @api {get} /mic/room/{roomId}/members 获取房间成员列表
     * @apiVersion 1.0.0
     * @apiName roomMembers
     * @apiGroup 房间模块
     * @apiParam {Int} roomId 房间id (Path 参数，需替换 url 地址中的 {roomId} 变量)
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/setting/xxxxxxx/members HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {String} userId 用户id
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data": [{
     * "userId": "2akJS6N5QOYsCKf5LhpgqY",
     * "userName": "李晓明",
     * "portrait": "xxxxxxxx"
     * },
     * {
     * "userId": "sIl1nG5AQD8h-O7A2zlN5Q",
     * "userName": "张三",
     * "portrait": "xxxxx"
     * }
     * ]
     * }
     */
    @GetMapping(value = "/{roomId}/members")
    public RestResult getRoomMembers(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("get room members, operator:{}, roomId:{}", jwtUser.getUserId(), roomId);
        return roomService.getRoomMembers(roomId);
    }

    /**
     * @api {post} /mic/room/gag 用户禁言设置
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName roomUserGag
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id
     * @apiParam {String} operation 操作，add:禁言, remove:解除禁言
     * @apiParam {Array} userIds 用户id
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/xxxxxxx/user/gag HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * {
     * "roomId": "xxxxxxx",
     * "userIds": ["xxxxxxx","yyyyyyy"],
     * "operation": "add"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PostMapping(value = "/gag")
    public RestResult roomUserGag(@RequestBody ReqRoomUserGag data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("room user gag, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.roomUserGag(data, jwtUser);
    }

    /**
     * @api {get} /mic/room/{roomId}/gag/members 查询禁言用户列表
     * @apiDescription 访问权限: 只有主持人有权限操作
     * @apiVersion 1.0.0
     * @apiName queryGagRoomUsers
     * @apiGroup 房间模块
     * @apiParam {String} roomId 房间id，(Path 参数，需替换 url 地址中的 {roomId} 变量)
     * @apiParam {Array} userIds 用户id
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/xxxxxx/gag/members HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * @apiSuccess (返回结果) {String} userId 用户id
     * @apiSuccess (返回结果) {String} userName 用户名称
     * @apiSuccess (返回结果) {String} portrait 头像
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data": [{
     * "userId": "2akJS6N5QOYsCKf5LhpgqY",
     * "userName": "李晓明",
     * "portrait": "xxxxxxxx"
     * },
     * {
     * "userId": "sIl1nG5AQD8h-O7A2zlN5Q",
     * "userName": "张三",
     * "portrait": "xxxxx"
     * }
     * ]
     * }
     */
    @GetMapping(value = "/{roomId}/gag/members")
    public RestResult queryGagRoomUsers(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("query room gag users , operator:{}, roomId:{}", jwtUser.getUserId(), roomId);
        return roomService.queryGagRoomUsers(roomId, jwtUser);
    }

    /**
     * @api {post} /mic/room/message/broadcast 发送聊天室广播消息
     * @apiVersion 1.0.0
     * @apiName roomMessageBroadcast
     * @apiGroup 消息模块
     * @apiParam {String} fromUserId 发送人用户 Id。
     * @apiParam {String} objectName 消息类型，参考融云消息类型表.消息标志；可自定义消息类型，长度不超过 32 个字符，您在自定义消息时需要注意，不要以 "RC:" 开头，以避免与融云系统内置消息的 ObjectName 重名。（必传）
     * @apiParam {String} content 发送消息内容，单条消息最大 128k，内置消息以 JSON 方式进行数据序列化，消息中可选择是否携带用户信息，详见融云内置消息结构详解；如果 objectName 为自定义消息类型，该参数可自定义格式，不限于 JSON。（必传）
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/message/broadcast HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "fromUserId": "xxxxx",
     * "objectName": "RC:TxtMsg"
     * "content":"xxxxx"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PostMapping(value = "/message/broadcast")
    public RestResult messageBroadcast(@RequestBody ReqBroadcastMessage data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("message broadcast, operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.messageBroadcast(data, jwtUser);
    }


    /**
     * @api {post} /mic/room/status_sync IM 聊天室状态同步
     * @apiVersion 1.0.0
     * @apiName roomStatusSync
     * @apiGroup 房间模块
     * @apiParam {String} chatRoomId 聊天室 Id。
     * @apiParam {String[]} userIds 用户 Id 数据。
     * @apiParam {String} status 操作状态：0：直接调用接口 1：触发融云退出聊天室机制将用户踢出（聊天室中用户在离线 30 秒后有新消息产生时或离线后聊天室中产生 30 条消息时会被自动退出聊天室，此状态需要聊天室中有新消息时才会进行同步）2：用户被封禁 3：触发融云销毁聊天室机制自动销毁
     * @apiParam {String} type 聊天室事件类型：0 创建聊天室、1 加入聊天室、2 退出聊天室、3 销毁聊天室
     * @apiParam {Long} time 发生时间。
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/status_sync HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * [
     * {
     * "chatRoomId":"destory_11",
     * "userIds":["gggg"],
     * "status":0,
     * "type":1,
     * "time":1574476797772
     * },
     * {
     * "chatRoomId":"destory_12",
     * "userIds":[],
     * "status":0,
     * "type":0,
     * "time":1574476797772
     * }
     * ]
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @PostMapping(value = "/status_sync")
    public RestResult statusSync(@RequestBody List<ReqRoomStatusSync> data,
                                 @RequestParam(value = "signTimestamp") Long signTimestamp,
                                 @RequestParam(value = "nonce") String nonce,
                                 @RequestParam(value = "signature") String signature) {
        log.info("chartroom status sync, data:{}", GsonUtil.toJson(data));
        roomService.chrmStatusSync(data, signTimestamp, nonce, signature);
        return RestResult.success();
    }

    /**
     * @api {get} /mic/room/{roomId}/delete IM 删除聊天室
     * @apiVersion 1.0.0
     * @apiName roomDelete
     * @apiGroup 房间模块
     * @apiParam {String} roomId 聊天室 Id。
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * GET /mic/room/xxxxx/delete HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success"
     * }
     */
    @GetMapping(value = "/{roomId}/delete")
    public RestResult statusSync(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        log.info("chartroom delete , data:{}", GsonUtil.toJson(roomId));
        roomService.chrmDelete(roomId);
        return RestResult.success();
    }

    /**
     * @api {post} /mic/room/music/list 聊天室音乐列表
     * @apiVersion 1.0.0
     * @apiName roomMusicList
     * @apiGroup 房间模块
     * @apiParam {String} roomId 聊天室 Id。
     * @apiParam {Inter} type 聊天室类型 0 官方，1 自定义。
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/music/list HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId":"xxxx",
     * "type":0
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccess (返回结果) {Int} id 音乐id
     * @apiSuccess (返回结果) {String} name 名称
     * @apiSuccess (返回结果) {String} author 音乐作者
     * @apiSuccess (返回结果) {String} roomId 聊天室id
     * @apiSuccess (返回结果) {String} size 文件大小 单位 M
     * @apiSuccess (返回结果) {Int} type 0 官方  1 本地添加 2 从官方添加
     * @apiSuccess (返回结果) {Timestamp} updateDt 更新时间
     * @apiSuccess (返回结果) {Timestamp} createDt 创建时间
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data":
     * [
     * {
     * "id" : 1,
     * "name" : "a",
     * "author" : "b",
     * "roomId" : "sdfsdf",
     * "type" : 0,
     * "url" : "1",
     * "size" : "154",
     * "createDt" : 1621951183000,
     * "updateDt" : 1621951185000
     * }
     * ]
     * }
     */
    @PostMapping(value = "/music/list")
    public RestResult musicList(@RequestBody ReqRoomMusicList data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("chartroom music list, data:{}", GsonUtil.toJson(data));
        return roomService.musicList(data);
    }

    /**
     * @api {post}  /mic/room/music/add 添加音乐
     * @apiDescription 访问权限: 只有登录用户才可访问，游客无权限访问该接口
     * @apiVersion 1.0.0
     * @apiName addRoomMusic
     * @apiGroup 房间模块
     * @apiParam {String} name 音乐名称
     * @apiParam {String} author 作者
     * @apiParam {String} name 名称
     * @apiParam {String} author 音乐作者
     * @apiParam {String} roomId 聊天室id
     * @apiParam {Int} type  1 本地添加 2 从官方添加
     * @apiParam {String} url  音乐url
     * @apiParam {Integer} size  音乐大小（默认KB）
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/music/add HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "name" : "xxxx",
     * "author" : "xxx",
     * "roomId" : "xxxxx",
     * "type" : 0,
     * "url" : "xxxxx",
     * "size" : "100"
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccess (返回结果) {Int} id 音乐id
     * @apiSuccess (返回结果) {String} name 名称
     * @apiSuccess (返回结果) {String} author 音乐作者
     * @apiSuccess (返回结果) {String} roomId 聊天室id
     * @apiSuccess (返回结果) {String} size 文件大小 单位 M
     * @apiSuccess (返回结果) {Int} type 0 官方  1 本地添加 2 从官方添加
     * @apiSuccess (返回结果) {Timestamp} updateDt 更新时间
     * @apiSuccess (返回结果) {Timestamp} createDt 创建时间
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data": {
     * "id" : 1,
     * "name" : "a",
     * "author" : "b",
     * "roomId" : "sdfsdf",
     * "type" : 0,
     * "url" : "1",
     * "size" : "154",
     * "createDt" : 1621951183000,
     * "updateDt" : 1621951185000
     * }
     * }
     */
    @PostMapping(value = "/music/add")
    public RestResult musicAdd(@RequestBody ReqRoomMusicAdd data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("room music add , operator:{}, data:{}", jwtUser, GsonUtil.toJson(data));
        return roomService.addMusic(data, jwtUser);
    }

    /**
     * @api {post}  /mic/room/music/move 添加音乐
     * @apiDescription 访问权限: 只有登录用户才可访问，游客无权限访问该接口
     * @apiVersion 1.0.0
     * @apiName addRoomMusic
     * @apiGroup 房间模块
     * @apiParam {Inter} fromId 音乐id 放到 toId 的后面
     * @apiParam {Inter} toId 音乐id
     * @apiParam {Inter} roomId 聊天室id
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/music/move HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId" : "xxxxx",
     * "fromId" : 0,
     * "toId":0
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * }
     */
    @PostMapping(value = "/music/move")
    public RestResult musicMove(@RequestBody ReqRoomMusicMove data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("room music move , operator:{}, data:{}", jwtUser, GsonUtil.toJson(data));
        return roomService.musicMove(data, jwtUser);
    }

    /**
     * @api {post} /mic/room/music/delete 聊天室音乐删除
     * @apiVersion 1.0.0
     * @apiName roomMusicDelete
     * @apiGroup 房间模块
     * @apiParam {String} roomId 聊天室 Id。
     * @apiParam {Inter} id 音乐id
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/music/delete HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId":"xxxx",
     * "id":xxx
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * }
     */
    @PostMapping(value = "/music/delete")
    public RestResult musicDelete(@RequestBody ReqRoomMusicDelete data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) {
        log.info("chartroom music delete, data:{}", GsonUtil.toJson(data));
        return roomService.musicDelete(data);
    }

    /**
     * @api {post}  /mic/room/gift/add 赠送礼物
     * @apiDescription 访问权限: 只有登录用户才可访问，游客无权限访问该接口
     * @apiVersion 1.0.0
     * @apiName addRoomGift
     * @apiGroup 房间模块
     * @apiParam {String} roomId  房间id
     * @apiParam {String} giftId  礼物id
     * @apiParam {String} toUid  接收人Id
     * @apiParam {Int} num  数量
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/gift/add HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * {
     * "roomId" : "xxxx",
     * "giftId" : 1,
     * "toUid":"xxxx",
     * "num":10
     * }
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * }
     */
    @PostMapping(value = "/gift/add")
    public RestResult giftAdd(@RequestBody ReqRoomGiftAdd data, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("room gift add , operator:{}, data:{}", jwtUser.getUserId(), GsonUtil.toJson(data));
        return roomService.addGift(data, jwtUser);
    }

    /**
     * @api {post}  /mic/room/{roomId}/gift/list 赠送礼物列表
     * @apiDescription 访问权限: 只有登录用户才可访问，游客无权限访问该接口
     * @apiVersion 1.0.0
     * @apiName addRoomGiftList
     * @apiGroup 房间模块
     * @apiParam {String} roomId 聊天室id
     * @apiHeader {String} Authorization Authorization 头部需要传的值
     * @apiSampleRequest off
     * @apiParamExample 请求参数示例:
     * POST /mic/room/{roomId}/gift/list HTTP/1.1
     * Host: api-cn.ronghub.com
     * Authorization: authorization
     * Content-Type: application/json;charset=UTF-8
     * @apiSuccess (返回结果) {Int} code 返回码，10000 为正常。
     * @apiSuccessExample {json} 服务响应示例:
     * {
     * "code": 10000,
     * "msg": "success",
     * "data" : [ {
     * "userid1" : 240
     * } ]
     * }
     */
    @GetMapping(value = "/{roomId}/gift/list")
    public RestResult giftList(@PathVariable("roomId") String roomId, @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws Exception {
        log.info("room gift list , operator:{}, data:{}", jwtUser.getUserId(), roomId);
        return roomService.giftList(roomId, jwtUser);
    }


}
