package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.Product;

import java.util.List;

/**
 * 产品服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询产品
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<Product> pageProducts(PageRequest pageRequest, Product query);

    /**
     * 获取库存预警产品
     *
     * @return 产品列表
     */
    List<Product> getStockWarningProducts();

    /**
     * 新增产品
     *
     * @param product 产品信息
     */
    void addProduct(Product product);

    /**
     * 修改产品
     *
     * @param product 产品信息
     */
    void updateProduct(Product product);

    /**
     * 入库
     *
     * @param productId 产品ID
     * @param quantity 数量
     */
    void stockIn(Long productId, Integer quantity);

    /**
     * 出库
     *
     * @param productId 产品ID
     * @param quantity 数量
     */
    void stockOut(Long productId, Integer quantity);
}
