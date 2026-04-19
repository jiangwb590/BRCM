package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.ConsumeRecordDTO;
import com.bcrm.entity.*;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.ConsumeRecordMapper;
import com.bcrm.mapper.CustomerCareMapper;
import com.bcrm.mapper.PackagePurchaseMapper;
import com.bcrm.mapper.ProductMapper;
import com.bcrm.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 消费记录服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumeRecordServiceImpl extends ServiceImpl<ConsumeRecordMapper, ConsumeRecord> implements ConsumeRecordService {

    private final CustomerService customerService;
    private final ServiceProjectService serviceProjectService;
    private final ServicePackageService servicePackageService;
    private final SysUserService sysUserService;
    private final MemberCardService memberCardService;
    private final PackagePurchaseMapper packagePurchaseMapper;
    private final CustomerCareMapper customerCareMapper;
    private final SysDictService sysDictService;
    private final ProductMapper productMapper;

    @Override
    public Page<ConsumeRecord> pageRecords(PageRequest pageRequest, ConsumeRecord query) {
        Page<ConsumeRecord> page = pageRequest.toPage();
        
        LambdaQueryWrapper<ConsumeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getCustomerName()), ConsumeRecord::getCustomerName, query.getCustomerName());
        wrapper.eq(query.getCustomerId() != null, ConsumeRecord::getCustomerId, query.getCustomerId());
        wrapper.eq(StringUtils.hasText(query.getConsumeType()), ConsumeRecord::getConsumeType, query.getConsumeType());
        wrapper.orderByDesc(ConsumeRecord::getConsumeTime);
        
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRecord(ConsumeRecordDTO dto) {
        // 查询客户信息
        Customer customer = customerService.getById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        
        ConsumeRecord record = new ConsumeRecord();
        record.setConsumeNo(generateConsumeNo());
        record.setCustomerId(dto.getCustomerId());
        record.setCustomerName(customer.getName());
        record.setConsumeType(dto.getConsumeType());
        record.setPayAmount(dto.getPayAmount() != null ? dto.getPayAmount() : dto.getAmount());
        record.setAppointmentId(dto.getAppointmentId());
        record.setPayMethod(dto.getPayMethod());
        record.setRemark(dto.getRemark());
        record.setConsumeTime(dto.getConsumeTime() != null ? dto.getConsumeTime() : LocalDateTime.now());
        record.setConsumeTimes(dto.getConsumeTimes() != null ? dto.getConsumeTimes() : 1);
        
        // 项目/套餐描述（用于消费关怀短信内容）
        String projectDescription = null;
        
        // 设置美容师
        if (dto.getBeauticianId() != null) {
            SysUser beautician = sysUserService.getById(dto.getBeauticianId());
            if (beautician != null) {
                record.setBeauticianId(dto.getBeauticianId());
                record.setBeauticianName(beautician.getRealName());
            }
        }
        
        // 根据消费模式处理
        if ("package".equals(dto.getConsumeMode())) {
            // 套餐消费模式
            if (dto.getPurchaseId() == null) {
                throw new BusinessException("请选择已购买的套餐");
            }
            
            PackagePurchase purchase = packagePurchaseMapper.selectById(dto.getPurchaseId());
            if (purchase == null) {
                throw new BusinessException("套餐购买记录不存在");
            }
            if (purchase.getRemainTimes() <= 0) {
                throw new BusinessException("套餐次数已用完");
            }
            
            record.setProjectId(purchase.getPackageId());
            record.setProjectName(purchase.getPackageName());
            record.setPurchaseId(dto.getPurchaseId());
            record.setCardId(purchase.getMemberCardId());
            
            // 获取套餐描述（用于消费关怀）
            ServicePackage servicePackage = servicePackageService.getById(purchase.getPackageId());
            if (servicePackage != null && StringUtils.hasText(servicePackage.getDescription())) {
                projectDescription = servicePackage.getDescription();
            }
            
            // 次卡消费：扣除套餐次数
            int consumeTimes = dto.getConsumeTimes() != null ? dto.getConsumeTimes() : 1;
            if (consumeTimes > purchase.getRemainTimes()) {
                throw new BusinessException("套餐剩余次数不足，当前剩余：" + purchase.getRemainTimes() + " 次");
            }
            
            // 更新套餐剩余次数
            PackagePurchase updatePurchase = new PackagePurchase();
            updatePurchase.setId(dto.getPurchaseId());
            updatePurchase.setRemainTimes(purchase.getRemainTimes() - consumeTimes);
            packagePurchaseMapper.updateById(updatePurchase);
            
            // 更新会员卡剩余次数
            if (purchase.getMemberCardId() != null) {
                MemberCard card = memberCardService.getById(purchase.getMemberCardId());
                if (card != null) {
                    MemberCard updateCard = new MemberCard();
                    updateCard.setId(card.getId());
                    updateCard.setRemainCount(card.getRemainCount() - consumeTimes);
                    memberCardService.updateById(updateCard);
                }
            }
            
            // 次卡消费：计算单次价格计入营收（套餐总价 ÷ 总次数 × 消费次数）
            BigDecimal unitPrice = purchase.getPrice().divide(
                new BigDecimal(purchase.getTimes()), 
                2, 
                java.math.RoundingMode.HALF_UP
            );
            BigDecimal consumeAmount = unitPrice.multiply(new BigDecimal(consumeTimes));
            record.setAmount(consumeAmount);  // 记录消费金额用于营收统计
            record.setPayAmount(BigDecimal.ZERO);  // 实际支付为0
            
        } else if ("product".equals(dto.getConsumeMode())) {
            // 购买产品模式
            if (dto.getProductId() == null) {
                throw new BusinessException("请选择产品");
            }
            
            Product product = productMapper.selectById(dto.getProductId());
            if (product == null) {
                throw new BusinessException("产品不存在");
            }
            
            int quantity = dto.getQuantity() != null ? dto.getQuantity() : 1;
            if (product.getStock() < quantity) {
                throw new BusinessException("产品库存不足，当前库存：" + product.getStock());
            }
            
            record.setProductId(dto.getProductId());
            record.setProductName(product.getName());
            record.setQuantity(quantity);
            record.setAmount(dto.getAmount() != null ? dto.getAmount() : product.getSalePrice().multiply(new BigDecimal(quantity)));
            record.setPayAmount(dto.getPayAmount() != null ? dto.getPayAmount() : record.getAmount());
            record.setConsumeType("product");
            
            // 扣减库存
            Product updateProduct = new Product();
            updateProduct.setId(dto.getProductId());
            updateProduct.setStock(product.getStock() - quantity);
            productMapper.updateById(updateProduct);
            
            log.info("产品销售成功，产品：{}，数量：{}，扣减库存后剩余：{}", 
                    product.getName(), quantity, product.getStock() - quantity);
            
        } else {
            // 单独项目模式
            if (dto.getProjectId() == null) {
                throw new BusinessException("请选择服务项目");
            }
            
            ServiceProject project = serviceProjectService.getById(dto.getProjectId());
            if (project == null) {
                throw new BusinessException("项目不存在");
            }
            
            record.setProjectId(dto.getProjectId());
            record.setProjectName(project.getName());
            record.setAmount(dto.getAmount());
            record.setPayAmount(dto.getPayAmount() != null ? dto.getPayAmount() : dto.getAmount());
            
            // 获取项目描述（用于消费关怀）
            if (StringUtils.hasText(project.getDescription())) {
                projectDescription = project.getDescription();
            }
            
            // 处理储值消费
            if ("stored".equals(dto.getConsumeType())) {
                if (dto.getCardId() != null) {
                    MemberCard card = memberCardService.getById(dto.getCardId());
                    if (card == null) {
                        throw new BusinessException("会员卡不存在");
                    }
                    if (card.getRemainAmount().compareTo(dto.getAmount()) < 0) {
                        throw new BusinessException("会员卡余额不足");
                    }
                    record.setCardId(dto.getCardId());
                    
                    // 扣减余额
                    MemberCard updateCard = new MemberCard();
                    updateCard.setId(dto.getCardId());
                    updateCard.setRemainAmount(card.getRemainAmount().subtract(dto.getAmount()));
                    memberCardService.updateById(updateCard);
                    
                    // 同步更新客户余额
                    customer.setBalance(card.getRemainAmount().subtract(dto.getAmount()));
                    customerService.updateById(customer);
                }
            }
        }
        
        // 处理产品/耗材消耗（单独项目和套餐模式）
        if (("project".equals(dto.getConsumeMode()) || "package".equals(dto.getConsumeMode())) 
                && StringUtils.hasText(dto.getProductConsumptions())) {
            record.setProductConsumptions(dto.getProductConsumptions());
            
            // 解析并扣减库存
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<java.util.Map<String, Object>> consumptions = mapper.readValue(
                        dto.getProductConsumptions(), 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
                );
                
                for (java.util.Map<String, Object> item : consumptions) {
                    Long productId = Long.valueOf(item.get("productId").toString());
                    Integer quantity = Integer.valueOf(item.get("quantity").toString());
                    
                    Product product = productMapper.selectById(productId);
                    if (product != null && product.getStock() >= quantity) {
                        Product updateProduct = new Product();
                        updateProduct.setId(productId);
                        updateProduct.setStock(product.getStock() - quantity);
                        productMapper.updateById(updateProduct);
                        
                        log.info("产品消耗扣减库存，产品：{}，数量：{}，扣减后剩余：{}", 
                                product.getName(), quantity, product.getStock() - quantity);
                    }
                }
            } catch (Exception e) {
                log.error("解析产品消耗记录失败", e);
            }
        }
        
        this.save(record);
        
        // 更新客户消费信息（所有消费类型都需要更新）
        if (record.getAmount() != null && record.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            updateCustomerConsumeInfo(dto.getCustomerId(), record.getAmount());
        }
        
        // 创建消费关怀记录
        createConsumeCareRecord(record, customer, projectDescription);
    }

    @Override
    public List<ConsumeRecord> getByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<ConsumeRecord>()
                .eq(ConsumeRecord::getCustomerId, customerId)
                .orderByDesc(ConsumeRecord::getConsumeTime));
    }

    @Override
    public Map<String, Object> getRevenueStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 总营收
        BigDecimal totalRevenue = this.getBaseMapper().selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .between(ConsumeRecord::getConsumeTime, startDate, endDate)
        ).stream().map(ConsumeRecord::getPayAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        result.put("totalRevenue", totalRevenue);
        
        return result;
    }

    @Override
    public BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        return this.getBaseMapper().selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .between(ConsumeRecord::getConsumeTime, startOfDay, endOfDay)
        ).stream().map(ConsumeRecord::getPayAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 生成消费单号
     */
    private String generateConsumeNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%04d", new Random().nextInt(10000));
        return "CO" + dateStr + randomStr;
    }

    /**
     * 更新客户消费信息
     */
    private void updateCustomerConsumeInfo(Long customerId, BigDecimal amount) {
        Customer customer = customerService.getById(customerId);
        if (customer != null) {
            Customer updateCustomer = new Customer();
            updateCustomer.setId(customerId);
            updateCustomer.setTotalAmount(customer.getTotalAmount().add(amount));
            updateCustomer.setConsumeCount(customer.getConsumeCount() + 1);
            updateCustomer.setLastConsumeTime(LocalDateTime.now());
            customerService.updateById(updateCustomer);
            
            // 更新客户分类
            customerService.updateCustomerCategory(customerId);
        }
    }
    
    /**
     * 创建消费关怀记录
     * 消费后根据配置的间隔天数创建回访任务
     */
    private void createConsumeCareRecord(ConsumeRecord record, Customer customer, String projectDescription) {
        try {
            // 获取回访间隔天数配置
            String intervalValue = sysDictService.getDictValue("consume_care_interval");
            int intervalDays = 3; // 默认3天
            if (StringUtils.hasText(intervalValue)) {
                try {
                    intervalDays = Integer.parseInt(intervalValue);
                } catch (NumberFormatException e) {
                    log.warn("消费回访间隔天数配置值无效: {}", intervalValue);
                }
            }
            
            // 计划回访日期 = 消费日期 + 间隔天数
            LocalDate planDate = LocalDate.now().plusDays(intervalDays);
            
            // 创建消费关怀记录
            CustomerCare care = new CustomerCare();
            care.setCustomerId(record.getCustomerId());
            care.setCustomerName(record.getCustomerName());
            care.setConsumeRecordId(record.getId());
            care.setProjectId(record.getProjectId());
            care.setProjectName(record.getProjectName());
            care.setProjectDescription(projectDescription);
            care.setCustomerPhone(customer.getPhone());
            care.setCareType("consume_care");
            care.setContent("消费后回访：" + record.getProjectName());
            care.setPlanDate(planDate);
            care.setStatus("待执行");
            care.setCreateTime(LocalDateTime.now());
            
            customerCareMapper.insert(care);
            log.info("创建消费关怀记录成功，客户：{}，项目：{}，计划回访日期：{}", 
                    customer.getName(), record.getProjectName(), planDate);
        } catch (Exception e) {
            log.error("创建消费关怀记录失败", e);
            // 不抛出异常，避免影响消费流程
        }
    }
}
