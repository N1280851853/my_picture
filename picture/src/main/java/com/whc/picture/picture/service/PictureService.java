package com.whc.picture.picture.service;

import com.whc.picture.entity.picture.entity.PictureDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.picture.controller.qo.PictureQO;
import com.whc.picture.picture.controller.qo.PictureUpdateQO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author simceredx
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-13 16:30:50
*/
public interface PictureService extends IService<PictureDO> {

    /**
     * 上传图片
     *
     * @param pictureQO
     * @param loginUser
     * @return
     */
    PictureDO uploadPicture(MultipartFile multipartFile, PictureQO pictureQO,
                            UserDO loginUser);


    /**
     * 更新图片信息
     * @param qo
     */
    void updatePicture(PictureUpdateQO qo);
}
