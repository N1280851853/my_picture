package com.whc.picture.picture.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.manager.FileManager;
import com.whc.picture.picture.controller.qo.PictureQO;
import com.whc.picture.picture.controller.qo.PictureUpdateQO;
import com.whc.picture.picture.service.PictureService;
import com.whc.picture.picture.mapper.PictureMapper;
import com.whc.picture.picture.service.PictureTagService;
import com.whc.picture.tag.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
* @author simceredx
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-06-13 16:30:50
*/
@Service
@Transactional
public class PictureServiceImpl extends ServiceImpl<PictureMapper, PictureDO>
    implements PictureService{

    @Resource
    private FileManager fileManager;

    @Resource
    private TagService tagService;

    @Resource
    private PictureTagService pictureTagService;

    @Override
    public PictureDO uploadPicture(PictureQO pictureQO, UserDO loginUser) {
        Long id = pictureQO.getId();
        MultipartFile multipartFile = pictureQO.getImage();

        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 上传图片，得到图片信息
        // 划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        PictureDO pictureDO = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        pictureDO.setUserId(loginUser.getId());

        // 判断是 新增还是删除
        if (ObjectUtil.isNotEmpty(id)) {
            Boolean exists = this.lambdaQuery()
                    .eq(PictureDO::getId, id)
                    .exists();
            if (exists) { // 更新
                pictureDO.setId(id);
            }
        }

        // saveOrUpdate() 会根据传入的对象是否有id，来决定是更新还是新增
        this.saveOrUpdate(pictureDO);

        return pictureDO;
    }

    @Override
    public void updatePicture(PictureUpdateQO qo) {
        Long pictureId = qo.getId();
        String pictureName = qo.getName();
        String introduction = qo.getIntroduction();
        String category = qo.getCategory();
        List<String> tags = qo.getTags();

        PictureDO pictureDO = new PictureDO();
        pictureDO.setId(pictureId)
                .setName(pictureName)
                .setIntroduction(introduction)
                .setCategory(category);
        this.updateById(pictureDO);

        // 保存标签
        tagService.insertTag(tags);

        // 删除图片之前绑定的所有标签
        pictureTagService.deletePictureRelationTag(pictureId);

        // 保存标签与图片的关联关系
        pictureTagService.savePictureRelationTag(pictureId, pictureName, tags);

    }

}




