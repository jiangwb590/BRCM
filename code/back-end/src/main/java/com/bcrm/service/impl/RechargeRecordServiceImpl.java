package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.RechargeDTO;
import com.bcrm.entity.Customer;
import com.bcrm.entity.MemberCard;
import com.bcrm.entity.RechargeRecord;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.CustomerMapper;
import com.bcrm.mapper.MemberCardMapper;
import com.bcrm.mapper.RechargeRecordMapper;
import com.bcrm.service.RechargeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordMapper, RechargeRecord> implements RechargeRecordService {

    private final CustomerMapper customerMapper;
    private final MemberCardMapper memberCardMapper;

    @Override
    public Page<RechargeRecord> pageRecharges(PageRequest pageRequest, Long customerId) {
        Page<RechargeRecord> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customerId != null, RechargeRecord::getCustomerId, customerId);
        wrapper.orderByDesc(RechargeRecord::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(RechargeDTO dto) {
        // 查询客户
        Customer customer = customerMapper.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        BigDecimal giftAmount = dto.getGiftAmount() != null ? dto.getGiftAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = dto.getRechargeAmount().add(giftAmount);

        // 查询或创建储值卡
        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCard::getCustomerId, dto.getCustomerId())
               .eq(MemberCard::getCardType, "储值卡");
        MemberCard storedCard = memberCardMapper.selectOne(wrapper);

        BigDecimal balanceBefore = BigDecimal.ZERO;
        if (storedCard == null) {
            // 创建储值卡
            storedCard = new MemberCard();
            storedCard.setCardNo(generateCardNo("SV"));
            storedCard.setCustomerId(dto.getCustomerId());
            storedCard.setCustomerName(customer.getName());
            storedCard.setCardType("储值卡");
            storedCard.setTotalAmount(totalAmount);
            storedCard.setRemainAmount(totalAmount);
            storedCard.setValidStartDate(LocalDate.now());
            storedCard.setValidEndDate(LocalDate.now().plusYears(10));
            storedCard.setStatus(1);
            memberCardMapper.insert(storedCard);
        } else {
            balanceBefore = storedCard.getRemainAmount();
            // 更新储值卡余额
            storedCard.setTotalAmount(storedCard.getTotalAmount().add(totalAmount));
            storedCard.setRemainAmount(storedCard.getRemainAmount().add(totalAmount));
            memberCardMapper.updateById(storedCard);
        }

        // 创建充值记录
        RechargeRecord record = new RechargeRecord();
        record.setRechargeNo(generateRechargeNo());
        record.setCustomerId(dto.getCustomerId());
        record.setCustomerName(customer.getName());
        record.setCardId(storedCard.getId());
        record.setRechargeAmount(dto.getRechargeAmount());
        record.setGiftAmount(giftAmount);
        record.setTotalAmount(totalAmount);
        record.setPayMethod(dto.getPayMethod());
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(storedCard.getRemainAmount());
        record.setStatus(1);
        record.setRemark(dto.getRemark());
        this.save(record);

        // 同步更新客户表的余额字段
        customer.setBalance(storedCard.getRemainAmount());
        customerMapper.updateById(customer);

        log.info("客户[{}]充值成功，充值金额：{}，赠送金额：{}，余额：{}", 
                customer.getName(), dto.getRechargeAmount(), giftAmount, storedCard.getRemainAmount());
    }

    /**
     * 生成充值单号
     */
    private String generateRechargeNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "RC" + date + random;
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
