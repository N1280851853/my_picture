package com.whc.picture.entity.picture.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 图片、标签关联关系表
 * @TableName picture_tag
 */
@TableName(value ="picture_tag")
@Data
@Accessors(chain = true)
public class PictureTagDO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片ID
     */
    @TableField(value = "picture_id")
    private Long pictureId;

    /**
     * 图片名称
     */
    @TableField(value = "picture_name")
    private String pictureName;

    /**
     * 标签Id
     */
    @TableField(value = "tag_id")
    private Long tagId;

    /**
     * 标签名称
     */
    @TableField(value = "tag_name")
    private String tagName;

    /**
     * 创建时间
     */
    @TableField(value = "gmt_create")
    private Date gmtCreate;

    /**
     * 更新时间
     */
    @TableField(value = "gmt_modified")
    private Date gmtModified;

    /**
     * 是否删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Integer isDelete;
}