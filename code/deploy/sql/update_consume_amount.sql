-- ========================================
-- 更新已有的次卡消费记录金额
-- 按套餐单价计算消费金额
-- ========================================

UPDATE consume_record cr
JOIN package_purchase pp ON cr.purchase_id = pp.id
SET cr.amount = ROUND(pp.price / pp.times * COALESCE(cr.consume_times, 1), 2)
WHERE cr.consume_type = 'times' 
  AND cr.purchase_id IS NOT NULL
  AND (cr.amount IS NULL OR cr.amount = 0);

-- ========================================
-- 更新客户的消费信息（基于消费记录）
-- ========================================

-- 更新客户累计消费金额
UPDATE customer c
SET total_amount = (
    SELECT COALESCE(SUM(amount), 0)
    FROM consume_record
    WHERE customer_id = c.id AND status IS NULL OR status = 1
);

-- 更新客户消费次数
UPDATE customer c
SET consume_count = (
    SELECT COUNT(*)
    FROM consume_record
    WHERE customer_id = c.id AND (status IS NULL OR status = 1)
);

-- 更新客户最近消费时间
UPDATE customer c
SET last_consume_time = (
    SELECT MAX(consume_time)
    FROM consume_record
    WHERE customer_id = c.id AND (status IS NULL OR status = 1)
);
