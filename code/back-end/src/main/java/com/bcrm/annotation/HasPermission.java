package com.bcrm.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标注需要进行权限校验的方法
 *
 * @author BCRM
 * @since 2026-03-17
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

    /**
     * 需要的权限标识（支持多个，满足其一即可）
     */
    String[] value() default {};

    /**
     * 权限逻辑关系：AND-全部满足，OR-满足其一
     */
    Logical logical() default Logical.OR;

    /**
     * 逻辑关系枚举
     */
    enum Logical {
        AND,
        OR
    }
}
