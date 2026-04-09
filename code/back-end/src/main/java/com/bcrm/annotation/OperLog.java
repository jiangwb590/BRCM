package com.bcrm.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author BCRM
 * @since 2026-03-17
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 操作标题
     */
    String title() default "";

    /**
     * 业务类型：0-其他 1-新增 2-修改 3-删除 4-查询
     */
    int businessType() default 0;

    /**
     * 操作对象类型：customer-客户, project-项目, package-套餐等
     */
    String targetType() default "";

    /**
     * 是否保存请求参数
     */
    boolean isSaveParam() default true;

    /**
     * 是否保存响应结果
     */
    boolean isSaveResult() default true;
}
