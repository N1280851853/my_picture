package com.whc.picture.file.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.server.HttpServerResponse;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.whc.picture.annotation.AuthCheck;
import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.constant.UserConstant;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 文件
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     * @param multipartFile
     * @return
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", fileName);

        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            // 将前端传过来的文件传输到本地临时文件中
            multipartFile.transferTo(file);
            // 上传文件
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("file upload error, filePath:{}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (file != null) {
                // 最后都要删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filePath:{}", filePath);
                }
            }
        }

    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            String fileName = "";
            if (filepath.contains("/")) {
                fileName = filepath.substring(filepath.lastIndexOf("/") + 1);
            } else {
                fileName = filepath;
            }
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }


}
