package cn.rongcloud.mic.room.pojos;

import java.util.Date;
import java.util.HashMap;

import cn.rongcloud.mic.user.model.TUser;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class ResRoomInfo {

    private Long id;

    private String roomId;

    private String roomName;

    private String themePictureUrl;

    private String backgroundUrl;

    private Integer isPrivate;

    private String password;

    private String userId;

    private Date updateDt;

    private RespRoomUser createUser;

    // 房间统计用户数量
    private Integer userTotal;
}
