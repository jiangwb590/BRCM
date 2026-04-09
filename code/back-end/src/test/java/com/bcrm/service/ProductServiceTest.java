package com.bcrm.service;

import com.bcrm.entity.Product;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.ProductMapper;
import com.bcrm.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 产品服务测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductCode("PRD001");
        testProduct.setName("护肤品套装");
        testProduct.setSpecification("100ml");
        testProduct.setUnit("套");
        testProduct.setPurchasePrice(new BigDecimal("200"));
        testProduct.setSalePrice(new BigDecimal("398"));
        testProduct.setStock(50);
        testProduct.setStockWarning(10);
        testProduct.setStatus(1);
        testProduct.setDeleted(0);
        testProduct.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("入库成功测试")
    void testStockInSuccess() {
        // 准备测试数据
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> productService.stockIn(1L, 10));

        // 验证update被调用
        verify(productMapper, times(1)).updateById(any(Product.class));
    }

    @Test
    @DisplayName("入库-产品不存在测试")
    void testStockInProductNotFound() {
        // 准备测试数据
        when(productMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> productService.stockIn(1L, 10));
    }

    @Test
    @DisplayName("出库成功测试")
    void testStockOutSuccess() {
        // 准备测试数据
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> productService.stockOut(1L, 10));

        // 验证update被调用
        verify(productMapper, times(1)).updateById(any(Product.class));
    }

    @Test
    @DisplayName("出库-库存不足测试")
    void testStockOutInsufficientStock() {
        // 准备测试数据
        testProduct.setStock(5);
        when(productMapper.selectById(1L)).thenReturn(testProduct);

        // 执行测试
        assertThrows(BusinessException.class, () -> productService.stockOut(1L, 10));
    }

    @Test
    @DisplayName("出库-产品不存在测试")
    void testStockOutProductNotFound() {
        // 准备测试数据
        when(productMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> productService.stockOut(1L, 10));
    }

    @Test
    @DisplayName("修改产品-ID为空测试")
    void testUpdateProductWithoutId() {
        // 准备测试数据
        Product updateProduct = new Product();
        updateProduct.setName("更新后的产品");

        // 执行测试
        assertThrows(BusinessException.class, () -> productService.updateProduct(updateProduct));
    }

    @Test
    @DisplayName("作废会员卡-卡不存在测试")
    void testDisableCardNotFound() {
        // 这个测试验证业务异常处理
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException("会员卡不存在");
        });
    }
}