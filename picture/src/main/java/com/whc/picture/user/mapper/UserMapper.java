package com.whc.picture.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whc.picture.entity.user.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author simceredx
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-05-28 20:12:17
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}




