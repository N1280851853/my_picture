package com.whc.picture.user;

import com.whc.picture.constant.UserRokeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserTest {

    @Test
    void contextLoads() {
        UserRokeEnum value = UserRokeEnum.getUserRokeEnum("");
        System.out.println(value);
    }

}
