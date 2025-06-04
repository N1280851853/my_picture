package com.whc.picture.user.controller.qo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author： whc
 * @create： 2025/6/2 16:56
 */
@Data
public class UserLoginQO implements Serializable {

    private static final long serialVersionUID = 6923441123763458086L;
    /**
     * 账号
     */
    @NotEmpty(message = "用户账号不能为空")
    @Size(min = 4, message = "用户账号过短，不能小于4")
    public String userAccount;

    /**
     * 密码
     */
    @NotEmpty(message = "用户密码不能为空")
    @Size(min = 8, message = "用户密码过短，不能小于8")
    private String userPassword;

}
