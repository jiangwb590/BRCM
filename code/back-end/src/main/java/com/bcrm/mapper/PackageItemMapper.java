package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.PackageItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 套餐项目关联Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface PackageItemMapper extends BaseMapper<PackageItem> {

    /**
     * 根据套餐ID查询项目列表
     */
    default List<PackageItem> getByPackageId(Long packageId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PackageItem>()
                .eq(PackageItem::getPackageId, packageId));
    }
}
