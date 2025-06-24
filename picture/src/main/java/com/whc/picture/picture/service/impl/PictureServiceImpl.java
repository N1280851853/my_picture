package com.whc.picture.picture.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.entity.picture.common.PictureReviewStatusEnum;
import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.manager.FileManager;
import com.whc.picture.manager.upload.FilePictureUpload;
import com.whc.picture.manager.upload.PictureUploadTemplate;
import com.whc.picture.manager.upload.UrlPictureUpload;
import com.whc.picture.picture.controller.qo.PictureQO;
import com.whc.picture.picture.controller.qo.PictureReviewQO;
import com.whc.picture.picture.controller.qo.PictureUpdateQO;
import com.whc.picture.picture.service.PictureService;
import com.whc.picture.picture.mapper.PictureMapper;
import com.whc.picture.picture.service.PictureTagService;
import com.whc.picture.tag.service.TagService;
import com.whc.picture.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author simceredx
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-06-13 16:30:50
 */
@Service
@Transactional
public class PictureServiceImpl extends ServiceImpl<PictureMapper, PictureDO>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private TagService tagService;

    @Resource
    private PictureTagService pictureTagService;

    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload  urlPictureUpload;

    @Override
    public PictureDO uploadPicture(Object inputSource, PictureQO pictureQO, UserDO loginUser) {
        Long pictureId = pictureQO.getId();

        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 上传图片，得到图片信息
        // 划分目录
        Long userId = loginUser.getId();
        String uploadPathPrefix = String.format("public/%s", userId);
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        PictureDO pictureDO = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        pictureDO.setUserId(userId);

        // 判断是 新增还是删除
        if (ObjectUtil.isNotEmpty(pictureId)) {
            PictureDO oldPictureDO = this.lambdaQuery()
                    .select(PictureDO::getUserId)
                    .eq(PictureDO::getId, pictureId)
                    .one();

            // 更新，要把图片id设置上去
            pictureDO.setId(pictureId);
            // 仅本人和管理员可编辑图片
            if (!oldPictureDO.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 插入数据库之前补充审核参数
        this.fillReviewParams(pictureDO, loginUser);

        // saveOrUpdate() 会根据传入的对象是否有id，来决定是更新还是新增
        this.saveOrUpdate(pictureDO);

        return pictureDO;
    }

    @Override
    public void updatePicture(PictureUpdateQO qo, UserDO loginUser) {
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

        this.fillReviewParams(pictureDO, loginUser);

        this.updateById(pictureDO);

        // 保存标签
        tagService.insertTag(tags);

        // 删除图片之前绑定的所有标签
        pictureTagService.deletePictureRelationTag(pictureId);

        // 保存标签与图片的关联关系
        pictureTagService.savePictureRelationTag(pictureId, pictureName, tags);

    }

    @Override
    public void doPictureReview(PictureReviewQO qo, UserDO loginUser) {
        Long pictureId = qo.getId();
        Integer reviewStatus = qo.getReviewStatus();
        String reviewMessage = qo.getReviewMessage();
        // 1. 检验审核状态
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        // 要更改的状态不能还是“待审核”，不然就是从 待审核 到 待审核
        if (Objects.equals(PictureReviewStatusEnum.REVIEWING, reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 数据库操作
        PictureDO updatePicture = new PictureDO();
        updatePicture.setId(pictureId)
                .setReviewerId(loginUser.getId())
                .setReviewStatus(reviewStatus)
                .setReviewMessage(reviewMessage)
                .setReviewTime(LocalDateTimeUtil.now());

        this.updateById(updatePicture);
    }


    @Override
    public void fillReviewParams(PictureDO pictureDO, UserDO loginUser) {
        // 如果是管理员则自动过审
        if (userService.isAdmin(loginUser)) {
            pictureDO.setReviewStatus(PictureReviewStatusEnum.PASS.getValue())
                    .setReviewerId(loginUser.getId())
                    .setReviewMessage("管理员自动过审")
                    .setReviewTime(LocalDateTimeUtil.now());
        } else {
            // 非管理员，无论是编辑还是创建，默认都是待审核
            pictureDO.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

}




