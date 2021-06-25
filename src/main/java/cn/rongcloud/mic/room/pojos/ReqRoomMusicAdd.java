package cn.rongcloud.mic.room.pojos;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ReqRoomMusicAdd {
    @NotBlank(message = "roomId should not be blank")
    private String roomId;

    @NotNull(message = "name should not be null")
    private String name;
    private String author;
    @NotNull(message = "type should not be null")
    private Integer type;
    @NotNull(message = "url should not be null")
    private String url;
    //默认kb
    private Integer size;

}
