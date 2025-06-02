package com.whc.yunpicturebackend.controller;

import com.whc.yunpicturebackend.common.BaseResponse;
import com.whc.yunpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author： whc
 */
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检测
     * @return
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
