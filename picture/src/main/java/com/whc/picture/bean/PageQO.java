package com.whc.picture.bean;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class PageQO {
    /**
     * 当前页码
     */
    @NotNull(message = "当前页码不能为空")
    private Long pageNum;

    /**
     * 每页元素个数
     */
    @NotNull(message = "查询长度不能为空")
    private Long pageSize;

    public <T> Page<T> getPage() {
        return new Page<>(pageNum, pageSize);
    }
}
