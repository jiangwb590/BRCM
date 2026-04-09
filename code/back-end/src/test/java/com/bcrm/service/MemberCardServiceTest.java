package com.bcrm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.MemberCardDTO;
import com.bcrm.entity.Customer;
import com.bcrm.entity.MemberCard;
import com.bcrm.entity.ServiceProject;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.MemberCardMapper;
import com.bcrm.service.impl.MemberCardServiceImpl;
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
 * 会员卡服务测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@ExtendWith(MockitoExtension.class)
class MemberCardServiceTest {

    @Mock
    private MemberCardMapper memberCardMapper;

    @Mock
    private CustomerService customerService;

    @Mock
    private ServiceProjectService serviceProjectService;

    @InjectMocks
    private MemberCardServiceImpl memberCardService;

    private MemberCard testCard;
    private MemberCardDTO cardDTO;
    private Customer testCustomer;
    private ServiceProject testProject;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("张三");

        testProject = new ServiceProject();
        testProject.setId(1L);
        testProject.setName("面部护理");

        testCard = new MemberCard();
        testCard.setId(1L);
        testCard.setCardNo("MC202603140001");
        testCard.setCustomerId(1L);
        testCard.setCustomerName("张三");
        testCard.setCardType("储值卡");
        testCard.setTotalAmount(new BigDecimal("5000"));
        testCard.setRemainAmount(new BigDecimal("3000"));
        testCard.setPurchaseAmount(new BigDecimal("5000"));
        testCard.setValidStartDate(LocalDate.now());
        testCard.setValidEndDate(LocalDate.now().plusYears(1));
        testCard.setStatus(1);
        testCard.setDeleted(0);
        testCard.setCreateTime(LocalDateTime.now());

        cardDTO = new MemberCardDTO();
        cardDTO.setCustomerId(1L);
        cardDTO.setCardType("储值卡");
        cardDTO.setTotalAmount(new BigDecimal("5000"));
        cardDTO.setPurchaseAmount(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("分页查询会员卡测试")
    void testPageCards() {
        // 准备测试数据
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(1);
        pageRequest.setSize(10);

        Page<MemberCard> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testCard));
        page.setTotal(1);

        when(memberCardMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行测试
        Page<MemberCard> result = memberCardService.pageCards(pageRequest, new MemberCard());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("张三", result.getRecords().get(0).getCustomerName());
    }

    @Test
    @DisplayName("查询客户会员卡测试")
    void testGetByCustomerId() {
        // 准备测试数据
        when(memberCardMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testCard));

        // 执行测试
        List<MemberCard> result = memberCardService.getByCustomerId(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("开储值卡测试")
    void testCreateStoredValueCard() {
        // 准备测试数据
        when(customerService.getById(1L)).thenReturn(testCustomer);
        when(memberCardMapper.insert(any(MemberCard.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> memberCardService.createCard(cardDTO));

        // 验证insert被调用
        verify(memberCardMapper, times(1)).insert(any(MemberCard.class));
    }

    @Test
    @DisplayName("开次卡测试")
    void testCreateTimeCard() {
        // 准备测试数据
        cardDTO.setCardType("次卡");
        cardDTO.setProjectId(1L);
        cardDTO.setTotalCount(10);
        when(customerService.getById(1L)).thenReturn(testCustomer);
        when(serviceProjectService.getById(1L)).thenReturn(testProject);
        when(memberCardMapper.insert(any(MemberCard.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> memberCardService.createCard(cardDTO));

        // 验证insert被调用
        verify(memberCardMapper, times(1)).insert(any(MemberCard.class));
    }

    @Test
    @DisplayName("开次卡-未关联项目测试")
    void testCreateTimeCardWithoutProject() {
        // 准备测试数据
        cardDTO.setCardType("次卡");
        cardDTO.setTotalCount(10);
        when(customerService.getById(1L)).thenReturn(testCustomer);

        // 执行测试
        assertThrows(BusinessException.class, () -> memberCardService.createCard(cardDTO));
    }

    @Test
    @DisplayName("开卡-客户不存在测试")
    void testCreateCardCustomerNotFound() {
        // 准备测试数据
        when(customerService.getById(1L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> memberCardService.createCard(cardDTO));
    }

    @Test
    @DisplayName("充值成功测试")
    void testRechargeSuccess() {
        // 准备测试数据
        when(memberCardMapper.selectById(1L)).thenReturn(testCard);
        when(memberCardMapper.updateById(any(MemberCard.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> memberCardService.recharge(1L, new BigDecimal("1000")));

        // 验证update被调用
        verify(memberCardMapper, times(1)).updateById(any(MemberCard.class));
    }

    @Test
    @DisplayName("充值-会员卡不存在测试")
    void testRechargeCardNotFound() {
        // 准备测试数据
        when(memberCardMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> memberCardService.recharge(1L, new BigDecimal("1000")));
    }

    @Test
    @DisplayName("充值-非储值卡测试")
    void testRechargeNotStoredValueCard() {
        // 准备测试数据
        testCard.setCardType("次卡");
        when(memberCardMapper.selectById(1L)).thenReturn(testCard);

        // 执行测试
        assertThrows(BusinessException.class, () -> memberCardService.recharge(1L, new BigDecimal("1000")));
    }

    @Test
    @DisplayName("作废会员卡测试")
    void testDisableCard() {
        // 准备测试数据
        when(memberCardMapper.selectById(1L)).thenReturn(testCard);
        when(memberCardMapper.updateById(any(MemberCard.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> memberCardService.disableCard(1L));

        // 验证update被调用
        verify(memberCardMapper, times(1)).updateById(any(MemberCard.class));
    }

    @Test
    @DisplayName("作废会员卡-卡不存在测试")
    void testDisableCardNotFound() {
        // 准备测试数据
        when(memberCardMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> memberCardService.disableCard(1L));
    }
}
