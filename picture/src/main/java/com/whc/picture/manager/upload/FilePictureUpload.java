package com.whc.picture.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件上传
 */
@Service
public class FilePictureUpload  extends PictureUploadTemplate{
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空 ");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超出2MB");
        // 2. 校验文件后缀
        String filename = multipartFile.getOriginalFilename();
        String fileSuffix = FileUtil.getSuffix(filename);
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        return ((MultipartFile) inputSource).getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        ((MultipartFile) inputSource).transferTo(file);
    }
}
