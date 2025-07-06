package com.whc.picture.space.controller;

import cn.hutool.core.util.ObjectUtil;
import com.whc.picture.annotation.AuthCheck;
import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.constant.UserConstant;
import com.whc.picture.constant.UserRoleEnum;
import com.whc.picture.entity.space.common.SpaceLevelEnum;
import com.whc.picture.entity.space.entity.SpaceDO;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.space.controller.qo.SaveSpaceQO;
import com.whc.picture.space.controller.qo.SpaceIdQO;
import com.whc.picture.space.controller.qo.UpdateSpaceQO;
import com.whc.picture.space.controller.vo.SpaceLevelVO;
import com.whc.picture.space.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 空间管理
 */
@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;


    /**
     * 保存空间
     * @param qo
     * @return
     */
    @PostMapping("/saveSpace")
    public BaseResponse<Long> saveSpace(@RequestBody @Validated SaveSpaceQO qo, HttpServletRequest request) {
        Long spaceId = spaceService.saveSpace(qo, request);
        return ResultUtils.success(spaceId);
    }


    /**
     * 更新空间
     * @param qo
     * @return
     */
    @PostMapping("/updateSpace")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Object> updateSpace(@RequestBody @Validated UpdateSpaceQO qo, HttpServletRequest request) {
        SpaceDO spaceDO = spaceService.lambdaQuery()
                .select(SpaceDO::getId)
                .eq(SpaceDO::getId, qo.getId())
                .one();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceDO), ErrorCode.NOT_FOUND_ERROR);
        spaceService.updateSpace(qo, request);

        return ResultUtils.success();
    }


    /**
     * 更新空间
     * @param qo
     * @return
     */
    @PostMapping("/deleteSpace")
    public BaseResponse<Object> deleteSpace(@RequestBody @Validated SpaceIdQO qo, HttpServletRequest request) {
        SpaceDO spaceDO = spaceService.lambdaQuery()
                .select(SpaceDO::getId)
                .eq(SpaceDO::getId, qo.getId())
                .one();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceDO), ErrorCode.NOT_FOUND_ERROR);
        spaceService.deleteSpaceById(qo, request);

        return ResultUtils.success();
    }

    /**
     * 获取所有的空间级别，便于前端展示
     * @return
     */
    @GetMapping("/listLevel")
    public BaseResponse<List<SpaceLevelVO>> listSpaceLevel() {
        List<SpaceLevelVO> vos = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(t -> new SpaceLevelVO(
                        t.getValue(),
                        t.getText(),
                        t.getMaxCount(),
                        t.getMaxSize()
                    )
                )
                .collect(Collectors.toList());
        return ResultUtils.success(vos);
    }

}
