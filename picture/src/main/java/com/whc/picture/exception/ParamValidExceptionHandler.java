package com.whc.picture.exception;

import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.List;
import java.util.Set;


/**
 * 对于不同的参数解析方式，Spring 抛出的异常也不同，而且这些异常没有继承关系，异常的内部也各不相同，只能对每种异常单独处理。
 * 跟参数相关的异常主要有三个需要手动处理：
 * javax.validation.ConstraintViolationException
 * org.springframework.web.bind.MethodArgumentNotValidException
 * org.springframework.validation.BindException
 * <p>
 * 参考链接：https://segmentfault.com/a/1190000023471742
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ParamValidExceptionHandler {

    /**
     * GET请求一般会使用requestParam/PathVariable传参。
     * 举例：
     *
     * @GetMapping("/getSmsCode") public Ret getSmsCode(@PathVariable("phone") @NotNull String  phone)
     * <p>
     * 如果校验失败，会抛出ConstraintViolationException异常。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BaseResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.info("普通参数校验失败：{}", e.getMessage());

        StringBuilder sb = new StringBuilder();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            Path path = violation.getPropertyPath();
            String[] pathArr = path.toString().split("\\.");
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(pathArr[1]).append(violation.getMessage());
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, sb.toString());
    }

    /**
     * POST、PUT请求一般会使用requestBody传递参数。
     * 举例：
     *
     * @PostMapping("/getSmsCode") public Ret getSmsCode(@RequestBody GetSmsCodeForRegister qo)
     * <p>
     * 如果校验失败，会抛出MethodArgumentNotValidException异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.info("RequestBody对象参数校验失败：{}", e.getMessage());

        StringBuilder sb = new StringBuilder();

        List<ObjectError> objectErrors = e.getBindingResult().getAllErrors();
        for (ObjectError objectError : objectErrors) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(objectError.getDefaultMessage());
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, sb.toString());
    }

    /**
     * 请求参数绑定到java bean上失败时抛出。注：没有@RequestBody。纯对象的绑定。
     * 举例：
     *
     * @PostMapping("/getSmsCode") public Ret getSmsCode(GetSmsCodeForRegister qo)
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BaseResponse<?> handleBindException(BindException e) {
        log.info("实体对象参数校验失败：{}", e.getMessage());

        StringBuilder sb = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(error.getField()).append(error.getDefaultMessage());
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, sb.toString());
    }

}
