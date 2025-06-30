package com.whc.picture.picture.controller.qo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class PictureUploadByBatchQO {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取条数
     */
    private Integer count = 10;

    /**
     * 图片名称前缀
     */
    private String namePrefix;
}
