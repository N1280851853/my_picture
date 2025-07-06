package com.whc.picture.space.service;

import com.whc.picture.entity.space.entity.SpaceDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whc.picture.space.controller.qo.SaveSpaceQO;
import com.whc.picture.space.controller.qo.SpaceIdQO;
import com.whc.picture.space.controller.qo.UpdateSpaceQO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

/**
* @author 23161
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-06-30 20:38:04
*/
public interface SpaceService extends IService<SpaceDO> {

    /**
     * 创建空间
     * @param qo
     * @param request
     */
    Long saveSpace(SaveSpaceQO qo, HttpServletRequest request);

    /**
     * 更新空间
     * @param qo
     * @param request
     */
    void updateSpace(UpdateSpaceQO qo, HttpServletRequest request);

    /**
     * 根据空间id，删除空间
     * @param qo
     */
    void deleteSpaceById(SpaceIdQO qo, HttpServletRequest request);

    /**
     * 根据空间级别 填充空间对象
     * @param space
     */
    void fillSpaceBySpaceLevel(SpaceDO space);
}
