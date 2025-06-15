package com.whc.picture.picture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.entity.picture.entity.PictureTagDO;
import com.whc.picture.picture.service.PictureTagService;
import com.whc.picture.picture.mapper.PictureTagMapper;
import com.whc.picture.tag.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @author simceredx
* @description 针对表【picture_tag(图片、标签关联关系表)】的数据库操作Service实现
* @createDate 2025-06-14 18:22:13
*/
@Service
public class PictureTagServiceImpl extends ServiceImpl<PictureTagMapper, PictureTagDO>
    implements PictureTagService{

    @Resource
    private TagService tagService;

    @Override
    public List<PictureTagDO> findPictureTagByPictureId(Long pictureId) {
        return this.lambdaQuery()
                .select(PictureTagDO::getPictureId, PictureTagDO::getTagId, PictureTagDO::getTagName)
                .eq(PictureTagDO::getPictureId, pictureId)
                .list();
    }

    @Override
    public void savePictureRelationTag(Long pictureId, String pictureName, List<String> tags) {
        Map<String, Long> allTagMap = tagService.getAllTagMap();
        List<PictureTagDO> insertDOS = new ArrayList<>();
        tags.forEach(tag -> {
            PictureTagDO entity = new PictureTagDO();
            entity.setPictureId(pictureId)
                    .setPictureName(pictureName)
                    .setTagId(allTagMap.get(tag))
                    .setTagName(tag);
            insertDOS.add(entity);
        });
        this.saveBatch(insertDOS);
    }

    @Override
    public void deletePictureRelationTag(Long pictureId) {
        List<PictureTagDO> pictureTagDOS = this.lambdaQuery()
                .select(PictureTagDO::getId)
                .eq(PictureTagDO::getPictureId, pictureId)
                .list();
        this.removeByIds(pictureTagDOS);
    }
}




