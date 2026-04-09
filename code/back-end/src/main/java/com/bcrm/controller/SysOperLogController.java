package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.SysOperLog;
import com.bcrm.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 操作日志控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/system/oper-log")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    /**
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    public Result<PageResult<SysOperLog>> page(
            PageRequest pageRequest, 
            SysOperLog query,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<SysOperLog> page = sysOperLogService.pageLogs(pageRequest, query, startTime, endTime);
        return Result.success(PageResult.of(page));
    }

    /**
     * 根据ID查询操作日志
     */
    @Operation(summary = "根据ID查询操作日志")
    @GetMapping("/{id}")
    public Result<SysOperLog> getById(@PathVariable Long id) {
        SysOperLog log = sysOperLogService.getById(id);
        return Result.success(log);
    }

    /**
     * 删除操作日志
     */
    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysOperLogService.removeById(id);
        return Result.success();
    }

    /**
     * 清空操作日志
     */
    @Operation(summary = "清空操作日志")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        sysOperLogService.remove(null);
        return Result.success();
    }
}
