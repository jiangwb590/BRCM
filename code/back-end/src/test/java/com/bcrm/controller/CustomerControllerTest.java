package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.Customer;
import com.bcrm.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 客户控制器测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private Customer testCustomer;

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
        testCustomer.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("分页查询客户列表测试")
    @WithMockUser(username = "admin", roles = {"admin"})
    void testPageCustomers() throws Exception {
        // 准备测试数据
        Page<Customer> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testCustomer));
        page.setTotal(1);

        when(customerService.pageCustomers(any(PageRequest.class), any(Customer.class))).thenReturn(page);

        // 执行测试
        mockMvc.perform(get("/customer/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].name").value("张三"));
    }

    @Test
    @DisplayName("根据ID查询客户测试")
    @WithMockUser(username = "admin", roles = {"admin"})
    void testGetCustomerById() throws Exception {
        // 准备测试数据
        when(customerService.getById(1L)).thenReturn(testCustomer);

        // 执行测试
        mockMvc.perform(get("/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("张三"));
    }

    @Test
    @DisplayName("未认证访问测试")
    void testUnauthorizedAccess() throws Exception {
        // 执行测试
        mockMvc.perform(get("/customer/page"))
                .andExpect(status().isUnauthorized());
    }
}
