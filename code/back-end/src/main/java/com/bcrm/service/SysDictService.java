package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.SysDict;

import java.util.List;

/**
 * 字典服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 分页查询字典
     */
    Page<SysDict> pageDicts(PageRequest pageRequest, SysDict query);

    /**
     * 新增字典
     */
    void addDict(SysDict dict);

    /**
     * 修改字典
     */
    void updateDict(SysDict dict);

    /**
     * 删除字典
     */
    void deleteDict(Long id);

    /**
     * 根据字典编码获取字典项
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<SysDict> getByDictCode(String dictCode);

    /**
     * 获取字典项列表
     */
    List<SysDict> getDictItems(Long dictId);

    /**
     * 新增字典项
     */
    void addDictItem(SysDict item);

    /**
     * 修改字典项
     */
    void updateDictItem(SysDict item);

    /**
     * 删除字典项
     */
    void deleteDictItem(Long id);

    /**
     * 根据字典编码获取字典值
     *
     * @param dictCode 字典编码
     * @return 字典值，不存在则返回null
     */
    String getDictValue(String dictCode);
}