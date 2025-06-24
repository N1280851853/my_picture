package com.whc.picture.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * url 图片上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
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

    @Override
    protected String getOriginalFilename(Object inputSource) {
        return FileUtil.mainName((String) inputSource);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        // 下载文件到临时目录
        HttpUtil.downloadFile((String) inputSource, file);
    }
}
