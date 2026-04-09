package com.bcrm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.CustomerDTO;
import com.bcrm.entity.Customer;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.CustomerMapper;
import com.bcrm.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 客户服务测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("张三");
        testCustomer.setPhone("13800138000");
        testCustomer.setGender(2);
        testCustomer.setCategory("潜在客户");
        testCustomer.setTotalAmount(BigDecimal.ZERO);
        testCustomer.setConsumeCount(0);
        testCustomer.setStatus(1);
        testCustomer.setDeleted(0);
        testCustomer.setCreateTime(LocalDateTime.now());

        customerDTO = new CustomerDTO();
        customerDTO.setName("李四");
        customerDTO.setPhone("13900139000");
        customerDTO.setGender(1);
        customerDTO.setSource("微信");
    }

    @Test
    @DisplayName("分页查询客户测试")
    void testPageCustomers() {
        // 准备测试数据
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(1);
        pageRequest.setSize(10);

        Page<Customer> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testCustomer));
        page.setTotal(1);

        when(customerMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行测试
        Page<Customer> result = customerService.pageCustomers(pageRequest, new Customer());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("张三", result.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("新增客户成功测试")
    void testAddCustomerSuccess() {
        // 准备测试数据
        when(customerMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(customerMapper.insert(any(Customer.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> customerService.addCustomer(customerDTO));

        // 验证insert被调用
        verify(customerMapper, times(1)).insert(any(Customer.class));
    }

    @Test
    @DisplayName("新增客户-手机号已存在测试")
    void testAddCustomerPhoneExists() {
        // 准备测试数据
        when(customerMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 执行测试
        assertThrows(BusinessException.class, () -> customerService.addCustomer(customerDTO));
    }

    @Test
    @DisplayName("修改客户成功测试")
    void testUpdateCustomerSuccess() {
        // 准备测试数据
        customerDTO.setId(1L);
        when(customerMapper.selectById(1L)).thenReturn(testCustomer);
        when(customerMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(customerMapper.updateById(any(Customer.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> customerService.updateCustomer(customerDTO));

        // 验证update被调用
        verify(customerMapper, times(1)).updateById(any(Customer.class));
    }

    @Test
    @DisplayName("修改客户-客户不存在测试")
    void testUpdateCustomerNotFound() {
        // 准备测试数据
        customerDTO.setId(999L);
        when(customerMapper.selectById(999L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> customerService.updateCustomer(customerDTO));
    }

    @Test
    @DisplayName("删除客户测试")
    void testDeleteCustomer() {
        // 准备测试数据
        when(customerMapper.deleteById(1L)).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));

        // 验证delete被调用
        verify(customerMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("更新客户分类-VIP客户测试")
    void testUpdateCustomerCategoryToVIP() {
        // 准备测试数据
        testCustomer.setTotalAmount(new BigDecimal("6000"));
        testCustomer.setConsumeCount(10);
        testCustomer.setLastConsumeTime(LocalDateTime.now());
        when(customerMapper.selectById(1L)).thenReturn(testCustomer);
        when(customerMapper.updateById(any(Customer.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> customerService.updateCustomerCategory(1L));
    }

    @Test
    @DisplayName("获取客户来源统计测试")
    void testGetSourceStatistics() {
        // 准备测试数据
        when(customerMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList("微信", "抖音", "美团"));

        // 执行测试
        List<Object> result = customerService.getSourceStatistics();

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("获取客户分类统计测试")
    void testGetCategoryStatistics() {
        // 准备测试数据
        when(customerMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList("VIP客户", "老客户", "新客户"));

        // 执行测试
        List<Object> result = customerService.getCategoryStatistics();

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
