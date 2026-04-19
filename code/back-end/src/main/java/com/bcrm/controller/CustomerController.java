package com.bcrm.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.annotation.HasPermission;
import com.bcrm.annotation.OperLog;
import com.bcrm.aspect.OperLogContextHolder;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.CustomerDTO;
import com.bcrm.entity.Customer;
import com.bcrm.service.CustomerService;
import com.bcrm.vo.CustomerExportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户管理控制器
 */
@Tag(name = "客户管理")
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 分页查询客户列表
     */
    @Operation(summary = "分页查询客户列表")
    @GetMapping("/page")
    public Result<PageResult<Customer>> page(PageRequest pageRequest, Customer query) {
        Page<Customer> page = customerService.pageCustomers(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取所有客户列表（用于下拉选择）
     */
    @Operation(summary = "获取所有客户列表")
    @GetMapping("/all")
    public Result<List<Customer>> getAll() {
        List<Customer> list = customerService.list();
        return Result.success(list);
    }

    /**
     * 根据ID查询客户
     */
    @Operation(summary = "根据ID查询客户")
    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        return Result.success(customer);
    }

    /**
     * 新增客户
     */
    @Operation(summary = "新增客户")
    @HasPermission("customer:add")
    @OperLog(title = "客户管理", businessType = 1, targetType = "customer")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CustomerDTO dto) {
        customerService.addCustomer(dto);
        return Result.success();
    }

    /**
     * 修改客户
     */
    @Operation(summary = "修改客户")
    @HasPermission("customer:edit")
    @OperLog(title = "客户管理", businessType = 2, targetType = "customer")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CustomerDTO dto) {
        customerService.updateCustomer(dto);
        return Result.success();
    }

    /**
     * 删除客户
     */
    @Operation(summary = "删除客户")
    @HasPermission("customer:delete")
    @OperLog(title = "客户管理", businessType = 3, targetType = "customer")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        String customerName = customer != null ? customer.getName() : null;
        customerService.deleteCustomer(id);
        OperLogContextHolder.setTargetName(customerName);
        return Result.success();
    }

    /**
     * 批量删除客户
     */
    @Operation(summary = "批量删除客户")
    @OperLog(title = "客户管理", businessType = 3, targetType = "customer")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody Long[] ids) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            Customer customer = customerService.getById(ids[i]);
            if (customer != null) {
                if (i > 0) names.append(",");
                names.append(customer.getName());
            }
            customerService.deleteCustomer(ids[i]);
        }
        OperLogContextHolder.setTargetName(names.toString());
        return Result.success();
    }

    /**
     * 获取客户来源统计
     */
    @Operation(summary = "获取客户来源统计")
    @GetMapping("/statistics/source")
    public Result<?> getSourceStatistics() {
        return Result.success(customerService.getSourceStatistics());
    }

    /**
     * 获取客户分类统计
     */
    @Operation(summary = "获取客户分类统计")
    @GetMapping("/statistics/category")
    public Result<?> getCategoryStatistics() {
        return Result.success(customerService.getCategoryStatistics());
    }

    /**
     * 导出客户数据
     */
    @Operation(summary = "导出客户数据")
    @GetMapping("/export")
    public void export(Customer query, HttpServletResponse response) throws IOException {
        // 获取客户列表
        List<Customer> customers = customerService.getCustomerListForExport(query);
        
        // 转换为导出数据
        List<CustomerExportVO> exportList = customers.stream().map(customer -> {
            CustomerExportVO vo = new CustomerExportVO();
            vo.setName(customer.getName());
            vo.setGender(customer.getGender() != null ? (customer.getGender() == 1 ? "男" : "女") : "未知");
            vo.setPhone(customer.getPhone());
            vo.setBirthday(customer.getBirthday());
            vo.setCategory(customer.getCategory());
            vo.setSource(customer.getSource());
            vo.setTotalAmount(customer.getTotalAmount());
            vo.setConsumeCount(customer.getConsumeCount());
            vo.setLastConsumeTime(customer.getLastConsumeTime());
            vo.setBalance(customer.getBalance());
            vo.setRemark(customer.getRemark());
            vo.setCreateTime(customer.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        
        // 设置响应头（必须先设置，再获取输出流）
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        String fileName = URLEncoder.encode("客户数据_" + DateUtil.today(), StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
        
        // 创建Excel写入器，使用VO类上的@Alias注解作为表头
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(exportList, true);
        
        // 写入响应流并关闭
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }
}