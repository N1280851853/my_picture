package com.whc.picture.space.controller;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whc.picture.annotation.AuthCheck;
import com.whc.picture.bean.PageVO;
import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.constant.UserConstant;
import com.whc.picture.entity.space.common.SpaceLevelEnum;
import com.whc.picture.entity.space.entity.SpaceDO;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.space.controller.qo.ListPageSpaceQO;
import com.whc.picture.space.controller.qo.SaveSpaceQO;
import com.whc.picture.space.controller.qo.SpaceIdQO;
import com.whc.picture.space.controller.qo.UpdateSpaceQO;
import com.whc.picture.space.controller.vo.ListPageSpaceVO;
import com.whc.picture.space.controller.vo.SpaceLevelVO;
import com.whc.picture.space.controller.vo.SpaceVO;
import com.whc.picture.space.service.SpaceService;
import com.whc.picture.user.controller.vo.LoginUserVO;
import com.whc.picture.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    @Resource
    private UserService userService;


    /**
     * 获取空间列表(分页)
     *
     * @param qo
     * @param request
     * @return
     */
    @PostMapping("/listPageSpaceVO")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageVO<ListPageSpaceVO>> listPageSpaceVO(@RequestBody ListPageSpaceQO qo, HttpServletRequest request) {
        Long id = qo.getId();
        Long userId = qo.getUserId();
        String spaceName = qo.getSpaceName();
        Integer spaceLevel = qo.getSpaceLevel();

        Page<SpaceDO> page = spaceService.lambdaQuery()
                .select(
                        SpaceDO::getId,
                        SpaceDO::getSpaceName,
                        SpaceDO::getSpaceLevel,
                        SpaceDO::getMaxSize,
                        SpaceDO::getMaxCount,
                        SpaceDO::getTotalSize,
                        SpaceDO::getTotalCount,
                        SpaceDO::getUserId,
                        SpaceDO::getGmtCreate,
                        SpaceDO::getGmtModified
                )
                .eq(id != null, SpaceDO::getId, id)
                .eq(userId != null, SpaceDO::getUserId, userId)
                .eq(spaceLevel != null, SpaceDO::getSpaceLevel, spaceLevel)
                .like(ObjectUtil.isNotEmpty(spaceName), SpaceDO::getSpaceName, spaceName)
                .page(qo.getPage());

        PageVO<ListPageSpaceVO> ret = new PageVO<>();
        List<ListPageSpaceVO> rtList = new ArrayList<>();
        List<SpaceDO> spaceDOS = page.getRecords();

        if (ObjectUtil.isNotEmpty(spaceDOS)) {
            List<Long> userIds = spaceDOS.stream().map(SpaceDO::getUserId).toList();
            Map<Long, UserDO> userDOMap = userService.lambdaQuery()
                    .select(
                            UserDO::getId,
                            UserDO::getUserAccount,
                            UserDO::getUserName,
                            UserDO::getUserAvatar,
                            UserDO::getUserProfile,
                            UserDO::getUserRole,
                            UserDO::getGmtCreate,
                            UserDO::getGmtModified
                    )
                    .in(UserDO::getId, userIds)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(UserDO::getId, t -> t));
            for (SpaceDO spaceDO : spaceDOS) {
                ListPageSpaceVO vo = new ListPageSpaceVO();
                Long spaceUserId = spaceDO.getUserId();
                vo.setId(spaceDO.getId())
                        .setSpaceName(spaceDO.getSpaceName())
                        .setSpaceLevel(spaceDO.getSpaceLevel())
                        .setMaxSize(spaceDO.getMaxSize())
                        .setMaxCount(spaceDO.getMaxCount())
                        .setTotalSize(spaceDO.getTotalSize())
                        .setTotalCount(spaceDO.getTotalCount())
                        .setUserId(spaceUserId)
                        .setGmtCreate(LocalDateTimeUtil.format(spaceDO.getGmtCreate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .setGmtModified(LocalDateTimeUtil.format(spaceDO.getGmtModified(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                UserDO userDO = userDOMap.get(spaceUserId);
                if (ObjectUtil.isNotEmpty(userDO)) {
                    LoginUserVO loginUserVO = new LoginUserVO();
                    loginUserVO.setId(spaceUserId)
                            .setUserAccount(userDO.getUserAccount())
                            .setUserName(userDO.getUserName())
                            .setUserAvatar(userDO.getUserAvatar())
                            .setUserProfile(userDO.getUserProfile())
                            .setUserRole(userDO.getUserRole())
                            .setGmtCreate(LocalDateTimeUtil.format(userDO.getGmtCreate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .setGmtModified(LocalDateTimeUtil.format(userDO.getGmtModified(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    vo.setUser(loginUserVO);
                }
                rtList.add(vo);
            }
        }
        ret.setList(rtList);

        return ResultUtils.success(ret);
    }

    /**
     * 获取空间详细信息
     *
     * @param qo
     * @param request
     * @return
     */
    @PostMapping("/getSpaceVOById")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<SpaceVO> getSpaceVOById(@RequestBody SpaceIdQO qo, HttpServletRequest request) {
        Long id = qo.getId();

        SpaceDO spaceDO = spaceService.lambdaQuery()
                .select(
                        SpaceDO::getId,
                        SpaceDO::getSpaceName,
                        SpaceDO::getSpaceLevel,
                        SpaceDO::getMaxSize,
                        SpaceDO::getMaxCount,
                        SpaceDO::getTotalSize,
                        SpaceDO::getTotalCount,
                        SpaceDO::getUserId,
                        SpaceDO::getGmtCreate,
                        SpaceDO::getGmtModified
                )
                .eq(id != null, SpaceDO::getId, id)
                .one();

        SpaceVO vo = new SpaceVO();

        if (ObjectUtil.isNotEmpty(spaceDO)) {
            Long userId = spaceDO.getUserId();
            UserDO userDO = userService.lambdaQuery()
                    .select(
                            UserDO::getId,
                            UserDO::getUserAccount,
                            UserDO::getUserName,
                            UserDO::getUserAvatar,
                            UserDO::getUserProfile,
                            UserDO::getUserRole,
                            UserDO::getGmtCreate,
                            UserDO::getGmtModified
                    )
                    .eq(UserDO::getId, userId)
                    .one();
            Long spaceUserId = spaceDO.getUserId();
            vo.setId(spaceDO.getId())
                    .setSpaceName(spaceDO.getSpaceName())
                    .setSpaceLevel(spaceDO.getSpaceLevel())
                    .setMaxSize(spaceDO.getMaxSize())
                    .setMaxCount(spaceDO.getMaxCount())
                    .setTotalSize(spaceDO.getTotalSize())
                    .setTotalCount(spaceDO.getTotalCount())
                    .setUserId(spaceUserId)
                    .setGmtCreate(LocalDateTimeUtil.format(spaceDO.getGmtCreate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setGmtModified(LocalDateTimeUtil.format(spaceDO.getGmtModified(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (ObjectUtil.isNotEmpty(userDO)) {
                LoginUserVO loginUserVO = new LoginUserVO();
                loginUserVO.setId(spaceUserId)
                        .setUserAccount(userDO.getUserAccount())
                        .setUserName(userDO.getUserName())
                        .setUserAvatar(userDO.getUserAvatar())
                        .setUserProfile(userDO.getUserProfile())
                        .setUserRole(userDO.getUserRole())
                        .setGmtCreate(LocalDateTimeUtil.format(userDO.getGmtCreate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .setGmtModified(LocalDateTimeUtil.format(userDO.getGmtModified(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                vo.setUser(loginUserVO);
            }
        }

        return ResultUtils.success(vo);

    }

    /**
     * 保存空间
     *
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
     *
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
     * 删除空间
     *
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
     *
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
