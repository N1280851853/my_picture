package com.whc.picture.space.controller.qo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SpaceIdQO {
    /**
     * 空间id
     */
    @NotNull
    private Long id;
}
