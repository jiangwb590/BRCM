package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.PackagePurchaseDTO;
import com.bcrm.dto.ServicePackageDTO;
import com.bcrm.entity.*;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.*;
import com.bcrm.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 项目套餐服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicePackageServiceImpl extends ServiceImpl<ServicePackageMapper, ServicePackage> implements ServicePackageService {

    private final PackageItemMapper packageItemMapper;
    private final PackagePurchaseMapper packagePurchaseMapper;
    private final CustomerMapper customerMapper;
    private final MemberCardMapper memberCardMapper;

    @Override
    public Page<ServicePackage> pagePackages(PageRequest pageRequest, ServicePackage query) {
        LambdaQueryWrapper<ServicePackage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getPackageName())) {
            wrapper.like(ServicePackage::getPackageName, query.getPackageName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ServicePackage::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ServicePackage::getCreateTime);
        return page(pageRequest.toPage(), wrapper);
    }

    @Override
    public List<ServicePackage> listActivePackages() {
        LambdaQueryWrapper<ServicePackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServicePackage::getStatus, 1);
        wrapper.orderByAsc(ServicePackage::getSort);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPackage(ServicePackageDTO dto) {
        ServicePackage entity = new ServicePackage();
        BeanUtils.copyProperties(dto, entity);
        entity.setServiceTimes(0);
        save(entity);
        
        // 保存套餐项目
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ServicePackageDTO.PackageItemDTO itemDTO : dto.getItems()) {
                PackageItem item = new PackageItem();
                item.setPackageId(entity.getId());
                item.setServiceId(itemDTO.getServiceId());
                item.setServiceName(itemDTO.getServiceName());
                item.setTimes(itemDTO.getTimes() != null ? itemDTO.getTimes() : 1);
                packageItemMapper.insert(item);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePackage(ServicePackageDTO dto) {
        ServicePackage entity = getById(dto.getId());
        if (entity == null) {
            throw new BusinessException("套餐不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        
        // 删除原套餐项目
        packageItemMapper.delete(new LambdaQueryWrapper<PackageItem>()
                .eq(PackageItem::getPackageId, dto.getId()));
        
        // 保存新套餐项目
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ServicePackageDTO.PackageItemDTO itemDTO : dto.getItems()) {
                PackageItem item = new PackageItem();
                item.setPackageId(dto.getId());
                item.setServiceId(itemDTO.getServiceId());
                item.setServiceName(itemDTO.getServiceName());
                item.setTimes(itemDTO.getTimes() != null ? itemDTO.getTimes() : 1);
                packageItemMapper.insert(item);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePackage(Long id) {
        removeById(id);
        // 删除关联项目
        packageItemMapper.delete(new LambdaQueryWrapper<PackageItem>()
                .eq(PackageItem::getPackageId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ServicePackage entity = getById(id);
        if (entity == null) {
            throw new BusinessException("套餐不存在");
        }
        entity.setStatus(status);
        updateById(entity);
    }

    @Override
    public List<PackageItem> getPackageItems(Long packageId) {
        return packageItemMapper.getByPackageId(packageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purchasePackage(PackagePurchaseDTO dto) {
        // 查询客户
        Customer customer = customerMapper.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        // 查询套餐
        ServicePackage pkg = this.getById(dto.getPackageId());
        if (pkg == null) {
            throw new BusinessException("套餐不存在");
        }
        if (pkg.getStatus() != 1) {
            throw new BusinessException("套餐已下架");
        }

        int quantity = dto.getQuantity() != null ? dto.getQuantity() : 1;
        int totalTimes = pkg.getTimes() * quantity;

        // 查询客户储值卡余额
        LambdaQueryWrapper<MemberCard> cardWrapper = new LambdaQueryWrapper<>();
        cardWrapper.eq(MemberCard::getCustomerId, dto.getCustomerId())
                   .eq(MemberCard::getCardType, "储值卡");
        MemberCard storedCard = memberCardMapper.selectOne(cardWrapper);
        BigDecimal customerBalance = storedCard != null ? storedCard.getRemainAmount() : BigDecimal.ZERO;

        // 根据储值余额判断使用原价还是卡扣价
        // 储值余额为0时使用原价，否则使用卡扣价（price字段）
        BigDecimal unitPrice;
        if (customerBalance.compareTo(BigDecimal.ZERO) == 0) {
            // 余额为0，使用原价
            unitPrice = pkg.getOriginalPrice() != null ? pkg.getOriginalPrice() : pkg.getPrice();
        } else {
            // 余额大于0，使用卡扣价
            unitPrice = pkg.getPrice();
        }
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        BigDecimal balanceBefore = BigDecimal.ZERO;
        BigDecimal balanceAfter = BigDecimal.ZERO;

        // 如果使用储值卡支付
        if ("balance".equals(dto.getPayMethod()) || dto.getCardId() != null) {
            if (storedCard == null) {
                throw new BusinessException("客户没有储值卡，请先充值");
            }
            if (storedCard.getRemainAmount().compareTo(totalAmount) < 0) {
                throw new BusinessException("储值卡余额不足，当前余额：" + storedCard.getRemainAmount());
            }

            balanceBefore = storedCard.getRemainAmount();
            storedCard.setRemainAmount(storedCard.getRemainAmount().subtract(totalAmount));
            memberCardMapper.updateById(storedCard);
            balanceAfter = storedCard.getRemainAmount();
            dto.setCardId(storedCard.getId());
            
            // 同步更新客户表的余额字段
            customer.setBalance(balanceAfter);
            customerMapper.updateById(customer);
        }

        // 创建次卡（会员卡）
        MemberCard timesCard = new MemberCard();
        timesCard.setCardNo(generateCardNo("TC"));
        timesCard.setCustomerId(dto.getCustomerId());
        timesCard.setCustomerName(customer.getName());
        timesCard.setCardType("次卡");
        timesCard.setProjectId(pkg.getId());
        timesCard.setProjectName(pkg.getPackageName());
        timesCard.setTotalCount(totalTimes);
        timesCard.setRemainCount(totalTimes);
        timesCard.setPurchaseAmount(totalAmount);
        timesCard.setValidStartDate(LocalDate.now());
        timesCard.setValidEndDate(LocalDate.now().plusDays(pkg.getValidDays()));
        timesCard.setStatus(1);
        memberCardMapper.insert(timesCard);

        // 创建购买记录
        PackagePurchase purchase = new PackagePurchase();
        purchase.setPurchaseNo(generatePurchaseNo());
        purchase.setCustomerId(dto.getCustomerId());
        purchase.setCustomerName(customer.getName());
        purchase.setPackageId(pkg.getId());
        purchase.setPackageName(pkg.getPackageName());
        purchase.setTimes(totalTimes);
        purchase.setRemainTimes(totalTimes);
        purchase.setPrice(pkg.getPrice());
        purchase.setTotalAmount(totalAmount);
        purchase.setPayMethod(dto.getPayMethod());
        purchase.setCardId(dto.getCardId());
        purchase.setBalanceBefore(balanceBefore);
        purchase.setBalanceAfter(balanceAfter);
        purchase.setMemberCardId(timesCard.getId());
        purchase.setValidStartDate(timesCard.getValidStartDate());
        purchase.setValidEndDate(timesCard.getValidEndDate());
        purchase.setStatus(1);
        purchase.setRemark(dto.getRemark());
        packagePurchaseMapper.insert(purchase);

        log.info("客户[{}]购买套餐[{}]成功，数量：{}，总金额：{}", 
                customer.getName(), pkg.getPackageName(), quantity, totalAmount);
    }

    @Override
    public Page<PackagePurchase> pagePurchases(PageRequest pageRequest, Long customerId) {
        Page<PackagePurchase> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<PackagePurchase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customerId != null, PackagePurchase::getCustomerId, customerId);
        wrapper.orderByDesc(PackagePurchase::getCreateTime);
        return packagePurchaseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<PackagePurchase> getCustomerActivePurchases(Long customerId) {
        LambdaQueryWrapper<PackagePurchase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PackagePurchase::getCustomerId, customerId);
        wrapper.gt(PackagePurchase::getRemainTimes, 0);  // 有剩余次数
        wrapper.eq(PackagePurchase::getStatus, 1);  // 状态正常
        wrapper.orderByDesc(PackagePurchase::getCreateTime);
        return packagePurchaseMapper.selectList(wrapper);
    }

    /**
     * 生成购买单号
     */
    private String generatePurchaseNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "PP" + date + random;
    }

    /**
     * 生成卡号
     */
    private String generateCardNo(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return prefix + date + random;
    }
}