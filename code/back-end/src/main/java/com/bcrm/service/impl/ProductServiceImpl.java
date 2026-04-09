package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.Product;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.ProductMapper;
import com.bcrm.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 产品服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Page<Product> pageProducts(PageRequest pageRequest, Product query) {
        Page<Product> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), Product::getName, query.getName());
        wrapper.eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId());
        wrapper.eq(query.getStatus() != null, Product::getStatus, query.getStatus());
        wrapper.orderByDesc(Product::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public List<Product> getStockWarningProducts() {
        return this.list(new LambdaQueryWrapper<Product>()
                .apply("stock <= stock_warning")
                .eq(Product::getStatus, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(Product product) {
        product.setStock(product.getStock() != null ? product.getStock() : 0);
        product.setStockWarning(product.getStockWarning() != null ? product.getStockWarning() : 10);
        product.setStatus(product.getStatus() != null ? product.getStatus() : 1);
        this.save(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new BusinessException("产品ID不能为空");
        }
        this.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStock(product.getStock() + quantity);
        this.updateById(updateProduct);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockOut(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        
        if (product.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStock(product.getStock() - quantity);
        this.updateById(updateProduct);
    }
}
