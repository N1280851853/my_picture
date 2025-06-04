package com.whc.picture.user;

import com.whc.picture.constant.UserRoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserTest {

    @Test
    void contextLoads() {
        UserRoleEnum value = UserRoleEnum.getUserRokeEnum("");
        System.out.println(value);
    }

}
