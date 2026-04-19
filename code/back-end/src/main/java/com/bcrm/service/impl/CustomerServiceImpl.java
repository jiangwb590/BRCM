package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.CustomerDTO;
import com.bcrm.entity.Customer;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.CustomerMapper;
import com.bcrm.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Override
    public Page<Customer> pageCustomers(PageRequest pageRequest, Customer query) {
        Page<Customer> page = pageRequest.toPage();
        
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                .like(Customer::getName, query.getKeyword())
                .or()
                .like(Customer::getPhone, query.getKeyword())
            );
        }
        
        wrapper.eq(StringUtils.hasText(query.getSource()), Customer::getSource, query.getSource());
        wrapper.eq(StringUtils.hasText(query.getCategory()), Customer::getCategory, query.getCategory());
        wrapper.eq(query.getAdvisorId() != null, Customer::getAdvisorId, query.getAdvisorId());
        wrapper.eq(query.getStatus() != null, Customer::getStatus, query.getStatus());
        wrapper.orderByDesc(Customer::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCustomer(CustomerDTO dto) {
        if (this.checkPhoneExists(dto.getPhone())) {
            throw new BusinessException("手机号已存在");
        }
        
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setGender(dto.getGender());
        customer.setPhone(dto.getPhone());
        customer.setWechat(dto.getWechat());
        customer.setBirthday(dto.getBirthday());
        customer.setSource(dto.getSource());
        customer.setMemberLevelId(dto.getMemberLevelId());
        customer.setAdvisorId(dto.getAdvisorId());
        customer.setTags(dto.getTags());
        customer.setRemark(dto.getRemark());
        customer.setCategory("潜在客户");
        customer.setTotalAmount(BigDecimal.ZERO);
        customer.setConsumeCount(0);
        customer.setBalance(BigDecimal.ZERO);
        customer.setPoints(0);
        customer.setStatus(1);
        
        this.save(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomer(CustomerDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("客户ID不能为空");
        }
        
        Customer existCustomer = this.getById(dto.getId());
        if (existCustomer == null) {
            throw new BusinessException("客户不存在");
        }
        
        if (!existCustomer.getPhone().equals(dto.getPhone()) && this.checkPhoneExists(dto.getPhone())) {
            throw new BusinessException("手机号已存在");
        }
        
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setGender(dto.getGender());
        customer.setPhone(dto.getPhone());
        customer.setWechat(dto.getWechat());
        customer.setBirthday(dto.getBirthday());
        customer.setSource(dto.getSource());
        customer.setMemberLevelId(dto.getMemberLevelId());
        customer.setAdvisorId(dto.getAdvisorId());
        customer.setTags(dto.getTags());
        customer.setRemark(dto.getRemark());
        
        this.updateById(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomer(Long id) {
        this.removeById(id);
    }

    @Override
    public List<Object> getSourceStatistics() {
        return this.getBaseMapper().selectObjs(
                new LambdaQueryWrapper<Customer>()
                        .select(Customer::getSource)
                        .groupBy(Customer::getSource)
        );
    }

    @Override
    public List<Object> getCategoryStatistics() {
        return this.getBaseMapper().selectObjs(
                new LambdaQueryWrapper<Customer>()
                        .select(Customer::getCategory)
                        .groupBy(Customer::getCategory)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerCategory(Long customerId) {
        Customer customer = this.getById(customerId);
        if (customer == null) {
            return;
        }
        
        String category = calculateCategory(customer);
        
        if (!category.equals(customer.getCategory())) {
            Customer updateCustomer = new Customer();
            updateCustomer.setId(customerId);
            updateCustomer.setCategory(category);
            this.updateById(updateCustomer);
        }
    }

    @Override
    public List<Customer> getCustomerListForExport(Customer query) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                .like(Customer::getName, query.getKeyword())
                .or()
                .like(Customer::getPhone, query.getKeyword())
            );
        }
        
        wrapper.eq(StringUtils.hasText(query.getSource()), Customer::getSource, query.getSource());
        wrapper.eq(StringUtils.hasText(query.getCategory()), Customer::getCategory, query.getCategory());
        wrapper.eq(query.getStatus() != null, Customer::getStatus, query.getStatus());
        wrapper.orderByDesc(Customer::getCreateTime);
        
        return this.list(wrapper);
    }

    private String calculateCategory(Customer customer) {
        BigDecimal totalAmount = customer.getTotalAmount();
        Integer consumeCount = customer.getConsumeCount();
        LocalDateTime lastConsumeTime = customer.getLastConsumeTime();
        
        if (totalAmount != null && totalAmount.compareTo(new BigDecimal("5000")) >= 0) {
            return "VIP客户";
        }
        
        if (lastConsumeTime != null && consumeCount != null && consumeCount > 0) {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
            if (lastConsumeTime.isBefore(ninetyDaysAgo)) {
                return "沉睡客户";
            }
        }
        
        if (consumeCount != null && consumeCount > 3 && lastConsumeTime != null) {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
            if (lastConsumeTime.isAfter(ninetyDaysAgo)) {
                return "老客户";
            }
        }
        
        if (consumeCount != null && consumeCount > 0 && consumeCount <= 3 && lastConsumeTime != null) {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            if (lastConsumeTime.isAfter(thirtyDaysAgo)) {
                return "新客户";
            }
        }
        
        return "潜在客户";
    }

    private boolean checkPhoneExists(String phone) {
        return this.count(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, phone)
                .eq(Customer::getDeleted, 0)) > 0;
    }
}