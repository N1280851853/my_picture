package com.whc.picture.manager;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.config.CosClientConfig;
import com.whc.picture.entity.picture.common.PictureCommon;
import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.gateway.property.CosProperty;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author： whc
 * @create： 2025/6/11 20:20
 */
@Slf4j
@Service
public class FileManager {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosProperty cosProperty;



    final long ONE_MB = 1024 * 1024;

    final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");

    /**
     * 上传图片
     * @param multipartFile 文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public PictureDO uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validPicture(multipartFile);

        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        // 这里有个小细节就是说图片文件的原始名称可能是和url地址有冲突的，自己拼接文件上传名称，而不是原始文件名称，可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        // 上传路径  /项目名称/前缀/文件名称
        String uploadPath = String.format("/%s/%s/%s", cosProperty.getProject(), uploadPathPrefix, uploadFileName);

        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            // 将前端传过来的文件传输到本地临时文件中
            multipartFile.transferTo(file);
            // 上传文件
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片原始信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 计算宽高比
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight , 2).doubleValue();

            PictureDO pictureDO = new PictureDO();
            pictureDO.setUrl(cosProperty.getHost() + uploadPath)
                    .setName(FileUtil.mainName(originalFilename))
                    .setPicSize(FileUtil.size(file))
                    .setPicWidth(picWidth)
                    .setPicHeight(picHeight)
                    .setPicScale(picScale)
                    .setPicFormat(imageInfo.getFormat());

            return pictureDO;
        } catch (IOException e) {
            log.error("file upload error, filePath:{}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 临时文件清理
            deleteTmpFile(file);
        }


    }


    /**
     * 校验文件
     * @param multipartFile
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空 ");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超出2MB");
        // 2. 校验文件后缀
        String filename = multipartFile.getOriginalFilename();
        String fileSuffix = FileUtil.getSuffix(filename);
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");

    }


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
