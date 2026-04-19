package com.bcrm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.StockOutRecord;
import com.bcrm.service.StockOutRecordService;
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

    /**
     * 导出出库记录到Excel
     */
    @Operation(summary = "导出出库记录")
    @GetMapping("/export")
    public void export(StockOutRecord query, HttpServletResponse response) throws Exception {
        LambdaQueryWrapper<StockOutRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOutRecord::getStatus, 1);
        wrapper.orderByDesc(StockOutRecord::getStockOutTime);
        List<StockOutRecord> list = stockOutRecordService.list(wrapper);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("出库记录");
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers = {"出库单号", "产品名称", "出库数量", "单价", "总金额", "出库类型", "客户", "备注", "出库时间", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (StockOutRecord record : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getStockOutNo() != null ? record.getStockOutNo() : "");
                row.createCell(1).setCellValue(record.getProductName() != null ? record.getProductName() : "");
                row.createCell(2).setCellValue(record.getQuantity() != null ? record.getQuantity() : 0);
                row.createCell(3).setCellValue(record.getUnitPrice() != null ? record.getUnitPrice().doubleValue() : 0);
                row.createCell(4).setCellValue(record.getTotalPrice() != null ? record.getTotalPrice().doubleValue() : 0);
                String typeStr = record.getStockOutType() != null ? (record.getStockOutType() == 1 ? "服务消耗" : record.getStockOutType() == 2 ? "报废" : record.getStockOutType() == 3 ? "其他" : "客户消费") : "";
                row.createCell(5).setCellValue(typeStr);
                row.createCell(6).setCellValue(record.getStockOutType() != null && record.getStockOutType() == 4 ? (record.getCustomerName() != null ? record.getCustomerName() : "") : "");
                row.createCell(7).setCellValue(record.getRemark() != null ? record.getRemark() : "");
                row.createCell(8).setCellValue(record.getStockOutTime() != null ? record.getStockOutTime().toString() : "");
                row.createCell(9).setCellValue(record.getStatus() != null && record.getStatus() == 1 ? "正常" : "已作废");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("出库记录.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
        }
    }
}
