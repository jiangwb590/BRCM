package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.StockInRecord;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.StockInRecordMapper;
import com.bcrm.service.ProductService;
import com.bcrm.service.StockInRecordService;
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
 * 入库记录服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class StockInRecordServiceImpl extends ServiceImpl<StockInRecordMapper, StockInRecord> implements StockInRecordService {

    private final ProductService productService;

    @Override
    public Page<StockInRecord> pageRecords(PageRequest pageRequest, StockInRecord query) {
        LambdaQueryWrapper<StockInRecord> wrapper = new LambdaQueryWrapper<>();
        // 只查询正常记录，不显示已作废的
        wrapper.eq(StockInRecord::getStatus, 1);
        if (StringUtils.hasText(query.getProductName())) {
            wrapper.like(StockInRecord::getProductName, query.getProductName());
        }
        if (query.getStockInType() != null) {
            wrapper.eq(StockInRecord::getStockInType, query.getStockInType());
        }
        wrapper.orderByDesc(StockInRecord::getStockInTime);
        return page(pageRequest.toPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecord(StockInRecord record) {
        // 生成入库单号
        String stockInNo = "RK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        record.setStockInNo(stockInNo);
        record.setStockInTime(LocalDateTime.now());
        record.setStatus(1);
        
        // 计算总价
        if (record.getUnitPrice() != null && record.getQuantity() != null) {
            record.setTotalPrice(record.getUnitPrice().multiply(new BigDecimal(record.getQuantity())));
        }
        
        save(record);
        
        // 更新库存
        productService.stockIn(record.getProductId(), record.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRecord(Long id) {
        StockInRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("入库记录不存在");
        }
        if (record.getStatus() == 0) {
            throw new BusinessException("该记录已作废");
        }
        record.setStatus(0);
        updateById(record);
        
        // 回退库存
        productService.stockOut(record.getProductId(), record.getQuantity());
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        // 统计今日入库
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayCount = count(new LambdaQueryWrapper<StockInRecord>()
                .eq(StockInRecord::getStatus, 1)
                .ge(StockInRecord::getStockInTime, todayStart));
        result.put("todayCount", todayCount);
        return result;
    }
}
