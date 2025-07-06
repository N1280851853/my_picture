package com.whc.picture.picture.service;

import com.whc.picture.entity.picture.entity.PictureDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.picture.controller.qo.PictureQO;
import com.whc.picture.picture.controller.qo.PictureReviewQO;
import com.whc.picture.picture.controller.qo.PictureUpdateQO;
import com.whc.picture.picture.controller.qo.PictureUploadByBatchQO;

/**
* @author simceredx
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-13 16:30:50
*/
public interface PictureService extends IService<PictureDO> {

    /**
     * 上传图片
     *
     * @param inputSource
     * @param loginUser
     * @return
     */
    PictureDO uploadPicture(Object inputSource, PictureQO pictureQO,
                            UserDO loginUser);


    /**
     * 更新图片信息
     * @param qo
     */
    void updatePicture(PictureUpdateQO qo, UserDO loginUser);

    /**
     * 删除图片
     * @param oldPicture
     */
    void deletePictureById(PictureDO oldPicture);

    /**
     * 图片审核
     * @param qo
     * @param loginUser
     */
    void doPictureReview(PictureReviewQO qo, UserDO loginUser);

    /**
     * 填充审核参数
     * @param pictureDO
     * @param loginUser
     */
    void fillReviewParams(PictureDO pictureDO, UserDO loginUser);

    /**
     * 批量上传图片
     * @param qo
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchQO qo, UserDO loginUser);

    /**
     * 清理图片文件
     * @param oldPicture
     */
    void clearPictureFile(PictureDO oldPicture);

    /**
     * 校验图片空间的权限
     * @param loginUser
     * @param pictureDO
     */
    void checkPictureAuth(UserDO loginUser, PictureDO pictureDO);
}
