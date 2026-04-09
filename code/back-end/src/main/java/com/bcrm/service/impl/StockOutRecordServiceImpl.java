package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.StockOutRecord;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.StockOutRecordMapper;
import com.bcrm.service.ProductService;
import com.bcrm.service.StockOutRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 出库记录服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class StockOutRecordServiceImpl extends ServiceImpl<StockOutRecordMapper, StockOutRecord> implements StockOutRecordService {

    private final ProductService productService;

    @Override
    public Page<StockOutRecord> pageRecords(PageRequest pageRequest, StockOutRecord query) {
        LambdaQueryWrapper<StockOutRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getProductName())) {
            wrapper.like(StockOutRecord::getProductName, query.getProductName());
        }
        if (query.getStockOutType() != null) {
            wrapper.eq(StockOutRecord::getStockOutType, query.getStockOutType());
        }
        wrapper.orderByDesc(StockOutRecord::getStockOutTime);
        return page(pageRequest.toPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecord(StockOutRecord record) {
        // 生成出库单号
        String stockOutNo = "CK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        record.setStockOutNo(stockOutNo);
        record.setStockOutTime(LocalDateTime.now());
        record.setStatus(1);
        
        // 计算总价
        if (record.getUnitPrice() != null && record.getQuantity() != null) {
            record.setTotalPrice(record.getUnitPrice().multiply(new BigDecimal(record.getQuantity())));
        }
        
        save(record);
        
        // 更新库存
        productService.stockOut(record.getProductId(), record.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRecord(Long id) {
        StockOutRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("出库记录不存在");
        }
        if (record.getStatus() == 0) {
            throw new BusinessException("该记录已作废");
        }
        record.setStatus(0);
        updateById(record);
        
        // 回退库存
        productService.stockIn(record.getProductId(), record.getQuantity());
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        // 统计今日出库
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayCount = count(new LambdaQueryWrapper<StockOutRecord>()
                .eq(StockOutRecord::getStatus, 1)
                .ge(StockOutRecord::getStockOutTime, todayStart));
        result.put("todayCount", todayCount);
        return result;
    }
}
