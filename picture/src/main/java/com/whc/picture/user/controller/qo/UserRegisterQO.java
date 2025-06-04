package com.whc.picture.user.controller.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author： whc
 * @create： 2025/6/2 13:28
 */
@Data
@Accessors(chain = true)
public class UserRegisterQO implements Serializable {

    private static final long serialVersionUID = 4915896887711499506L;

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

    /**
     * 确认密码
     */
    @NotEmpty(message = "确认密码不能为空")
    private String checkPassword;

}
