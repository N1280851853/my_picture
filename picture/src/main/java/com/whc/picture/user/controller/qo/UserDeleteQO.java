package com.whc.picture.user.controller.qo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新用户信息请求接口
 */
@Data
public class UserDeleteQO implements Serializable {

    private static final long serialVersionUID = 1593282031093231029L;

    /**
     * id
     */
    @NotNull(message = "id不能为null")
    private Long id;

}
