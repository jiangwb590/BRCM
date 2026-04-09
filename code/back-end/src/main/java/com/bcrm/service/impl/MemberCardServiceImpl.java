package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.MemberCardDTO;
import com.bcrm.entity.Customer;
import com.bcrm.entity.MemberCard;
import com.bcrm.entity.ServiceProject;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.MemberCardMapper;
import com.bcrm.service.CustomerService;
import com.bcrm.service.MemberCardService;
import com.bcrm.service.ServiceProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 会员卡服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCardServiceImpl extends ServiceImpl<MemberCardMapper, MemberCard> implements MemberCardService {

    private final CustomerService customerService;
    private final ServiceProjectService serviceProjectService;

    @Override
    public Page<MemberCard> pageCards(PageRequest pageRequest, MemberCard query) {
        Page<MemberCard> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getCustomerName()), MemberCard::getCustomerName, query.getCustomerName());
        wrapper.eq(query.getCustomerId() != null, MemberCard::getCustomerId, query.getCustomerId());
        wrapper.eq(StringUtils.hasText(query.getCardType()), MemberCard::getCardType, query.getCardType());
        wrapper.eq(query.getStatus() != null, MemberCard::getStatus, query.getStatus());
        wrapper.orderByDesc(MemberCard::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public List<MemberCard> getByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<MemberCard>()
                .eq(MemberCard::getCustomerId, customerId)
                .eq(MemberCard::getStatus, 1)
                .orderByDesc(MemberCard::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCard(MemberCardDTO dto) {
        // 查询客户信息
        Customer customer = customerService.getById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        
        MemberCard card = new MemberCard();
        card.setCardNo(generateCardNo());
        card.setCustomerId(dto.getCustomerId());
        card.setCustomerName(customer.getName());
        card.setCardType(dto.getCardType());
        card.setPurchaseAmount(dto.getPurchaseAmount());
        card.setValidStartDate(dto.getValidStartDate() != null ? dto.getValidStartDate() : LocalDate.now());
        card.setValidEndDate(dto.getValidEndDate() != null ? dto.getValidEndDate() : LocalDate.now().plusYears(1));
        card.setStatus(1);
        
        if ("次卡".equals(dto.getCardType())) {
            if (dto.getProjectId() == null) {
                throw new BusinessException("次卡必须关联项目");
            }
            ServiceProject project = serviceProjectService.getById(dto.getProjectId());
            if (project == null) {
                throw new BusinessException("项目不存在");
            }
            card.setProjectId(dto.getProjectId());
            card.setProjectName(project.getName());
            card.setTotalCount(dto.getTotalCount());
            card.setRemainCount(dto.getTotalCount());
        } else if ("储值卡".equals(dto.getCardType())) {
            card.setTotalAmount(dto.getTotalAmount());
            card.setRemainAmount(dto.getTotalAmount());
            
            // 同步更新客户表的余额字段
            BigDecimal newBalance = customer.getBalance() != null 
                    ? customer.getBalance().add(dto.getTotalAmount()) 
                    : dto.getTotalAmount();
            customer.setBalance(newBalance);
            customerService.updateById(customer);
        }
        
        this.save(card);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long cardId, BigDecimal amount) {
        MemberCard card = this.getById(cardId);
        if (card == null) {
            throw new BusinessException("会员卡不存在");
        }
        
        if (!"储值卡".equals(card.getCardType())) {
            throw new BusinessException("只有储值卡可以充值");
        }
        
        MemberCard updateCard = new MemberCard();
        updateCard.setId(cardId);
        updateCard.setTotalAmount(card.getTotalAmount().add(amount));
        updateCard.setRemainAmount(card.getRemainAmount().add(amount));
        this.updateById(updateCard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableCard(Long cardId) {
        MemberCard card = this.getById(cardId);
        if (card == null) {
            throw new BusinessException("会员卡不存在");
        }
        
        MemberCard updateCard = new MemberCard();
        updateCard.setId(cardId);
        updateCard.setStatus(0);
        this.updateById(updateCard);
    }

    /**
     * 生成卡号
     */
    private String generateCardNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", new Random().nextInt(1000000));
        return "MC" + dateStr + randomStr;
    }
}
