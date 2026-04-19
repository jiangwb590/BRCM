package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.Product;
import com.bcrm.service.ProductService;
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

/**
 * 产品控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "产品管理")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询产品
     */
    @Operation(summary = "分页查询产品")
    @GetMapping("/page")
    public Result<PageResult<Product>> page(PageRequest pageRequest, Product query) {
        Page<Product> page = productService.pageProducts(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取所有产品
     */
    @Operation(summary = "获取所有产品")
    @GetMapping("/all")
    public Result<List<Product>> getAll() {
        List<Product> list = productService.list();
        return Result.success(list);
    }

    /**
     * 获取库存预警产品
     */
    @Operation(summary = "获取库存预警产品")
    @GetMapping("/stock-warning")
    public Result<List<Product>> getStockWarningProducts() {
        List<Product> list = productService.getStockWarningProducts();
        return Result.success(list);
    }

    /**
     * 根据ID查询产品
     */
    @Operation(summary = "根据ID查询产品")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    /**
     * 新增产品
     */
    @Operation(summary = "新增产品")
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        productService.addProduct(product);
        return Result.success();
    }

    /**
     * 修改产品
     */
    @Operation(summary = "修改产品")
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success();
    }

    /**
     * 删除产品
     */
    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    /**
     * 入库
     */
    @Operation(summary = "入库")
    @PostMapping("/stock-in/{id}")
    public Result<Void> stockIn(@PathVariable Long id, @RequestParam Integer quantity) {
        productService.stockIn(id, quantity);
        return Result.success();
    }

    /**
     * 出库
     */
    @Operation(summary = "出库")
    @PostMapping("/stock-out/{id}")
    public Result<Void> stockOut(@PathVariable Long id, @RequestParam Integer quantity) {
        productService.stockOut(id, quantity);
        return Result.success();
    }

    /**
     * 导出产品列表到Excel
     */
    @Operation(summary = "导出产品列表")
    @GetMapping("/export")
    public void export(Product query, HttpServletResponse response) throws Exception {
        List<Product> list = productService.list();
        
        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("产品列表");
            
            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建表头
            String[] headers = {"产品编号", "产品名称", "分类", "规格", "单位", "售价", "库存", "预警值", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (Product product : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getProductCode() != null ? product.getProductCode() : "");
                row.createCell(1).setCellValue(product.getName() != null ? product.getName() : "");
                row.createCell(2).setCellValue(product.getCategoryName() != null ? product.getCategoryName() : "");
                row.createCell(3).setCellValue(product.getSpecification() != null ? product.getSpecification() : "");
                row.createCell(4).setCellValue(product.getUnit() != null ? product.getUnit() : "");
                row.createCell(5).setCellValue(product.getSalePrice() != null ? product.getSalePrice().doubleValue() : 0);
                row.createCell(6).setCellValue(product.getStock() != null ? product.getStock() : 0);
                row.createCell(7).setCellValue(product.getStockWarning() != null ? product.getStockWarning() : 0);
                row.createCell(8).setCellValue(product.getStatus() != null && product.getStatus() == 1 ? "启用" : "禁用");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("产品列表.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            // 写入响应
            workbook.write(response.getOutputStream());
        }
    }
}
