package com.whc.picture.picture.controller.qo;

import com.whc.picture.bean.PageQO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 图片分页查询请求
 */
@Data
@Accessors(chain = true)
public class ListPagePictureQO extends PageQO {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    /**
     * 用户 id
     */
    private Long userId;

    /*
     * 上传时间-开始
     */
    private LocalDateTime startUploadTime;

    /*
     * 上传时间-结束
     */
    private LocalDateTime endUploadTime;

}
