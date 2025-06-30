package com.whc.picture.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.gateway.property.CosProperty;
import com.whc.picture.manager.CosManager;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosProperty cosProperty;



    final long ONE_MB = 1024 * 1024;

    final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");

    /**
     * 上传图片
     * @param inputSource 文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public PictureDO uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validPicture(inputSource);

        // 2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        // 这里有个小细节就是说图片文件的原始名称可能是和url地址有冲突的，自己拼接文件上传名称，而不是原始文件名称，可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        // 上传路径  /项目名称/前缀/文件名称
        String uploadPath = String.format("/%s/%s/%s", cosProperty.getProject(), uploadPathPrefix, uploadFileName);

        File file = null;
        try {
            // 3. 获取临时文件，获取文件到服务器
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源
            processFile(inputSource, file);
            // 4. 上传文件到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片原始信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 获取到图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (ObjectUtil.isNotEmpty(objectList)) {
                // 获取压缩之后得到的文件信息
                CIObject compressedCiObject = objectList.get(0);
                // 缩略图默认等于压缩图
                CIObject thumbnailCiObject = compressedCiObject;
                // 有生成缩略图，才获取缩略图
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                // 封装压缩图的返回结果
                return buildResult(originalFilename, compressedCiObject, thumbnailCiObject);
            }

            return buildResult(imageInfo, uploadPath, originalFilename, file);
        } catch (Exception e) {
            log.error("file upload error, filePath:{}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 临时文件清理
            deleteTmpFile(file);
        }

    }

    /**
     * 封装返回结果
     * @param imageInfo
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @return
     */
    private PictureDO buildResult(ImageInfo imageInfo, String uploadPath, String originalFilename, File file) {
        // 计算宽高比
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight , 2).doubleValue();

        PictureDO pictureDO = new PictureDO();
        pictureDO.setUrl(cosProperty.getHost() + "/" + uploadPath)
                .setName(FileUtil.mainName(originalFilename))
                .setPicSize(FileUtil.size(file))
                .setPicWidth(picWidth)
                .setPicHeight(picHeight)
                .setPicScale(picScale)
                .setPicFormat(imageInfo.getFormat());
        return pictureDO;
    }

    /**
     * 封装返回结果
     * @param originalFilename 原始文件名称
     * @param compressedCiObject 压缩后的对象
     * @param thumbnailCiObject 缩略图的对象
     * @return
     */
    private PictureDO buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject) {
        // 计算宽高比
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight , 2).doubleValue();

        PictureDO pictureDO = new PictureDO();
        pictureDO.setUrl(cosProperty.getHost() + "/" + compressedCiObject.getKey())        // 压缩后的原图
                .setThumbnailUrl(cosProperty.getHost() + "/" + thumbnailCiObject.getKey()) // 缩略图地址
                .setName(FileUtil.mainName(originalFilename))
                .setPicSize(compressedCiObject.getSize().longValue())
                .setPicWidth(picWidth)
                .setPicHeight(picHeight)
                .setPicScale(picScale)
                .setPicFormat(compressedCiObject.getFormat());
        return pictureDO;
    }



    /**
     * 检验文件输入源（本地文件 或 URL）
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     * @param inputSource
     * @param file
     * @throws Exception
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 清理临时文件
     * @param file
     */
    public static void deleteTmpFile(File file) {
        if (file != null) {
            // 最后都要删除临时文件
            boolean deleteRes = file.delete();
            if (!deleteRes) {
                log.error("file delete error, filePath:{}", file.getAbsolutePath());
            }
        }
    }

}
