package cn.rongcloud.mic.room.pojos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class ReqRoomMusicDelete {
    @NotBlank(message = "roomId should not be blank")
    private String roomId;

    @NotNull(message = "id should not be null")
    private Long id;

}
