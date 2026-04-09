package com.bcrm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.AppointmentDTO;
import com.bcrm.entity.Appointment;
import com.bcrm.entity.Customer;
import com.bcrm.entity.ServiceProject;
import com.bcrm.entity.SysUser;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.AppointmentMapper;
import com.bcrm.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 预约服务测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private CustomerService customerService;

    @Mock
    private ServiceProjectService serviceProjectService;

    @Mock
    private SysUserService sysUserService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment testAppointment;
    private AppointmentDTO appointmentDTO;
    private Customer testCustomer;
    private ServiceProject testProject;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("张三");
        testCustomer.setPhone("13800138000");

        testProject = new ServiceProject();
        testProject.setId(1L);
        testProject.setName("面部护理");
        testProject.setDuration(60);

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setAppointmentNo("AP202603140001");
        testAppointment.setCustomerId(1L);
        testAppointment.setCustomerName("张三");
        testAppointment.setCustomerPhone("13800138000");
        testAppointment.setProjectId(1L);
        testAppointment.setProjectName("面部护理");
        testAppointment.setAppointmentDate(LocalDate.now());
        testAppointment.setStartTime(LocalTime.of(10, 0));
        testAppointment.setEndTime(LocalTime.of(11, 0));
        testAppointment.setStatus("待确认");
        testAppointment.setDeleted(0);
        testAppointment.setCreateTime(LocalDateTime.now());

        appointmentDTO = new AppointmentDTO();
        appointmentDTO.setCustomerId(1L);
        appointmentDTO.setProjectId(1L);
        appointmentDTO.setAppointmentDate(LocalDate.now());
        appointmentDTO.setStartTime(LocalTime.of(10, 0));
        appointmentDTO.setEndTime(LocalTime.of(11, 0));
    }

    @Test
    @DisplayName("分页查询预约测试")
    void testPageAppointments() {
        // 准备测试数据
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(1);
        pageRequest.setSize(10);

        Page<Appointment> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testAppointment));
        page.setTotal(1);

        when(appointmentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行测试
        Page<Appointment> result = appointmentService.pageAppointments(pageRequest, new Appointment());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("查询指定日期预约列表测试")
    void testGetByDate() {
        // 准备测试数据
        when(appointmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testAppointment));

        // 执行测试
        List<Appointment> result = appointmentService.getByDate(LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("新增预约成功测试")
    void testAddAppointmentSuccess() {
        // 准备测试数据
        when(customerService.getById(1L)).thenReturn(testCustomer);
        when(serviceProjectService.getById(1L)).thenReturn(testProject);
        when(appointmentMapper.insert(any(Appointment.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> appointmentService.addAppointment(appointmentDTO));

        // 验证insert被调用
        verify(appointmentMapper, times(1)).insert(any(Appointment.class));
    }

    @Test
    @DisplayName("新增预约-客户不存在测试")
    void testAddAppointmentCustomerNotFound() {
        // 准备测试数据
        when(customerService.getById(1L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> appointmentService.addAppointment(appointmentDTO));
    }

    @Test
    @DisplayName("新增预约-项目不存在测试")
    void testAddAppointmentProjectNotFound() {
        // 准备测试数据
        when(customerService.getById(1L)).thenReturn(testCustomer);
        when(serviceProjectService.getById(1L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> appointmentService.addAppointment(appointmentDTO));
    }

    @Test
    @DisplayName("确认预约测试")
    void testConfirmAppointment() {
        // 准备测试数据
        when(appointmentMapper.selectById(1L)).thenReturn(testAppointment);
        when(appointmentMapper.updateById(any(Appointment.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> appointmentService.confirmAppointment(1L));
    }

    @Test
    @DisplayName("取消预约测试")
    void testCancelAppointment() {
        // 准备测试数据
        when(appointmentMapper.selectById(1L)).thenReturn(testAppointment);
        when(appointmentMapper.updateById(any(Appointment.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> appointmentService.cancelAppointment(1L, "客户临时有事"));
    }

    @Test
    @DisplayName("取消预约-预约已完成测试")
    void testCancelAppointmentCompleted() {
        // 准备测试数据
        testAppointment.setStatus("已完成");
        when(appointmentMapper.selectById(1L)).thenReturn(testAppointment);

        // 执行测试
        assertThrows(BusinessException.class, () -> appointmentService.cancelAppointment(1L, "客户临时有事"));
    }

    @Test
    @DisplayName("开始服务测试")
    void testStartService() {
        // 准备测试数据
        testAppointment.setStatus("已确认");
        when(appointmentMapper.selectById(1L)).thenReturn(testAppointment);
        when(appointmentMapper.updateById(any(Appointment.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> appointmentService.startService(1L));
    }

    @Test
    @DisplayName("完成服务测试")
    void testCompleteService() {
        // 准备测试数据
        testAppointment.setStatus("服务中");
        when(appointmentMapper.selectById(1L)).thenReturn(testAppointment);
        when(appointmentMapper.updateById(any(Appointment.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> appointmentService.completeService(1L));
    }

    @Test
    @DisplayName("获取今日预约统计测试")
    void testGetTodayStatistics() {
        // 准备测试数据
        when(appointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // 执行测试
        Object result = appointmentService.getTodayStatistics();

        // 验证结果
        assertNotNull(result);
    }
}
