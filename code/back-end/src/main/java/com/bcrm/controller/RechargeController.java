package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.annotation.OperLog;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.RechargeDTO;
import com.bcrm.entity.RechargeRecord;
import com.bcrm.service.RechargeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "充值管理")
@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final RechargeRecordService rechargeRecordService;

    @Operation(summary = "分页查询充值记录")
    @GetMapping("/page")
    public Result<PageResult<RechargeRecord>> page(PageRequest pageRequest, @RequestParam(required = false) Long customerId) {
        Page<RechargeRecord> page = rechargeRecordService.pageRecharges(pageRequest, customerId);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "充值")
    @OperLog(title = "客户充值", businessType = 1, targetType = "customer")
    @PostMapping
    public Result<Void> recharge(@Valid @RequestBody RechargeDTO dto) {
        rechargeRecordService.recharge(dto);
        return Result.success();
    }
}
