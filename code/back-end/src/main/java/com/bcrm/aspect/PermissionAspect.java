package com.bcrm.aspect;

import com.bcrm.annotation.HasPermission;
import com.bcrm.exception.BusinessException;
import com.bcrm.security.LoginUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限校验切面
 *
 * @author BCRM
 * @since 2026-03-17
 */
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(com.bcrm.annotation.HasPermission)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解
        HasPermission hasPermission = method.getAnnotation(HasPermission.class);
        if (hasPermission == null) {
            return point.proceed();
        }
        
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof LoginUser)) {
            throw new BusinessException("用户信息异常");
        }
        
        LoginUser loginUser = (LoginUser) principal;
        Set<String> userPermissions = loginUser.getPermissions();
        
        // 如果用户拥有超级权限，直接放行
        if (userPermissions != null && userPermissions.contains("*:*:*")) {
            return point.proceed();
        }
        
        // 获取需要的权限
        String[] requiredPerms = hasPermission.value();
        if (requiredPerms.length == 0) {
            return point.proceed();
        }
        
        // 校验权限
        boolean hasAccess = checkPermission(userPermissions, requiredPerms, hasPermission.logical());
        
        if (!hasAccess) {
            String permStr = String.join(",", requiredPerms);
            throw new BusinessException("权限不足，需要权限：" + permStr);
        }
        
        return point.proceed();
    }
    
    /**
     * 检查权限
     */
    private boolean checkPermission(Set<String> userPermissions, String[] requiredPerms, HasPermission.Logical logical) {
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }
        
        List<String> requiredList = Arrays.asList(requiredPerms);
        
        if (logical == HasPermission.Logical.AND) {
            // 需要全部满足
            return userPermissions.containsAll(requiredList);
        } else {
            // 满足其一即可
            return requiredList.stream().anyMatch(userPermissions::contains);
        }
    }
}
