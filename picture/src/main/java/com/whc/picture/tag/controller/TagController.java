package com.whc.picture.tag.controller;

import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.tag.controller.vo.PictureTagCategory;
import com.whc.picture.tag.controller.vo.SaveTagVO;
import com.whc.picture.tag.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 标签管理
 */
@Slf4j
@RestController
@RequestMapping("/tag")
public class TagController {

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

}
