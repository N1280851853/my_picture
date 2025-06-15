package com.whc.picture.tag.service;

import com.whc.picture.entity.tag.entity.TagDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author simceredx
* @description 针对表【tag(标签)】的数据库操作Service
* @createDate 2025-06-14 18:18:04
*/
public interface TagService extends IService<TagDO> {

    Map<String, Long> getAllTagMap();

    /**
     * 根据传入的标签ID，返回标签名称
     * @param tagIds
     * @return
     */
    Map<String, Long> getAllTagNameList(List<Long> tagIds);

    /**
     * 保存所有标签信息
     * @param tags
     */
    void insertTag(List<String> tags);
}
