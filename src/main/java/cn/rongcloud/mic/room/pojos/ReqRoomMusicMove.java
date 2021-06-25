package cn.rongcloud.mic.room.pojos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Description:
 * @Date: 2021/6/3 10:43
 * @Author: by zxw
 */
@Data
public class ReqRoomMusicMove {
    @NotBlank(message = "roomId should not be blank")
    private String roomId;
    @NotNull(message = "fromId should not be null")
    private Long fromId;
    @NotNull(message = "toId should not be null")
    private Long toId;
}
