package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.annotation.OperLog;
import com.bcrm.aspect.OperLogContextHolder;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.PackagePurchaseDTO;
import com.bcrm.dto.ServicePackageDTO;
import com.bcrm.entity.PackageItem;
import com.bcrm.entity.PackagePurchase;
import com.bcrm.entity.ServicePackage;
import com.bcrm.service.ServicePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目套餐控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "套餐管理")
@RestController
@RequestMapping("/package")
@RequiredArgsConstructor
public class ServicePackageController {

    private final ServicePackageService servicePackageService;

    /**
     * 分页查询套餐
     */
    @Operation(summary = "分页查询套餐")
    @GetMapping("/page")
    public Result<PageResult<ServicePackage>> page(PageRequest pageRequest, ServicePackage query) {
        Page<ServicePackage> page = servicePackageService.pagePackages(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询所有上架套餐
     */
    @Operation(summary = "查询所有上架套餐")
    @GetMapping("/active")
    public Result<List<ServicePackage>> listActive() {
        List<ServicePackage> list = servicePackageService.listActivePackages();
        return Result.success(list);
    }

    /**
     * 根据ID查询套餐
     */
    @Operation(summary = "根据ID查询套餐")
    @GetMapping("/{id}")
    public Result<ServicePackage> getById(@PathVariable Long id) {
        ServicePackage pkg = servicePackageService.getById(id);
        return Result.success(pkg);
    }

    /**
     * 查询套餐项目列表
     */
    @Operation(summary = "查询套餐项目列表")
    @GetMapping("/{id}/items")
    public Result<List<PackageItem>> getPackageItems(@PathVariable Long id) {
        List<PackageItem> items = servicePackageService.getPackageItems(id);
        return Result.success(items);
    }

    /**
     * 新增套餐
     */
    @Operation(summary = "新增套餐")
    @OperLog(title = "套餐管理", businessType = 1, targetType = "package")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ServicePackageDTO dto) {
        servicePackageService.addPackage(dto);
        return Result.success();
    }

    /**
     * 修改套餐
     */
    @Operation(summary = "修改套餐")
    @OperLog(title = "套餐管理", businessType = 2, targetType = "package")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ServicePackageDTO dto) {
        servicePackageService.updatePackage(dto);
        return Result.success();
    }

    /**
     * 删除套餐
     */
    @Operation(summary = "删除套餐")
    @OperLog(title = "套餐管理", businessType = 3, targetType = "package")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 获取套餐名称用于日志记录
        ServicePackage pkg = servicePackageService.getById(id);
        String packageName = pkg != null ? pkg.getPackageName() : null;
        servicePackageService.deletePackage(id);
        OperLogContextHolder.setTargetName(packageName);
        return Result.success();
    }

    /**
     * 修改套餐状态
     */
    @Operation(summary = "修改套餐状态")
    @OperLog(title = "套餐管理", businessType = 2, targetType = "package")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        servicePackageService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 购买套餐
     */
    @Operation(summary = "购买套餐")
    @OperLog(title = "套餐购买", businessType = 1, targetType = "package")
    @PostMapping("/purchase")
    public Result<Void> purchase(@Valid @RequestBody PackagePurchaseDTO dto) {
        servicePackageService.purchasePackage(dto);
        return Result.success();
    }

    /**
     * 分页查询购买记录
     */
    @Operation(summary = "分页查询购买记录")
    @GetMapping("/purchase/page")
    public Result<PageResult<PackagePurchase>> pagePurchases(PageRequest pageRequest, @RequestParam(required = false) Long customerId) {
        Page<PackagePurchase> page = servicePackageService.pagePurchases(pageRequest, customerId);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取客户已购买的套餐（有剩余次数的）
     */
    @Operation(summary = "获取客户已购买的套餐")
    @GetMapping("/customer/{customerId}/purchases")
    public Result<List<PackagePurchase>> getCustomerPurchases(@PathVariable Long customerId) {
        List<PackagePurchase> list = servicePackageService.getCustomerActivePurchases(customerId);
        return Result.success(list);
    }
}
