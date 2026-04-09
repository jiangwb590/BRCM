package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.Product;
import com.bcrm.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
