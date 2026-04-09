package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.MemberLevelDTO;
import com.bcrm.entity.MemberLevel;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.MemberLevelMapper;
import com.bcrm.service.MemberLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员等级服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelMapper, MemberLevel> implements MemberLevelService {

    @Override
    public Page<MemberLevel> pageLevels(PageRequest pageRequest, MemberLevel query) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getLevelName())) {
            wrapper.like(MemberLevel::getLevelName, query.getLevelName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(MemberLevel::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(MemberLevel::getSort);
        return page(pageRequest.toPage(), wrapper);
    }

    @Override
    public List<MemberLevel> listActiveLevels() {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getStatus, 1);
        wrapper.orderByAsc(MemberLevel::getSort);
        return list(wrapper);
    }

    @Override
    public void addLevel(MemberLevelDTO dto) {
        MemberLevel entity = new MemberLevel();
        BeanUtils.copyProperties(dto, entity);
        save(entity);
    }

    @Override
    public void updateLevel(MemberLevelDTO dto) {
        MemberLevel entity = getById(dto.getId());
        if (entity == null) {
            throw new BusinessException("会员等级不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
    }

    @Override
    public void deleteLevel(Long id) {
        removeById(id);
    }

    @Override
    public MemberLevel getLevelByAmount(BigDecimal totalAmount) {
        List<MemberLevel> levels = listActiveLevels();
        for (MemberLevel level : levels) {
            if (totalAmount.compareTo(level.getMinAmount()) >= 0) {
                if (level.getMaxAmount() == null || level.getMaxAmount().compareTo(BigDecimal.ZERO) == 0 
                        || totalAmount.compareTo(level.getMaxAmount()) <= 0) {
                    return level;
                }
            }
        }
        // 返回最高等级
        if (!levels.isEmpty()) {
            return levels.get(levels.size() - 1);
        }
        return null;
    }

    @Override
    public Map<String, Object> getLevelStatistics() {
        Map<String, Object> result = new HashMap<>();
        List<MemberLevel> levels = listActiveLevels();
        result.put("levels", levels);
        result.put("total", levels.size());
        return result;
    }
}
