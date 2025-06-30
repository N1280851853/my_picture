package com.whc.picture.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.gateway.property.CosProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author： whc
 * @create： 2025/6/11 20:20
 */
@Slf4j
@Service
@Deprecated
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
            // 获取到图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (ObjectUtil.isNotEmpty(objectList)) {
                // 因为目前只有一个规则因此只会有一个结果值
                CIObject compressedCiObject = objectList.get(0);
                // 封装压缩图的返回结果
                return buildResult(originalFilename, compressedCiObject);
            }

            return buildResult(imageInfo, uploadPath, originalFilename, file);
        } catch (IOException e) {
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
        pictureDO.setUrl(cosProperty.getHost() + uploadPath)
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
     * @return
     */
    private PictureDO buildResult(String originalFilename, CIObject compressedCiObject) {
        // 计算宽高比
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight , 2).doubleValue();

        PictureDO pictureDO = new PictureDO();
        pictureDO.setUrl(cosProperty.getHost() + compressedCiObject.getKey())
                .setName(FileUtil.mainName(originalFilename))
                .setPicSize(compressedCiObject.getSize().longValue())
                .setPicWidth(picWidth)
                .setPicHeight(picHeight)
                .setPicScale(picScale)
                .setPicFormat(compressedCiObject.getFormat());
        return pictureDO;
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

    // TODO 新增的方法

    /**
     * 通过url上次图片
     * @param fileUrl
     * @param uploadPathPrefix
     * @return
     */
    public PictureDO uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        // 校验图片
        //validPicture(multipartFile);
        validPicture(fileUrl);


        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.mainName(fileUrl);

        // 这里有个小细节就是说图片文件的原始名称可能是和url地址有冲突的，自己拼接文件上传名称，而不是原始文件名称，可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        // 上传路径  /项目名称/前缀/文件名称
        String uploadPath = String.format("/%s/%s/%s", cosProperty.getProject(), uploadPathPrefix, uploadFileName);

        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            // 先下载
            HttpUtil.downloadFile(fileUrl, file);
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
     * 根据 url 检验文件
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        // 1.检验非空
        ThrowUtils.throwIf(ObjectUtil.isEmpty(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");

        // 2.校验 URL 格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 3.校验 URL 的协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持 http 和 https 协议的文件地址");

        // 发送 HEAD 请求验证文件是否存在,这里需要注意释放资源，因此采用 try-with-resource的方式使其自动释放
        try (HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();) {
            // 未正常返回，无需执行其他判断
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                // 这里直接返回而不是抛异常，是因为有一些文件服务器是不支持head请求的，其返回值可能并不是 HttpStatus.HTTP_OK
                // 但这并不能表示文件不存在
                return;
            }
            // 4. 校验文件类型
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    ThrowUtils.throwIf(contentLength > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超出2MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }
    }

}
