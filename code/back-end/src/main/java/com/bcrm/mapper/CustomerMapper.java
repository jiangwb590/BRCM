package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

}
