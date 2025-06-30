package com.whc.picture.picture.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.entity.picture.common.PictureReviewStatusEnum;
import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.manager.CosManager;
import com.whc.picture.manager.FileManager;
import com.whc.picture.manager.upload.FilePictureUpload;
import com.whc.picture.manager.upload.PictureUploadTemplate;
import com.whc.picture.manager.upload.UrlPictureUpload;
import com.whc.picture.picture.controller.qo.PictureQO;
import com.whc.picture.picture.controller.qo.PictureReviewQO;
import com.whc.picture.picture.controller.qo.PictureUpdateQO;
import com.whc.picture.picture.controller.qo.PictureUploadByBatchQO;
import com.whc.picture.picture.controller.vo.PictureVO;
import com.whc.picture.picture.service.PictureService;
import com.whc.picture.picture.mapper.PictureMapper;
import com.whc.picture.picture.service.PictureTagService;
import com.whc.picture.tag.service.TagService;
import com.whc.picture.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author simceredx
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-06-13 16:30:50
 */
@Service
@Transactional
@Slf4j
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

    @Resource
    private CosManager cosManager;


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

        String picName = pictureQO.getPicName();
        // 支持外层传递图片名称
        if (null != pictureQO && StrUtil.isNotEmpty(picName)) {
            pictureDO.setName(picName);
        }

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

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchQO qo, UserDO loginUser) {
        // 校验参数
        String searchText = qo.getSearchText();
        Integer count = qo.getCount();
        String namePrefix = qo.getNamePrefix();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "单词批量操作不能超过 30 条");
        // 名称前缀默认=搜索关键词
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }

        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止转义字符和对象存储冲突的问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureQO pictureUploadRequest = new PictureQO();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                PictureDO pictureDO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureDO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }


        return 0;
    }

    @Async
    @Override
    public void clearPictureFile(PictureDO oldPicture) {
        // 判断该图片是否被多条记录使用（秒传的情况下，这里我们并没有用到 秒传）
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(PictureDO::getUrl, pictureUrl)
                .count();
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        // FIXME 注意，这里的 url 包含了域名，实际上只要传 key 值（存储路径）就够了
        // 删除转换为 webp 后的图片
        cosManager.deleteObject(oldPicture.getUrl());
        // 清理缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }


}




