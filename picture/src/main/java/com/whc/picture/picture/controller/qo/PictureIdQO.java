package com.whc.picture.picture.controller.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 图片ID
 */
@Data
@Accessors(chain = true)
public class PictureIdQO {

    /**
     * id
     */
    @NotNull(message = "图片ID不能为null")
    private Long id;

}
