package cn.rongcloud.mic.room.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class ReqRoomCreate {

    @NotBlank(message = "room name should not be blank")
    private String name;

    @NotBlank(message = "room themePictureUrl should not be blank")
    private String themePictureUrl;

    private Integer isPrivate;

    private String password;

    private String backgroundUrl;


    private List<Map<String,String>> kv;

}
