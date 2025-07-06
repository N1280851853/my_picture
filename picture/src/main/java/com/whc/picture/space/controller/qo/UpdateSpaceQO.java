package com.whc.picture.space.controller.qo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Data
public class UpdateSpaceQO implements Serializable {

    /**
     * id
     */
    @NotNull
    private Long id;

    /**
     * 空间名称
     */
    @Length(max = 30)
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    @Serial
    private static final long serialVersionUID = 1L;
}
