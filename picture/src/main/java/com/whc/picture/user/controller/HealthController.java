package com.whc.picture.user.controller;

import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author： whc
 */
@RestController
@RequestMapping("/")
public class HealthController {

    /**
     * 健康检测
     * @return
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
