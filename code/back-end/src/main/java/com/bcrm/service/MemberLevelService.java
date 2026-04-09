package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.MemberLevelDTO;
import com.bcrm.entity.MemberLevel;

import java.util.List;
import java.util.Map;

/**
 * 会员等级服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface MemberLevelService extends IService<MemberLevel> {

    /**
     * 分页查询会员等级
     */
    Page<MemberLevel> pageLevels(PageRequest pageRequest, MemberLevel query);

    /**
     * 查询所有启用的会员等级
     */
    List<MemberLevel> listActiveLevels();

    /**
     * 新增会员等级
     */
    void addLevel(MemberLevelDTO dto);

    /**
     * 修改会员等级
     */
    void updateLevel(MemberLevelDTO dto);

    /**
     * 删除会员等级
     */
    void deleteLevel(Long id);

    /**
     * 根据消费金额获取对应会员等级
     */
    MemberLevel getLevelByAmount(java.math.BigDecimal totalAmount);

    /**
     * 获取会员等级统计
     */
    Map<String, Object> getLevelStatistics();
}
