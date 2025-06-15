package com.whc.picture.common;

import com.whc.picture.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应封装类
 *
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 状态描述
     */
    private String message;

    /**
     * 结果集
     */
    private T data;


    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    public BaseResponse(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    /**
     * 一些常用的错误返回值
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> businessException(String message) {
        return new BaseResponse<>(ErrorCode.OPERATION_ERROR, message);
    }
}
