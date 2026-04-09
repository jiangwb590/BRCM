package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
