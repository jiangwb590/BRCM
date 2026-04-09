package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.SysDict;
import com.bcrm.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    /**
     * 分页查询字典
     */
    @Operation(summary = "分页查询字典")
    @GetMapping("/page")
    public Result<PageResult<SysDict>> page(PageRequest pageRequest, SysDict query) {
        Page<SysDict> page = sysDictService.pageDicts(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 根据ID查询字典
     */
    @Operation(summary = "根据ID查询字典")
    @GetMapping("/{id}")
    public Result<SysDict> getById(@PathVariable Long id) {
        SysDict dict = sysDictService.getById(id);
        return Result.success(dict);
    }

    /**
     * 新增字典
     */
    @Operation(summary = "新增字典")
    @PostMapping
    public Result<Void> add(@RequestBody SysDict dict) {
        sysDictService.addDict(dict);
        return Result.success();
    }

    /**
     * 修改字典
     */
    @Operation(summary = "修改字典")
    @PutMapping
    public Result<Void> update(@RequestBody SysDict dict) {
        sysDictService.updateDict(dict);
        return Result.success();
    }

    /**
     * 删除字典
     */
    @Operation(summary = "删除字典")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysDictService.deleteDict(id);
        return Result.success();
    }

    /**
     * 根据字典编码获取字典项
     */
    @Operation(summary = "根据字典编码获取字典项")
    @GetMapping("/code/{dictCode}")
    public Result<List<SysDict>> getByCode(@PathVariable String dictCode) {
        List<SysDict> list = sysDictService.getByDictCode(dictCode);
        return Result.success(list);
    }

    /**
     * 获取所有字典
     */
    @Operation(summary = "获取所有字典")
    @GetMapping("/all")
    public Result<List<SysDict>> getAll() {
        List<SysDict> list = sysDictService.list();
        return Result.success(list);
    }

    /**
     * 获取字典项列表
     */
    @Operation(summary = "获取字典项列表")
    @GetMapping("/item/{dictId}")
    public Result<List<SysDict>> getItems(@PathVariable Long dictId) {
        List<SysDict> list = sysDictService.getDictItems(dictId);
        return Result.success(list);
    }

    /**
     * 新增字典项
     */
    @Operation(summary = "新增字典项")
    @PostMapping("/item")
    public Result<Void> addItem(@RequestBody SysDict item) {
        sysDictService.addDictItem(item);
        return Result.success();
    }

    /**
     * 修改字典项
     */
    @Operation(summary = "修改字典项")
    @PutMapping("/item")
    public Result<Void> updateItem(@RequestBody SysDict item) {
        sysDictService.updateDictItem(item);
        return Result.success();
    }

    /**
     * 删除字典项
     */
    @Operation(summary = "删除字典项")
    @DeleteMapping("/item/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        sysDictService.deleteDictItem(id);
        return Result.success();
    }
}