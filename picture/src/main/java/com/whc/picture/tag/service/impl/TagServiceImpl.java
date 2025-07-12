package com.whc.picture.tag.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.entity.tag.entity.TagDO;
import com.whc.picture.tag.service.TagService;
import com.whc.picture.tag.mapper.TagMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author simceredx
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2025-06-14 18:18:04
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagDO>
    implements TagService{


    @Override
    public Map<String, Long> getAllTagMap() {
        Map<String, Long> map = new HashMap<>();
        List<TagDO> tagList = this.lambdaQuery()
                .list();
        if (ObjectUtil.isNotEmpty(tagList)) {
            map = tagList.stream().collect(Collectors.toMap(TagDO::getTagName, TagDO::getId));
        }
        return map;
    }

    @Override
    public Map<String, Long> getAllTagNameList(List<Long> tagIds) {
        return this.lambdaQuery()
                .select(TagDO::getTagName, TagDO::getId)
                .in(TagDO::getId, tagIds)
                .list()
                .stream().collect(Collectors.toMap(TagDO::getTagName, TagDO::getId));
    }

    @Override
    public void insertTag(List<String> tags) {
        if (ObjectUtil.isEmpty(tags)) {
            return;
        }
        Map<String, Long> allTagMap = getAllTagMap();

        List<TagDO> insertTags = new ArrayList<>();
        // 如果数据库中没有标签
        if (ObjectUtil.isEmpty(allTagMap)) {
            tags.forEach(t -> {
                if (ObjectUtil.isNotEmpty(t)) {
                    TagDO tagDO = new TagDO();
                    tagDO.setTagName(t);
                    insertTags.add(tagDO); // 全部新增
                }
            });
        } else {
            tags.forEach(t -> {
                if (ObjectUtil.isNotEmpty(t)) {
                    Long id = allTagMap.get(t);
                    if (id == null) { // 表示这个标签还没有入库
                        TagDO tagDO = new TagDO();
                        tagDO.setTagName(t);
                        insertTags.add(tagDO);
                    }
                }
            });
        }
        if (ObjectUtil.isNotEmpty(insertTags)) {
            this.saveBatch(insertTags);
        }
    }

}




