package com.whc.picture.picture.controller.qo;

import lombok.Data;


@Data
public class PictureQO {

    /**
     * 图片Id
     */
    private Long id;

    /**
     * 文件 url 路径
     */
    private String fileUrl;

}
