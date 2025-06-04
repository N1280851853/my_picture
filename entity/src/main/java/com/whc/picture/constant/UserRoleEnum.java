package com.whc.picture.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author： whc
 * @create： 2025/6/2 11:36
 */
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    public static final Map<String, UserRoleEnum> USER_ROKE_MAP;

    private final String text;
    private final String value;

    static{
        USER_ROKE_MAP = Arrays.stream(UserRoleEnum.values())
                .collect(Collectors.toMap(UserRoleEnum::getValue, Function.identity()));
    }

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }


    public static UserRoleEnum getUserRokeEnum(String value) {
        return USER_ROKE_MAP.get(value);
    }

}
