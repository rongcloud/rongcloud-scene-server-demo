package cn.rongcloud.mic.room.pojos;

import lombok.Data;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Data
public class ReqRoomMicManage {

    @NotBlank(message = "roomId should not be blank")
    private String roomId;

    @NotBlank(message = "userId should not be blank")
    private String userId;

    @NotBlank(message = "isManage should not be blank")
    private Boolean isManage;
}
