package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

}
