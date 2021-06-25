package cn.rongcloud.mic.room.pojos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ReqRoomName {
    @NotBlank(message = "roomId should not be blank")
    private String roomId;

    @NotNull(message = "name should not be null")
    private String name;

}
