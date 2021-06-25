package cn.rongcloud.mic.user.pojos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ReqUpdate {
    private String userName;
    private String portrait;

}
