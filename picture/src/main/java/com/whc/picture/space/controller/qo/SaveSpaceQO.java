package com.whc.picture.space.controller.qo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SaveSpaceQO implements Serializable {
    /**
     * 空间名称
     */
    @Length(max = 30)
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    @Serial
    private static final long serialVersionUID = 1L;
}
