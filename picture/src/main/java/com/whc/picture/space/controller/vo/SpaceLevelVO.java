package com.whc.picture.space.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间级别
 */
@Data
@AllArgsConstructor
public class SpaceLevelVO {
    /**
     * 值
     */
    private int value;

    /**
     * 中文
     */
    private String text;

    /**
     * 空间级别对应的 最大数量
     */
    private long maxCount;

    /**
     * 空间级别对应的 最大容量
     */
    private long maxSize;
}
