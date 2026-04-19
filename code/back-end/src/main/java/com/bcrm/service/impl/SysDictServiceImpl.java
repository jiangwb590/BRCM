package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.SysDict;
import com.bcrm.mapper.SysDictMapper;
import com.bcrm.service.SysDictService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public Page<SysDict> pageDicts(PageRequest pageRequest, SysDict query) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SysDict::getParentId).or().eq(SysDict::getParentId, 0);
        if (StringUtils.hasText(query.getDictName())) {
            wrapper.like(SysDict::getDictName, query.getDictName());
        }
        if (StringUtils.hasText(query.getDictCode())) {
            wrapper.like(SysDict::getDictCode, query.getDictCode());
        }
        wrapper.orderByDesc(SysDict::getCreateTime);
        return this.page(pageRequest.toPage(), wrapper);
    }

    @Override
    public void addDict(SysDict dict) {
        dict.setStatus(1);
        this.save(dict);
    }

    @Override
    public void updateDict(SysDict dict) {
        this.updateById(dict);
    }

    @Override
    public void deleteDict(Long id) {
        this.removeById(id);
        // 同时删除字典项
        this.remove(new LambdaQueryWrapper<SysDict>().eq(SysDict::getParentId, id));
    }

    @Override
    public List<SysDict> getByDictCode(String dictCode) {
        // 先查找父级字典
        SysDict parentDict = this.getOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode)
                .last("LIMIT 1"));
        
        if (parentDict == null) {
            return List.of();
        }
        
        // 再查询该字典下的所有子项
        return this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, parentDict.getId())
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSort));
    }

    @Override
    public List<SysDict> getDictItems(Long dictId) {
        return this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, dictId)
                .orderByAsc(SysDict::getSort));
    }

    @Override
    public void addDictItem(SysDict item) {
        item.setStatus(1);
        this.save(item);
    }

    @Override
    public void updateDictItem(SysDict item) {
        this.updateById(item);
    }

    @Override
    public void deleteDictItem(Long id) {
        this.removeById(id);
    }

    @Override
    public String getDictValue(String dictCode) {
        SysDict dict = this.getOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode)
                .eq(SysDict::getStatus, 1)
                .last("LIMIT 1"));
        return dict != null ? dict.getDictValue() : null;
    }
}