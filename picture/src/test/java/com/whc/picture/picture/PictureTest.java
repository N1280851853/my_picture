package com.whc.picture.picture;

import com.whc.picture.entity.picture.entity.PictureDO;
import com.whc.picture.picture.service.PictureService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author： whc
 * @create： 2025/6/15 10:02
 */
@SpringBootTest
public class PictureTest {

    @Resource
    private PictureService pictureService;

    @Test
    public void tagTest() {
        List<PictureDO> list = pictureService.lambdaQuery()
                .isNull(PictureDO::getSpaceId)
                .list();
        System.out.println("aaa");
    }

    class TagName {
        List<String> tagNames;
    }
}
