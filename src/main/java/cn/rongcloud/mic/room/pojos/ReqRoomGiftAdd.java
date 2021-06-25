package cn.rongcloud.mic.room.pojos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class ReqRoomGiftAdd {
    @NotBlank(message = "roomId should not be blank")
    private String roomId;
    @NotNull(message = "giftId should not be null")
    private Integer num;
    @NotNull(message = "giftId should not be null")
    private Integer giftId;
    @NotNull(message = "toUid should not be null")
    private String toUid;

}
