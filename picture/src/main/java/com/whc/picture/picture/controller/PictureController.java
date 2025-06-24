package com.whc.picture.picture.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whc.picture.annotation.AuthCheck;
import com.whc.picture.bean.PageVO;
import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.constant.UserConstant;
import com.whc.picture.entity.picture.common.PictureReviewStatusEnum;
import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.entity.picture.entity.PictureTagDO;
import com.whc.picture.entity.tag.entity.TagDO;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.picture.controller.qo.*;
import com.whc.picture.picture.controller.vo.ListPagePictureVO;
import com.whc.picture.picture.controller.vo.PictureVO;
import com.whc.picture.picture.service.PictureService;
import com.whc.picture.picture.service.PictureTagService;
import com.whc.picture.tag.service.TagService;
import com.whc.picture.user.controller.vo.LoginUserVO;
import com.whc.picture.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 图片管理
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private PictureTagService pictureTagService;

    @Resource
    private TagService tagService;

    /**
     * 上传图片(可重新上传)
     *
     * @param pictureQO
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureQO pictureQO, HttpServletRequest request) {
        UserDO userDO = userService.getLoginUser(request);
        LoginUserVO loginUser = userService.getUserVO(userDO);
        PictureDO pictureDO = pictureService.uploadPicture(multipartFile, pictureQO, userDO);
        PictureVO vo = new PictureVO();
        vo.setId(pictureDO.getId())
                .setUrl(pictureDO.getUrl())
                .setName(pictureDO.getName())
                .setIntroduction(pictureDO.getIntroduction())
                .setPicSize(pictureDO.getPicSize())
                .setPicWidth(pictureDO.getPicWidth())
                .setPicHeight(pictureDO.getPicHeight())
                .setPicScale(pictureDO.getPicScale())
                .setPicFormat(pictureDO.getPicFormat())
                .setUserId(loginUser.getId())
                .setUser(loginUser);

        return ResultUtils.success(vo);
    }

    /**
     * 通过url 上传图片(可重新上传)
     *
     * @param pictureQO
     * @param request
     * @return
     */
    @PostMapping("/uploadByUrl")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureQO pictureQO, HttpServletRequest request) {
        String fileUrl = pictureQO.getFileUrl();
        UserDO userDO = userService.getLoginUser(request);
        LoginUserVO loginUser = userService.getUserVO(userDO);
        PictureDO pictureDO = pictureService.uploadPicture(fileUrl, pictureQO, userDO);
        PictureVO vo = new PictureVO();
        vo.setId(pictureDO.getId())
                .setUrl(pictureDO.getUrl())
                .setName(pictureDO.getName())
                .setIntroduction(pictureDO.getIntroduction())
                .setPicSize(pictureDO.getPicSize())
                .setPicWidth(pictureDO.getPicWidth())
                .setPicHeight(pictureDO.getPicHeight())
                .setPicScale(pictureDO.getPicScale())
                .setPicFormat(pictureDO.getPicFormat())
                .setUserId(loginUser.getId())
                .setUser(loginUser);

        return ResultUtils.success(vo);

    }


    /**
     * 更新图片信息
     * @param qo
     * @param request
     * @return
     */
    @PostMapping("/updatePicture")
    public BaseResponse<Object> updatePicture(@RequestBody @Validated PictureUpdateQO qo, HttpServletRequest request) {

        UserDO loginUser = userService.getLoginUser(request);
        PictureDO oldPictureDO = pictureService.lambdaQuery()
                .select(PictureDO::getId, PictureDO::getUserId)
                .eq(PictureDO::getId, qo.getId())
                .one();
        // 仅本人和管理员可编辑图片
        if (!oldPictureDO.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        pictureService.updatePicture(qo, loginUser);

        return ResultUtils.success();
    }

    /**
     * 根据ID获取图片（仅管理员可用）
     *
     * @param qo
     * @param request
     * @return
     */
    @PostMapping("/getPictureById")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureDO> getPictureById(@RequestBody @Validated PictureIdQO qo, HttpServletRequest request) {
        PictureDO pictureDO = pictureService.lambdaQuery()
                .eq(PictureDO::getId, qo.getId())
                .one();

        return ResultUtils.success(pictureDO);
    }

    /**
     * 根据ID获取图片（用户使用）
     *
     * @param qo
     * @param request
     * @return
     */
    @PostMapping("/getPictureVOById")
    public BaseResponse<PictureVO> getPictureVOById(@RequestBody @Validated PictureIdQO qo, HttpServletRequest request) {
        Long pictureId = qo.getId();
        PictureDO pictureDO = pictureService.lambdaQuery()
                .eq(PictureDO::getId, pictureId)
                // 防止普通用户直接根据id获取到未审核通过的图片，这里加一个条件
                .eq(PictureDO::getReviewStatus, PictureReviewStatusEnum.PASS.getValue())
                .one();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureDO), ErrorCode.PARAMS_ERROR);

        Long userId = pictureDO.getUserId();
        PictureVO vo = new PictureVO();
        vo.setId(pictureDO.getId())
                .setUrl(pictureDO.getUrl())
                .setName(pictureDO.getName())
                .setIntroduction(pictureDO.getIntroduction())
                .setCategory(pictureDO.getCategory())
                .setPicSize(pictureDO.getPicSize())
                .setPicWidth(pictureDO.getPicWidth())
                .setPicHeight(pictureDO.getPicHeight())
                .setPicScale(pictureDO.getPicScale())
                .setPicFormat(pictureDO.getPicFormat())
                .setUserId(userId)
                .setGmtCreate(pictureDO.getGmtCreate().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                .setGmtModified(pictureDO.getGmtModified().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));

        List<PictureTagDO> pictureTagByPictureId = pictureTagService.findPictureTagByPictureId(pictureId);
        if (ObjectUtil.isNotEmpty(pictureTagByPictureId)) {
            List<Long> tagIdList = pictureTagByPictureId.stream().map(PictureTagDO::getTagId).toList();
            Map<String, Long> allTagNameList = tagService.getAllTagNameList(tagIdList);
            List<String> tagNameList = new ArrayList<>(allTagNameList.keySet());
            vo.setTags(tagNameList);
        }

        UserDO userDO = userService.getById(userId);
        vo.setUser(userService.getUserVO(userDO));

        return ResultUtils.success(vo);
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/listPagePicture")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageVO<ListPagePictureVO>> listPagePicture(@RequestBody ListPagePictureQO qo) {
        Long id = qo.getId();
        String name = qo.getName();
        String introduction = qo.getIntroduction();
        String category = qo.getCategory();
        Long picSize = qo.getPicSize();
        Integer picWidth = qo.getPicWidth();
        Integer picHeight = qo.getPicHeight();
        Double picScale = qo.getPicScale();
        String picFormat = qo.getPicFormat();
        String searchText = qo.getSearchText();
        Long userId = qo.getUserId();
        Integer reviewStatus = qo.getReviewStatus();
        Long reviewerId = qo.getReviewerId();
        LocalDateTime startUploadTime = qo.getStartUploadTime();
        LocalDateTime endUploadTime = qo.getEndUploadTime();

        PageVO<ListPagePictureVO> pageVO = new PageVO<>();

        List<String> tags = qo.getTags();
        Set<Long> pictureIds = new HashSet<>();
        if (ObjectUtil.isNotEmpty(tags)) {
            List<TagDO> list = tagService.lambdaQuery()
                    .select(TagDO::getId)
                    .in(TagDO::getTagName, tags)
                    .list();
            if (ObjectUtil.isEmpty(list)) {
                // 输入的标签在数据库中没有查询到，则直接返回
                return ResultUtils.success(pageVO);
            }
            Set<Long> tagIds = list.stream().map(TagDO::getId).collect(Collectors.toSet());
            List<PictureTagDO> pictureTagDOS = pictureTagService.lambdaQuery()
                    .select(PictureTagDO::getPictureId)
                    .in(PictureTagDO::getTagId, tagIds)
                    .list();
            if (ObjectUtil.isEmpty(pictureTagDOS)) {
                // 输入的标签没有关联任何图片，则直接返回
                return ResultUtils.success(pageVO);
            }
            pictureIds = pictureTagDOS.stream().map(PictureTagDO::getPictureId).collect(Collectors.toSet());
        }

        Page<PictureDO> page = pictureService.lambdaQuery()
                .select(
                        PictureDO::getId,
                        PictureDO::getUrl,
                        PictureDO::getName,
                        PictureDO::getIntroduction,
                        PictureDO::getCategory,
                        PictureDO::getPicSize,
                        PictureDO::getPicWidth,
                        PictureDO::getPicHeight,
                        PictureDO::getPicScale,
                        PictureDO::getPicFormat,
                        PictureDO::getUserId,
                        PictureDO::getReviewStatus,
                        PictureDO::getReviewerId,
                        PictureDO::getReviewMessage,
                        PictureDO::getReviewTime,
                        PictureDO::getGmtCreate,
                        PictureDO::getGmtModified
                )
                .eq(ObjectUtil.isNotEmpty(id), PictureDO::getId, id)
                .eq(ObjectUtil.isNotEmpty(userId), PictureDO::getUserId, userId)
                .eq(ObjectUtil.isNotEmpty(category), PictureDO::getCategory, category)
                .eq(ObjectUtil.isNotEmpty(picWidth), PictureDO::getPicWidth, picWidth)
                .eq(ObjectUtil.isNotEmpty(picHeight), PictureDO::getPicHeight, picHeight)
                .eq(ObjectUtil.isNotEmpty(picSize), PictureDO::getPicSize, picSize)
                .eq(ObjectUtil.isNotEmpty(picScale), PictureDO::getPicScale, picScale)
                .eq(ObjectUtil.isNotEmpty(reviewStatus), PictureDO::getReviewStatus, reviewStatus)
                .eq(ObjectUtil.isNotEmpty(reviewerId), PictureDO::getReviewerId, reviewerId)
                .in(ObjectUtil.isNotEmpty(pictureIds), PictureDO::getId, pictureIds)
                .like(ObjectUtil.isNotEmpty(name), PictureDO::getName, name)
                .like(ObjectUtil.isNotEmpty(introduction), PictureDO::getIntroduction, introduction)
                .like(ObjectUtil.isNotEmpty(picFormat), PictureDO::getPicFormat, picFormat)
                .and(ObjectUtil.isNotEmpty(searchText), t -> {
                    t.like(PictureDO::getName, searchText)
                            .or()
                            .like(PictureDO::getIntroduction, introduction);
                })
                .ge(ObjectUtil.isNotEmpty(startUploadTime), PictureDO::getGmtCreate, startUploadTime)
                .le(ObjectUtil.isNotEmpty(endUploadTime), PictureDO::getGmtCreate, endUploadTime)
                .orderByDesc(PictureDO::getGmtModified)
                .page(qo.getPage());

        pageVO.setTotalRow(page.getTotal());

        List<ListPagePictureVO> rtList = new ArrayList<>();
        List<PictureDO> pictureList = page.getRecords();
        if (ObjectUtil.isEmpty(pictureList)) {
            return ResultUtils.success(pageVO);
        }

        List<Long> pictureIdList = pictureList.stream().map(PictureDO::getId).toList();
        Map<Long, List<PictureTagDO>> pictureTagMap = pictureTagService.lambdaQuery()
                .select(PictureTagDO::getPictureId, PictureTagDO::getTagName)
                .in(PictureTagDO::getPictureId, pictureIdList)
                .list()
                .stream()
                .collect(Collectors.groupingBy(PictureTagDO::getPictureId));

        pictureList.forEach(t -> {
            List<PictureTagDO> pictureTagDOS = pictureTagMap.get(t.getId());
            pictureTagDOS = Optional.ofNullable(pictureTagDOS).orElse(new ArrayList<>());
            List<String> tagNameList = pictureTagDOS.stream().map(PictureTagDO::getTagName).toList();
            ListPagePictureVO vo = new ListPagePictureVO();
            vo.setId(t.getId())
                    .setUrl(t.getUrl())
                    .setName(t.getName())
                    .setIntroduction(t.getIntroduction())
                    .setTags(JSONUtil.toJsonStr(tagNameList))
                    .setCategory(t.getCategory())
                    .setPicSize(t.getPicSize())
                    .setPicWidth(t.getPicWidth())
                    .setPicHeight(t.getPicHeight())
                    .setPicScale(t.getPicScale())
                    .setPicFormat(t.getPicFormat())
                    .setUserId(t.getUserId())
                    .setReviewStatus(t.getReviewStatus())
                    .setReviewMessage(t.getReviewMessage())
                    .setReviewerId(t.getReviewerId())
                    .setReviewTime(LocalDateTimeUtil.format(t.getReviewTime(), DatePattern.NORM_DATETIME_PATTERN))
                    .setGmtCreate(t.getGmtCreate().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                    .setGmtModified(t.getGmtModified().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
            rtList.add(vo);

        });

        pageVO.setList(rtList);

        return ResultUtils.success(pageVO);
    }

    /**
     * 分页获取图片列表（仅普通用户可用）
     */
    @PostMapping("/listPagePictureVO")
    public BaseResponse<PageVO<PictureVO>> listPagePictureVO(@RequestBody ListPagePictureQO qo) {
        Long id = qo.getId();
        String name = qo.getName();
        String introduction = qo.getIntroduction();
        String category = qo.getCategory();
        Long picSize = qo.getPicSize();
        Integer picWidth = qo.getPicWidth();
        Integer picHeight = qo.getPicHeight();
        Double picScale = qo.getPicScale();
        String picFormat = qo.getPicFormat();
        String searchText = qo.getSearchText();
        Long userId = qo.getUserId();
        Integer reviewStatus = qo.getReviewStatus();
        // 普通用户只能看到审核通过的数据
        reviewStatus = PictureReviewStatusEnum.PASS.getValue();
        Long reviewerId = qo.getReviewerId();
        String reviewMessage = qo.getReviewMessage();
        Date reviewTime = qo.getReviewTime();
        LocalDateTime startUploadTime = qo.getStartUploadTime();
        LocalDateTime endUploadTime = qo.getEndUploadTime();

        PageVO<PictureVO> pageVO = new PageVO<>();

        List<String> tags = qo.getTags();
        Set<Long> pictureIds = new HashSet<>();
        if (ObjectUtil.isNotEmpty(tags)) {
            List<TagDO> list = tagService.lambdaQuery()
                    .select(TagDO::getId)
                    .in(TagDO::getTagName, tags)
                    .list();
            if (ObjectUtil.isEmpty(list)) {
                // 输入的标签在数据库中没有查询到，则直接返回
                return ResultUtils.success(pageVO);
            }
            Set<Long> tagIds = list.stream().map(TagDO::getId).collect(Collectors.toSet());
            List<PictureTagDO> pictureTagDOS = pictureTagService.lambdaQuery()
                    .select(PictureTagDO::getPictureId)
                    .in(PictureTagDO::getTagId, tagIds)
                    .list();
            if (ObjectUtil.isEmpty(pictureTagDOS)) {
                // 输入的标签没有关联任何图片，则直接返回
                return ResultUtils.success(pageVO);
            }
            pictureIds = pictureTagDOS.stream().map(PictureTagDO::getPictureId).collect(Collectors.toSet());
        }

        Page<PictureDO> page = pictureService.lambdaQuery()
                .select(
                        PictureDO::getId,
                        PictureDO::getUrl,
                        PictureDO::getName,
                        PictureDO::getIntroduction,
                        PictureDO::getCategory,
                        PictureDO::getPicSize,
                        PictureDO::getPicWidth,
                        PictureDO::getPicHeight,
                        PictureDO::getPicScale,
                        PictureDO::getPicFormat,
                        PictureDO::getUserId,
                        PictureDO::getReviewStatus,
                        PictureDO::getReviewerId,
                        PictureDO::getReviewMessage,
                        PictureDO::getReviewTime,
                        PictureDO::getGmtCreate,
                        PictureDO::getGmtModified
                )
                .eq(ObjectUtil.isNotEmpty(id), PictureDO::getId, id)
                .eq(ObjectUtil.isNotEmpty(userId), PictureDO::getUserId, userId)
                .eq(ObjectUtil.isNotEmpty(category), PictureDO::getCategory, category)
                .eq(ObjectUtil.isNotEmpty(picWidth), PictureDO::getPicWidth, picWidth)
                .eq(ObjectUtil.isNotEmpty(picHeight), PictureDO::getPicHeight, picHeight)
                .eq(ObjectUtil.isNotEmpty(picSize), PictureDO::getPicSize, picSize)
                .eq(ObjectUtil.isNotEmpty(picScale), PictureDO::getPicScale, picScale)
                .eq(ObjectUtil.isNotEmpty(reviewStatus), PictureDO::getReviewStatus, reviewStatus)
                .eq(ObjectUtil.isNotEmpty(reviewerId), PictureDO::getReviewerId, reviewerId)
                .in(ObjectUtil.isNotEmpty(pictureIds), PictureDO::getId, pictureIds)
                .like(ObjectUtil.isNotEmpty(name), PictureDO::getName, name)
                .like(ObjectUtil.isNotEmpty(introduction), PictureDO::getIntroduction, introduction)
                .like(ObjectUtil.isNotEmpty(picFormat), PictureDO::getPicFormat, picFormat)
                .and(ObjectUtil.isNotEmpty(searchText), t -> {
                    t.like(PictureDO::getName, searchText)
                            .or()
                            .like(PictureDO::getIntroduction, introduction);
                })
                .ge(ObjectUtil.isNotEmpty(startUploadTime), PictureDO::getGmtCreate, startUploadTime)
                .le(ObjectUtil.isNotEmpty(endUploadTime), PictureDO::getGmtCreate, endUploadTime)
                .orderByDesc(PictureDO::getGmtModified)
                .page(qo.getPage());

        pageVO.setTotalRow(page.getTotal());

        List<PictureVO> rtList = new ArrayList<>();
        List<PictureDO> pictureList = page.getRecords();
        if (ObjectUtil.isNotEmpty(pictureList)) {
            // 获取图片标签信息
            List<Long> pictureIdList = pictureList.stream().map(PictureDO::getId).toList();
            Map<Long, List<PictureTagDO>> pictureTagMap = pictureTagService.lambdaQuery()
                    .select(PictureTagDO::getPictureId, PictureTagDO::getTagName)
                    .in(PictureTagDO::getPictureId, pictureIdList)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(PictureTagDO::getPictureId));
            // 获取用户信息
            Set<Long> userIdSet = pictureList.stream().map(PictureDO::getUserId).collect(Collectors.toSet());
            Map<Long, UserDO> userMap = userService.listByIds(userIdSet).stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));

            pictureList.forEach(t -> {
                List<PictureTagDO> pictureTagDOS = pictureTagMap.get(t.getId());
                pictureTagDOS = Optional.ofNullable(pictureTagDOS).orElse(new ArrayList<>());
                List<String> tagNameList = pictureTagDOS.stream().map(PictureTagDO::getTagName).toList();
                PictureVO vo = new PictureVO();
                vo.setId(t.getId())
                        .setUrl(t.getUrl())
                        .setName(t.getName())
                        .setIntroduction(t.getIntroduction())
                        .setTags(tagNameList)
                        .setCategory(t.getCategory())
                        .setPicSize(t.getPicSize())
                        .setPicWidth(t.getPicWidth())
                        .setPicHeight(t.getPicHeight())
                        .setPicScale(t.getPicScale())
                        .setPicFormat(t.getPicFormat())
                        .setUserId(t.getUserId())
                        .setUser(userService.getUserVO(userMap.get(t.getUserId())))
                        .setGmtCreate(t.getGmtCreate().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                        .setGmtModified(t.getGmtModified().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
                rtList.add(vo);

            });
        }

        pageVO.setList(rtList);

        return ResultUtils.success(pageVO);
    }

    /**
     * 根据图片id，删除图片
     *
     * @param qo
     * @return
     */
    @PostMapping("/deletePicture")
    public BaseResponse<Object> deletePicture(@RequestBody @Validated DeletePictureQO qo, HttpServletRequest request) {
        Long id = qo.getId();

        // 用户或者管理员可以删除自己的图片，所以要判断要删除的图片是否属于自己
        UserDO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        PictureDO oldPicture = pictureService.lambdaQuery()
                .select(PictureDO::getId, PictureDO::getUserId)
                .eq(PictureDO::getId, id)
                .one();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldPicture), ErrorCode.NOT_FOUND_ERROR);

        // 仅本人和超级管理员可删除
        if (!oldPicture.getUserId().equals(userId) || !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 操作数据库
        pictureService.removeById(id);

        return ResultUtils.success();
    }

    /**
     * 图片审核
     * @param qo
     * @return
     */
    @PostMapping("/reviewPicture")
    public BaseResponse<Object> reviewPicture(@RequestBody PictureReviewQO qo,HttpServletRequest request) {
        PictureDO pictureDO = pictureService.lambdaQuery()
                .select(PictureDO::getId)
                .eq(PictureDO::getId, qo.getId())
                .one();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureDO), ErrorCode.NOT_FOUND_ERROR);

        UserDO loginUser = userService.getLoginUser(request);

        pictureService.doPictureReview(qo, loginUser);

        return ResultUtils.success();
    }

}
