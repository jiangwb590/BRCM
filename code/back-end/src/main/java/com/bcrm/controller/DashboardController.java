package com.bcrm.controller;

import com.bcrm.common.Result;
import com.bcrm.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 数据分析控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "数据分析")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取概览统计
     */
    @Operation(summary = "获取概览统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> data = dashboardService.getOverview();
        return Result.success(data);
    }

    /**
     * 获取营收趋势
     */
    @Operation(summary = "获取营收趋势")
    @GetMapping("/revenue-trend")
    public Result<Map<String, Object>> getRevenueTrend(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> data = dashboardService.getRevenueTrend(days, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取客户来源统计
     */
    @Operation(summary = "获取客户来源统计")
    @GetMapping("/customer-source")
    public Result<Map<String, Object>> getCustomerSource(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> data = dashboardService.getCustomerSource(startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取客户分类统计
     */
    @Operation(summary = "获取客户分类统计")
    @GetMapping("/customer-category")
    public Result<Map<String, Object>> getCustomerCategory() {
        Map<String, Object> data = dashboardService.getCustomerCategory();
        return Result.success(data);
    }

    /**
     * 获取员工业绩排名
     */
    @Operation(summary = "获取员工业绩排名")
    @GetMapping("/employee-performance")
    public Result<Map<String, Object>> getEmployeePerformance(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> data = dashboardService.getEmployeePerformance(limit, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取项目销量排名
     */
    @Operation(summary = "获取项目销量排名")
    @GetMapping("/project-sales")
    public Result<Map<String, Object>> getProjectSales(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> data = dashboardService.getProjectSales(limit, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取产品/耗材消耗比例趋势
     */
    @Operation(summary = "获取产品/耗材消耗比例趋势")
    @GetMapping("/consumption-ratio-trend")
    public Result<Map<String, Object>> getConsumptionRatioTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> data = dashboardService.getConsumptionRatioTrend(days);
        return Result.success(data);
    }

    /**
     * 获取客户来源趋势
     */
    @Operation(summary = "获取客户来源趋势")
    @GetMapping("/customer-source-trend")
    public Result<Map<String, Object>> getCustomerSourceTrend(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(required = false) String channel) {
        Map<String, Object> data = dashboardService.getCustomerSourceTrend(days, channel);
        return Result.success(data);
    }
}
