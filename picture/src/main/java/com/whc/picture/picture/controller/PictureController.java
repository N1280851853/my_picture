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
import com.whc.picture.entity.space.entity.SpaceDO;
import com.whc.picture.entity.tag.entity.TagDO;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.manager.pagecache.LocalCache;
import com.whc.picture.manager.pagecache.PagePictureCacheTemplate;
import com.whc.picture.manager.pagecache.RedisCache;
import com.whc.picture.picture.controller.qo.*;
import com.whc.picture.picture.controller.vo.ListPagePictureVO;
import com.whc.picture.picture.controller.vo.PictureVO;
import com.whc.picture.picture.service.PictureService;
import com.whc.picture.picture.service.PictureTagService;
import com.whc.picture.space.service.SpaceService;
import com.whc.picture.tag.service.TagService;
import com.whc.picture.user.controller.vo.LoginUserVO;
import com.whc.picture.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
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

    @Resource
    private LocalCache localCache;

    @Resource
    private RedisCache redisCache;

    @Value("${spring.profiles.active}")
    private String env;

    @Resource
    private SpaceService spaceService;

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
                .setUser(loginUser)
                .setSpaceId(pictureDO.getSpaceId());

        return ResultUtils.success(vo);

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

        // 校验空间权限
        Long spaceId = pictureDO.getSpaceId();
        if (spaceId != null) {
            UserDO loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, pictureDO);
        }

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
    public BaseResponse<PageVO<PictureVO>> listPagePictureVO(@RequestBody ListPagePictureQO qo, HttpServletRequest request) {
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
        Long spaceId = qo.getSpaceId();
        Boolean isNullSpaceId = qo.getNullSpaceId();
        Integer reviewStatus = qo.getReviewStatus();
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

        if (spaceId == null) {
            // 公开图库
            isNullSpaceId = true;
            // 普通用户只能看到审核通过的数据
            reviewStatus = PictureReviewStatusEnum.PASS.getValue();
        } else {
            // 私有空间
            isNullSpaceId = false;
            UserDO loginUser = userService.getLoginUser(request);
            SpaceDO spaceDO = spaceService.lambdaQuery()
                    .select(SpaceDO::getId, SpaceDO::getUserId)
                    .eq(SpaceDO::getId, spaceId)
                    .one();
            ThrowUtils.throwIf(null == spaceDO, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(spaceDO.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }

        Page<PictureDO> page = pictureService.lambdaQuery()
                .select(
                        PictureDO::getId,
                        PictureDO::getUrl,
                        PictureDO::getThumbnailUrl,
                        PictureDO::getName,
                        PictureDO::getIntroduction,
                        PictureDO::getCategory,
                        PictureDO::getPicSize,
                        PictureDO::getPicWidth,
                        PictureDO::getPicHeight,
                        PictureDO::getPicScale,
                        PictureDO::getPicFormat,
                        PictureDO::getUserId,
                        PictureDO::getSpaceId,
                        PictureDO::getReviewStatus,
                        PictureDO::getReviewerId,
                        PictureDO::getReviewMessage,
                        PictureDO::getReviewTime,
                        PictureDO::getGmtCreate,
                        PictureDO::getGmtModified
                )
                .eq(ObjectUtil.isNotEmpty(id), PictureDO::getId, id)
                .eq(ObjectUtil.isNotEmpty(userId), PictureDO::getUserId, userId)
                .eq(ObjectUtil.isNotEmpty(spaceId), PictureDO::getSpaceId, spaceId)
                .isNull(isNullSpaceId, PictureDO::getSpaceId)
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
                        .setThumbnailUrl(t.getThumbnailUrl())
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
                        .setSpaceId(t.getSpaceId())
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
     * 分页获取图片列表-有缓存（仅普通用户可用）
     */
    @Deprecated
    @PostMapping("/listPagePictureVOByCache")
    public BaseResponse<PageVO<PictureVO>> listPagePictureVOByCache(@RequestBody ListPagePictureQO qo, HttpServletRequest request) {
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
        Long spaceId = qo.getSpaceId();
        Boolean isNullSpaceId = qo.getNullSpaceId();
        Integer reviewStatus = qo.getReviewStatus();
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

        if (spaceId == null) {
            // 公开图库
            isNullSpaceId = true;
            // 普通用户只能看到审核通过的数据
            reviewStatus = PictureReviewStatusEnum.PASS.getValue();
        } else {
            // 私有空间
            UserDO loginUser = userService.getLoginUser(request);
            SpaceDO spaceDO = spaceService.lambdaQuery()
                    .select(SpaceDO::getId)
                    .eq(SpaceDO::getId, spaceId)
                    .one();
            ThrowUtils.throwIf(null == spaceDO, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(spaceDO.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }

        // 查询缓存、缓存里面没有再去查询数据库
        // 构建缓存的key
        String queryCondition = JSONUtil.toJsonStr(qo);
        // 将查询条件进行MD5加密，然后截取一段较短的字符当key
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes()).substring(0, 16);
        String cacheKey = "listPictureVOByPage:" + hashKey;

        PagePictureCacheTemplate pagePictureCacheTemplate = localCache;
        if (!"local".equals(env)) {
            pagePictureCacheTemplate = redisCache;
        }

        String cachedValue = pagePictureCacheTemplate.getCacheValueByKey(cacheKey);

        if (ObjectUtil.isNotEmpty(cachedValue)) {
            // 如果缓存命中，返回结果
            PageVO<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, PageVO.class);
            return ResultUtils.success(cachedPage);
        }

        Page<PictureDO> page = pictureService.lambdaQuery()
                .select(
                        PictureDO::getId,
                        PictureDO::getUrl,
                        PictureDO::getThumbnailUrl,
                        PictureDO::getName,
                        PictureDO::getIntroduction,
                        PictureDO::getCategory,
                        PictureDO::getPicSize,
                        PictureDO::getPicWidth,
                        PictureDO::getPicHeight,
                        PictureDO::getPicScale,
                        PictureDO::getPicFormat,
                        PictureDO::getUserId,
                        PictureDO::getSpaceId,
                        PictureDO::getReviewStatus,
                        PictureDO::getReviewerId,
                        PictureDO::getReviewMessage,
                        PictureDO::getReviewTime,
                        PictureDO::getGmtCreate,
                        PictureDO::getGmtModified
                )
                .eq(ObjectUtil.isNotEmpty(id), PictureDO::getId, id)
                .eq(ObjectUtil.isNotEmpty(userId), PictureDO::getUserId, userId)
                .eq(ObjectUtil.isNotEmpty(spaceId), PictureDO::getSpaceId, spaceId)
                .isNull(isNullSpaceId, PictureDO::getSpaceId)
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
                        .setThumbnailUrl(t.getThumbnailUrl())
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
                        .setSpaceId(t.getSpaceId())
                        .setUser(userService.getUserVO(userMap.get(t.getUserId())))
                        .setGmtCreate(t.getGmtCreate().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                        .setGmtModified(t.getGmtModified().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
                rtList.add(vo);

            });
        }

        pageVO.setList(rtList);


        // 存入redis缓存
        String cacheValue = JSONUtil.toJsonStr(pageVO);

        pagePictureCacheTemplate.setCache(cacheKey, cacheValue);


        return ResultUtils.success(pageVO);
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
        PictureDO oldPicture = pictureService.lambdaQuery()
                .select(
                        PictureDO::getId,
                        PictureDO::getUserId,
                        PictureDO::getSpaceId
                )
                .eq(PictureDO::getId, qo.getId())
                .one();
        // 仅本人和管理员可编辑图片
        pictureService.checkPictureAuth(loginUser, oldPicture);

        pictureService.updatePicture(qo, loginUser);

        return ResultUtils.success();
    }

    /**
     * 根据图片id，删除图片
     *
     * @param qo
     * @return
     */
    @PostMapping("/deletePicture")
    public BaseResponse<Object> deletePicture(@RequestBody @Validated DeletePictureQO qo, HttpServletRequest request) {
        Long pictureId = qo.getId();

        // 用户或者管理员可以删除自己的图片，所以要判断要删除的图片是否属于自己
        UserDO loginUser = userService.getLoginUser(request);

        PictureDO oldPicture = pictureService.lambdaQuery()
                .select(
                        PictureDO::getId,
                        PictureDO::getUserId,
                        PictureDO::getUrl,
                        PictureDO::getPicSize,
                        PictureDO::getThumbnailUrl,
                        PictureDO::getSpaceId
                    )
                .eq(PictureDO::getId, pictureId)
                .one();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldPicture), ErrorCode.NOT_FOUND_ERROR);

        // 校验图片空间权限
        pictureService.checkPictureAuth(loginUser, oldPicture);

        pictureService.deletePictureById(oldPicture);

        // 删除对象存储中的文件
        pictureService.clearPictureFile(oldPicture);

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

    /**
     * 批量拉取图片
     * @param qo
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchQO qo, HttpServletRequest request) {
        ThrowUtils.throwIf(qo == null, ErrorCode.PARAMS_ERROR);
        UserDO loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatch(qo, loginUser);
        return ResultUtils.success(uploadCount);
    }

}
