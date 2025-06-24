package com.whc.picture.picture.controller.qo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 图片审核请求
 */
@Data
public class PictureReviewQO {

    /**
     * picture id
     */
    @NotNull(message = "图片id不能为null")
    private Long id;

    /**
     * 状态：0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

}
