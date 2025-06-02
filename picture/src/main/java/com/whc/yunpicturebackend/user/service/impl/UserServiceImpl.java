package com.whc.yunpicturebackend.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.User;
import com.whc.yunpicturebackend.user.mapper.UserMapper;
import com.whc.yunpicturebackend.user.service.UserService;
import org.springframework.stereotype.Service;

/**
* @author simceredx
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-28 20:12:17
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}




