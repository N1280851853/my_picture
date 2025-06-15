package com.whc.picture.picture.controller.qo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class PictureQO {

    /**
     * 图片Id
     */
    private Long id;

    /**
     * 文件
     */
    private MultipartFile image;

}
