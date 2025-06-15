package com.whc.picture.picture.service;

import com.whc.picture.entity.picture.entity.PictureTagDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author simceredx
* @description 针对表【picture_tag(图片、标签关联关系表)】的数据库操作Service
* @createDate 2025-06-14 18:22:13
*/
public interface PictureTagService extends IService<PictureTagDO> {


    List<PictureTagDO> findPictureTagByPictureId(Long pictureId);

    /**
     * 保存图片关联的标签信息
     * @param pictureId
     * @param pictureName
     * @param tags
     */
    void savePictureRelationTag(Long pictureId, String pictureName, List<String> tags);

    /**
     * 根据图片id，删除改图片下绑定的所有标签
     * @param pictureId
     */
    void deletePictureRelationTag(Long pictureId);
}
