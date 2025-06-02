package com.whc.picture.constant;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author： whc
 * @create： 2025/6/2 11:36
 */
@Getter
public enum UserRokeEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    public static final Map<String, UserRokeEnum> USER_ROKE_MAP;

    private final String text;
    private final String value;

    static{
        USER_ROKE_MAP = Arrays.stream(UserRokeEnum.values())
                .collect(Collectors.toMap(UserRokeEnum::getValue, Function.identity()));
    }

    UserRokeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }


    public static UserRokeEnum getUserRokeEnum(String value) {
        return USER_ROKE_MAP.get(value);
    }



}
