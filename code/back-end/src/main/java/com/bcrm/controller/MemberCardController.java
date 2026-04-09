package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.MemberCardDTO;
import com.bcrm.entity.MemberCard;
import com.bcrm.service.MemberCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员卡控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "会员卡管理")
@RestController
@RequestMapping("/member-card")
@RequiredArgsConstructor
public class MemberCardController {

    private final MemberCardService memberCardService;

    /**
     * 分页查询会员卡
     */
    @Operation(summary = "分页查询会员卡")
    @GetMapping("/page")
    public Result<PageResult<MemberCard>> page(PageRequest pageRequest, MemberCard query) {
        Page<MemberCard> page = memberCardService.pageCards(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询客户会员卡
     */
    @Operation(summary = "查询客户会员卡")
    @GetMapping("/customer/{customerId}")
    public Result<List<MemberCard>> getByCustomerId(@PathVariable Long customerId) {
        List<MemberCard> list = memberCardService.getByCustomerId(customerId);
        return Result.success(list);
    }

    /**
     * 根据ID查询会员卡
     */
    @Operation(summary = "根据ID查询会员卡")
    @GetMapping("/{id}")
    public Result<MemberCard> getById(@PathVariable Long id) {
        MemberCard card = memberCardService.getById(id);
        return Result.success(card);
    }

    /**
     * 开卡
     */
    @Operation(summary = "开卡")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody MemberCardDTO dto) {
        memberCardService.createCard(dto);
        return Result.success();
    }

    /**
     * 充值
     */
    @Operation(summary = "充值")
    @PostMapping("/recharge/{id}")
    public Result<Void> recharge(@PathVariable Long id, @RequestParam BigDecimal amount) {
        memberCardService.recharge(id, amount);
        return Result.success();
    }

    /**
     * 作废会员卡
     */
    @Operation(summary = "作废会员卡")
    @PostMapping("/disable/{id}")
    public Result<Void> disable(@PathVariable Long id) {
        memberCardService.disableCard(id);
        return Result.success();
    }
}
