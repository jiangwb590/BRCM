package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.StockOutRecord;
import com.bcrm.service.StockOutRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 出库记录控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "出库管理")
@RestController
@RequestMapping("/stock-out")
@RequiredArgsConstructor
public class StockOutController {

    private final StockOutRecordService stockOutRecordService;

    /**
     * 分页查询出库记录
     */
    @Operation(summary = "分页查询出库记录")
    @GetMapping("/page")
    public Result<PageResult<StockOutRecord>> page(PageRequest pageRequest, StockOutRecord query) {
        Page<StockOutRecord> page = stockOutRecordService.pageRecords(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 根据ID查询出库记录
     */
    @Operation(summary = "根据ID查询出库记录")
    @GetMapping("/{id}")
    public Result<StockOutRecord> getById(@PathVariable Long id) {
        StockOutRecord record = stockOutRecordService.getById(id);
        return Result.success(record);
    }

    /**
     * 新增出库记录
     */
    @Operation(summary = "新增出库记录")
    @PostMapping
    public Result<Void> add(@RequestBody StockOutRecord record) {
        stockOutRecordService.addRecord(record);
        return Result.success();
    }

    /**
     * 作废出库记录
     */
    @Operation(summary = "作废出库记录")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        stockOutRecordService.cancelRecord(id);
        return Result.success();
    }

    /**
     * 获取出库统计
     */
    @Operation(summary = "获取出库统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = stockOutRecordService.getStatistics();
        return Result.success(statistics);
    }
}
