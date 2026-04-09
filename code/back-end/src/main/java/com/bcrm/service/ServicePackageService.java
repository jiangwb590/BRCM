package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.PackagePurchaseDTO;
import com.bcrm.dto.ServicePackageDTO;
import com.bcrm.entity.PackageItem;
import com.bcrm.entity.PackagePurchase;
import com.bcrm.entity.ServicePackage;

import java.util.List;

/**
 * 项目套餐服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface ServicePackageService extends IService<ServicePackage> {

    /**
     * 分页查询套餐
     */
    Page<ServicePackage> pagePackages(PageRequest pageRequest, ServicePackage query);

    /**
     * 查询所有上架套餐
     */
    List<ServicePackage> listActivePackages();

    /**
     * 新增套餐
     */
    void addPackage(ServicePackageDTO dto);

    /**
     * 修改套餐
     */
    void updatePackage(ServicePackageDTO dto);

    /**
     * 删除套餐
     */
    void deletePackage(Long id);

    /**
     * 修改套餐状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取套餐项目列表
     */
    List<PackageItem> getPackageItems(Long packageId);

    /**
     * 购买套餐
     */
    void purchasePackage(PackagePurchaseDTO dto);

    /**
     * 分页查询购买记录
     */
    Page<PackagePurchase> pagePurchases(PageRequest pageRequest, Long customerId);

    /**
     * 获取客户已购买的套餐（有剩余次数的）
     */
    List<PackagePurchase> getCustomerActivePurchases(Long customerId);
}
