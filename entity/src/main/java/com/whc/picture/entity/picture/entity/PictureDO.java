package com.whc.picture.entity.picture.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 图片
 * @TableName picture
 */
@TableName(value ="picture")
@Data
@Accessors(chain = true)
public class PictureDO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片 url
     */
    @TableField(value = "url")
    private String url;

    /**
     * 图片名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 简介
     */
    @TableField(value = "introduction")
    private String introduction;

    /**
     * 分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 图片体积
     */
    @TableField(value = "pic_size")
    private Long picSize;

    /**
     * 图片宽度
     */
    @TableField(value = "pic_width")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @TableField(value = "pic_height")
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @TableField(value = "pic_scale")
    private Double picScale;

    /**
     * 图片格式
     */
    @TableField(value = "pic_format")
    private String picFormat;

    /**
     * 创建用户 id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "gmt_create")
    private LocalDateTime gmtCreate;

    /**
     * 更新时间
     */
    @TableField(value = "gmt_modified")
    private LocalDateTime gmtModified;

    /**
     * 是否删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Integer isDelete;
}