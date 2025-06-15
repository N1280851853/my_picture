package com.whc.picture.tag.controller.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 保存标签的请求类
 */
@Data
@Accessors(chain = true)
public class SaveTagVO {

    @NotEmpty(message = "标签名称不能为null")
    private List<String> tagNames;

}
