package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

}
