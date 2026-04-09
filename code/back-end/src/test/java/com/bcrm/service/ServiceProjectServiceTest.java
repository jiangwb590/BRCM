package com.bcrm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.ServiceProjectDTO;
import com.bcrm.entity.ServiceProject;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.ServiceProjectMapper;
import com.bcrm.service.impl.ServiceProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 服务项目测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@ExtendWith(MockitoExtension.class)
class ServiceProjectServiceTest {

    @Mock
    private ServiceProjectMapper serviceProjectMapper;

    @InjectMocks
    private ServiceProjectServiceImpl serviceProjectService;

    private ServiceProject testProject;
    private ServiceProjectDTO projectDTO;

    @BeforeEach
    void setUp() {
        testProject = new ServiceProject();
        testProject.setId(1L);
        testProject.setProjectCode("PRJ001");
        testProject.setName("面部护理");
        testProject.setDuration(60);
        testProject.setPrice(new BigDecimal("298"));
        testProject.setMemberPrice(new BigDecimal("268"));
        testProject.setStatus(1);
        testProject.setSort(1);
        testProject.setIsPackage(0);
        testProject.setIsCardProject(1);
        testProject.setDeleted(0);
        testProject.setCreateTime(LocalDateTime.now());

        projectDTO = new ServiceProjectDTO();
        projectDTO.setName("身体按摩");
        projectDTO.setDuration(90);
        projectDTO.setPrice(new BigDecimal("398"));
        projectDTO.setStatus(1);
    }

    @Test
    @DisplayName("分页查询项目测试")
    void testPageProjects() {
        // 准备测试数据
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(1);
        pageRequest.setSize(10);

        Page<ServiceProject> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testProject));
        page.setTotal(1);

        when(serviceProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行测试
        Page<ServiceProject> result = serviceProjectService.pageProjects(pageRequest, new ServiceProject());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("面部护理", result.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("查询所有上架项目测试")
    void testListActiveProjects() {
        // 准备测试数据
        when(serviceProjectMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testProject));

        // 执行测试
        List<ServiceProject> result = serviceProjectService.listActiveProjects();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getStatus());
    }

    @Test
    @DisplayName("新增项目测试")
    void testAddProject() {
        // 准备测试数据
        when(serviceProjectMapper.insert(any(ServiceProject.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> serviceProjectService.addProject(projectDTO));

        // 验证insert被调用
        verify(serviceProjectMapper, times(1)).insert(any(ServiceProject.class));
    }

    @Test
    @DisplayName("修改项目成功测试")
    void testUpdateProjectSuccess() {
        // 准备测试数据
        projectDTO.setId(1L);
        when(serviceProjectMapper.selectById(1L)).thenReturn(testProject);
        when(serviceProjectMapper.updateById(any(ServiceProject.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> serviceProjectService.updateProject(projectDTO));

        // 验证update被调用
        verify(serviceProjectMapper, times(1)).updateById(any(ServiceProject.class));
    }

    @Test
    @DisplayName("修改项目-项目不存在测试")
    void testUpdateProjectNotFound() {
        // 准备测试数据
        projectDTO.setId(999L);
        when(serviceProjectMapper.selectById(999L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> serviceProjectService.updateProject(projectDTO));
    }

    @Test
    @DisplayName("删除项目测试")
    void testDeleteProject() {
        // 准备测试数据
        when(serviceProjectMapper.deleteById(1L)).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> serviceProjectService.deleteProject(1L));

        // 验证delete被调用
        verify(serviceProjectMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("修改项目状态-上架测试")
    void testUpdateStatusToActive() {
        // 准备测试数据
        when(serviceProjectMapper.selectById(1L)).thenReturn(testProject);
        when(serviceProjectMapper.updateById(any(ServiceProject.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> serviceProjectService.updateStatus(1L, 1));
    }

    @Test
    @DisplayName("修改项目状态-下架测试")
    void testUpdateStatusToInactive() {
        // 准备测试数据
        when(serviceProjectMapper.selectById(1L)).thenReturn(testProject);
        when(serviceProjectMapper.updateById(any(ServiceProject.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> serviceProjectService.updateStatus(1L, 0));
    }

    @Test
    @DisplayName("修改项目状态-项目不存在测试")
    void testUpdateStatusProjectNotFound() {
        // 准备测试数据
        when(serviceProjectMapper.selectById(999L)).thenReturn(null);

        // 执行测试
        assertThrows(BusinessException.class, () -> serviceProjectService.updateStatus(999L, 1));
    }
}
