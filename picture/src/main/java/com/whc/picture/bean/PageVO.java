package com.whc.picture.bean;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页返回的列表数据
 */
@Data
public class PageVO<T> {

    /**
     * 总条数
     */
    private Long totalRow;
    /**
     * 列表
     */
    private List<T> list;

    /**
     * 当分页查询的对象与返回值的对象类型一致时，可以使用该构造函数
     */
    public PageVO(Page<T> page) {
        if (page != null) {
            this.totalRow = page.getTotal();
            this.list = page.getRecords();
        } else {
            this.totalRow = 0L;
            this.list = new ArrayList<>();
        }
    }

    public PageVO(Long totalRow) {
        this.totalRow = totalRow;
        this.list = new ArrayList<>();
    }

    public PageVO() {
        this.totalRow = 0L;
        this.list = new ArrayList<>();
    }
}
