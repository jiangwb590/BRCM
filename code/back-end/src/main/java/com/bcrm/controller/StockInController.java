package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.StockInRecord;
import com.bcrm.service.StockInRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 入库记录控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "入库管理")
@RestController
@RequestMapping("/stock-in")
@RequiredArgsConstructor
public class StockInController {

    private final StockInRecordService stockInRecordService;

    /**
     * 分页查询入库记录
     */
    @Operation(summary = "分页查询入库记录")
    @GetMapping("/page")
    public Result<PageResult<StockInRecord>> page(PageRequest pageRequest, StockInRecord query) {
        Page<StockInRecord> page = stockInRecordService.pageRecords(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 根据ID查询入库记录
     */
    @Operation(summary = "根据ID查询入库记录")
    @GetMapping("/{id}")
    public Result<StockInRecord> getById(@PathVariable Long id) {
        StockInRecord record = stockInRecordService.getById(id);
        return Result.success(record);
    }

    /**
     * 新增入库记录
     */
    @Operation(summary = "新增入库记录")
    @PostMapping
    public Result<Void> add(@RequestBody StockInRecord record) {
        stockInRecordService.addRecord(record);
        return Result.success();
    }

    /**
     * 作废入库记录
     */
    @Operation(summary = "作废入库记录")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        stockInRecordService.cancelRecord(id);
        return Result.success();
    }

    /**
     * 获取入库统计
     */
    @Operation(summary = "获取入库统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = stockInRecordService.getStatistics();
        return Result.success(statistics);
    }
}
