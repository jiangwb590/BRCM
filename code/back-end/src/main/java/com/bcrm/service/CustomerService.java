package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.CustomerDTO;
import com.bcrm.entity.Customer;

import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService extends IService<Customer> {

    Page<Customer> pageCustomers(PageRequest pageRequest, Customer query);

    void addCustomer(CustomerDTO dto);

    void updateCustomer(CustomerDTO dto);

    void deleteCustomer(Long id);

    List<Object> getSourceStatistics();

    List<Object> getCategoryStatistics();

    void updateCustomerCategory(Long customerId);

    List<Customer> getCustomerListForExport(Customer query);
}