package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.annotation.OperLog;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.ConsumeRecordDTO;
import com.bcrm.entity.ConsumeRecord;
import com.bcrm.service.ConsumeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费记录控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "消费记录管理")
@RestController
@RequestMapping("/consume")
@RequiredArgsConstructor
public class ConsumeRecordController {

    private final ConsumeRecordService consumeRecordService;

    /**
     * 分页查询消费记录
     */
    @Operation(summary = "分页查询消费记录")
    @GetMapping("/page")
    public Result<PageResult<ConsumeRecord>> page(PageRequest pageRequest, ConsumeRecord query) {
        Page<ConsumeRecord> page = consumeRecordService.pageRecords(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询客户消费记录
     */
    @Operation(summary = "查询客户消费记录")
    @GetMapping("/customer/{customerId}")
    public Result<List<ConsumeRecord>> getByCustomerId(@PathVariable Long customerId) {
        List<ConsumeRecord> list = consumeRecordService.getByCustomerId(customerId);
        return Result.success(list);
    }

    /**
     * 创建消费记录
     */
    @Operation(summary = "创建消费记录")
    @OperLog(title = "客户消费", businessType = 1, targetType = "customer")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ConsumeRecordDTO dto) {
        consumeRecordService.createRecord(dto);
        return Result.success();
    }

    /**
     * 获取营收统计
     */
    @Operation(summary = "获取营收统计")
    @GetMapping("/statistics/revenue")
    public Result<Map<String, Object>> getRevenueStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        Map<String, Object> statistics = consumeRecordService.getRevenueStatistics(startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取今日营收
     */
    @Operation(summary = "获取今日营收")
    @GetMapping("/statistics/today")
    public Result<BigDecimal> getTodayRevenue() {
        BigDecimal revenue = consumeRecordService.getTodayRevenue();
        return Result.success(revenue);
    }
}
