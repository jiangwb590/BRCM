package com.bcrm.aspect;

import com.bcrm.annotation.OperLog;
import com.bcrm.entity.SysOperLog;
import com.bcrm.service.SysOperLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志AOP切面
 *
 * @author BCRM
 * @since 2026-03-17
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogService operLogService;
    private final ObjectMapper objectMapper;

    /**
     * 定义切点：所有带有@OperLog注解的方法
     */
    @Pointcut("@annotation(com.bcrm.annotation.OperLog)")
    public void operLogPointCut() {
    }

    /**
     * 方法执行后记录日志
     */
    @AfterReturning(pointcut = "operLogPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * 方法抛出异常后记录日志
     */
    @AfterThrowing(pointcut = "operLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    /**
     * 处理日志记录
     */
    private void handleLog(JoinPoint joinPoint, Exception e, Object jsonResult) {
        try {
            // 获取注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperLog operLogAnnotation = method.getAnnotation(OperLog.class);
            if (operLogAnnotation == null) {
                return;
            }

            // 获取请求信息
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            // 创建日志对象
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(1);
            operLog.setOperIp(getClientIp(request));
            operLog.setOperUrl(request.getRequestURI());
            operLog.setOperName(getCurrentUsername());
            operLog.setOperTime(LocalDateTime.now());
            operLog.setRequestMethod(request.getMethod());

            // 设置注解信息
            operLog.setTitle(operLogAnnotation.title());
            operLog.setBusinessType(operLogAnnotation.businessType());
            operLog.setTargetType(operLogAnnotation.targetType());

            // 设置方法名
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            operLog.setMethod(className + "." + methodName + "()");

            // 保存请求参数
            if (operLogAnnotation.isSaveParam()) {
                String params = getRequestParams(joinPoint);
                operLog.setOperParam(params.length() > 2000 ? params.substring(0, 2000) : params);
                
                // 尝试从参数中提取目标对象信息
                extractTargetInfo(joinPoint, operLog);
            }

            // 从上下文获取目标名称（Controller 手动设置）
            String contextTargetName = OperLogContextHolder.getTargetName();
            if (contextTargetName != null && !contextTargetName.isEmpty()) {
                operLog.setTargetName(contextTargetName);
            }

            // 保存响应结果
            if (operLogAnnotation.isSaveResult() && jsonResult != null) {
                String result = objectMapper.writeValueAsString(jsonResult);
                operLog.setJsonResult(result.length() > 2000 ? result.substring(0, 2000) : result);
            }

            // 异常处理
            if (e != null) {
                operLog.setStatus(0);
                operLog.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 2000 
                    ? e.getMessage().substring(0, 2000) : e.getMessage());
            }

            // 保存日志
            operLogService.save(operLog);
            
            // 清理上下文
            OperLogContextHolder.clear();
        } catch (Exception ex) {
            log.error("记录操作日志异常", ex);
            OperLogContextHolder.clear();
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            if (username != null && !"anonymousUser".equals(username)) {
                return username;
            }
        }
        return "系统";
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                if (paramNames != null && i < paramNames.length) {
                    Object arg = args[i];
                    // 过滤不需要的参数
                    if (arg instanceof HttpServletRequest 
                        || arg instanceof HttpServletResponse 
                        || arg instanceof MultipartFile) {
                        continue;
                    }
                    params.put(paramNames[i], arg);
                }
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 从参数中提取目标对象信息
     */
    private void extractTargetInfo(JoinPoint joinPoint, SysOperLog operLog) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) continue;
                
                Object arg = args[i];
                String paramName = paramNames != null && i < paramNames.length ? paramNames[i] : "";
                
                // 处理路径参数（Long 类型的 ID）
                if (arg instanceof Long && ("id".equals(paramName) || paramName.endsWith("Id"))) {
                    operLog.setTargetId((Long) arg);
                    continue;
                }
                
                // 处理请求体参数（DTO 对象）
                try {
                    String argStr = objectMapper.writeValueAsString(arg);
                    Map<String, Object> map = objectMapper.readValue(argStr, Map.class);
                    
                    // 根据目标类型提取名称
                    String targetType = operLog.getTargetType();
                    
                    // 提取客户名称（name 字段）
                    if ("customer".equals(targetType) && map.containsKey("name")) {
                        operLog.setTargetName((String) map.get("name"));
                    }
                    
                    // 提取项目名称（name 字段）
                    if ("project".equals(targetType) && map.containsKey("name")) {
                        operLog.setTargetName((String) map.get("name"));
                    }
                    
                    // 提取套餐名称（packageName 字段）
                    if ("package".equals(targetType) && map.containsKey("packageName")) {
                        operLog.setTargetName((String) map.get("packageName"));
                    }
                    
                    // 提取产品名称（name 字段）
                    if ("product".equals(targetType) && map.containsKey("name")) {
                        operLog.setTargetName((String) map.get("name"));
                    }
                    
                    // 其他可能的名称字段
                    if (map.containsKey("customerName")) {
                        operLog.setTargetName((String) map.get("customerName"));
                    }
                    
                    // 提取ID
                    if (map.containsKey("id")) {
                        Object id = map.get("id");
                        if (id instanceof Number) {
                            operLog.setTargetId(((Number) id).longValue());
                        }
                    }
                    if (map.containsKey("customerId")) {
                        Object id = map.get("customerId");
                        if (id instanceof Number) {
                            operLog.setTargetId(((Number) id).longValue());
                        }
                    }
                } catch (Exception e) {
                    // 不是 JSON 对象，跳过
                }
            }
        } catch (Exception e) {
            log.debug("提取目标对象信息失败", e);
        }
    }
}
