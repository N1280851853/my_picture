package com.whc.picture.picture.controller.qo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeletePictureQO {

    /**
     * 图片Id
     */
    @NotNull(message = "图片ID不能为null")
    private Long id;

}
