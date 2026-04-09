package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.MemberLevelDTO;
import com.bcrm.entity.MemberLevel;
import com.bcrm.service.MemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员等级控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "会员等级管理")
@RestController
@RequestMapping("/member-level")
@RequiredArgsConstructor
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    /**
     * 分页查询会员等级
     */
    @Operation(summary = "分页查询会员等级")
    @GetMapping("/page")
    public Result<PageResult<MemberLevel>> page(PageRequest pageRequest, MemberLevel query) {
        Page<MemberLevel> page = memberLevelService.pageLevels(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询所有启用的会员等级
     */
    @Operation(summary = "查询所有启用的会员等级")
    @GetMapping("/active")
    public Result<List<MemberLevel>> listActive() {
        List<MemberLevel> list = memberLevelService.listActiveLevels();
        return Result.success(list);
    }

    /**
     * 根据ID查询会员等级
     */
    @Operation(summary = "根据ID查询会员等级")
    @GetMapping("/{id}")
    public Result<MemberLevel> getById(@PathVariable Long id) {
        MemberLevel level = memberLevelService.getById(id);
        return Result.success(level);
    }

    /**
     * 新增会员等级
     */
    @Operation(summary = "新增会员等级")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody MemberLevelDTO dto) {
        memberLevelService.addLevel(dto);
        return Result.success();
    }

    /**
     * 修改会员等级
     */
    @Operation(summary = "修改会员等级")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody MemberLevelDTO dto) {
        memberLevelService.updateLevel(dto);
        return Result.success();
    }

    /**
     * 删除会员等级
     */
    @Operation(summary = "删除会员等级")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memberLevelService.deleteLevel(id);
        return Result.success();
    }

    /**
     * 获取会员等级统计
     */
    @Operation(summary = "获取会员等级统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = memberLevelService.getLevelStatistics();
        return Result.success(statistics);
    }
}
