package com.bcrm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.StockInRecord;
import com.bcrm.service.StockInRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    /**
     * 导出入库记录到Excel
     */
    @Operation(summary = "导出入库记录")
    @GetMapping("/export")
    public void export(StockInRecord query, HttpServletResponse response) throws Exception {
        LambdaQueryWrapper<StockInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockInRecord::getStatus, 1);
        wrapper.orderByDesc(StockInRecord::getStockInTime);
        List<StockInRecord> list = stockInRecordService.list(wrapper);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("入库记录");
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers = {"入库单号", "产品名称", "入库数量", "单价", "总金额", "入库类型", "供应商", "入库时间", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (StockInRecord record : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getStockInNo() != null ? record.getStockInNo() : "");
                row.createCell(1).setCellValue(record.getProductName() != null ? record.getProductName() : "");
                row.createCell(2).setCellValue(record.getQuantity() != null ? record.getQuantity() : 0);
                row.createCell(3).setCellValue(record.getUnitPrice() != null ? record.getUnitPrice().doubleValue() : 0);
                row.createCell(4).setCellValue(record.getTotalPrice() != null ? record.getTotalPrice().doubleValue() : 0);
                String typeStr = record.getStockInType() != null ? (record.getStockInType() == 1 ? "采购入库" : record.getStockInType() == 2 ? "退货入库" : "其他") : "";
                row.createCell(5).setCellValue(typeStr);
                row.createCell(6).setCellValue(record.getSupplier() != null ? record.getSupplier() : "");
                row.createCell(7).setCellValue(record.getStockInTime() != null ? record.getStockInTime().toString() : "");
                row.createCell(8).setCellValue(record.getStatus() != null && record.getStatus() == 1 ? "正常" : "已作废");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("入库记录.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
        }
    }
}
