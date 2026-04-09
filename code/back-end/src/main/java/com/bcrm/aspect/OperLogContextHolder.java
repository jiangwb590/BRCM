package com.bcrm.aspect;

/**
 * 操作日志上下文持有者
 * 用于在同一线程中传递日志信息
 *
 * @author BCRM
 * @since 2026-03-17
 */
public class OperLogContextHolder {

    private static final ThreadLocal<String> targetNameHolder = new ThreadLocal<>();

    public static void setTargetName(String targetName) {
        targetNameHolder.set(targetName);
    }

    public static String getTargetName() {
        return targetNameHolder.get();
    }

    public static void clear() {
        targetNameHolder.remove();
    }
}
