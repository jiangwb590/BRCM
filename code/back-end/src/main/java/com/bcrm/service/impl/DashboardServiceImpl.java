package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bcrm.entity.*;
import com.bcrm.mapper.*;
import com.bcrm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CustomerMapper customerMapper;
    private final ConsumeRecordMapper consumeRecordMapper;
    private final AppointmentMapper appointmentMapper;
    private final MemberCardMapper memberCardMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    private final PackagePurchaseMapper packagePurchaseMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        
        // 本月时间范围
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        
        // ========== 基础指标 ==========
        // 客户总数
        long totalCustomers = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>().eq(Customer::getDeleted, 0));
        
        // 今日新增客户
        long todayNewCustomers = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getDeleted, 0)
                        .ge(Customer::getCreateTime, todayStart));
        
        // 今日预约数
        long todayAppointments = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getAppointmentDate, today)
                        .ne(Appointment::getStatus, "已取消"));
        
        // 会员总数（拥有储值卡的客户）
        long totalMembers = memberCardMapper.selectCount(
                new LambdaQueryWrapper<MemberCard>()
                        .eq(MemberCard::getCardType, "储值卡") // 2-储值卡
                        .eq(MemberCard::getDeleted, 0)
                        .gt(MemberCard::getRemainAmount, BigDecimal.ZERO));
        
        // ========== 收入指标 ==========
        // 今日营收
        BigDecimal todayRevenue = getTodayRevenue();
        
        // 本月项目业绩（单次项目消费 + 次卡消费对应金额）
        BigDecimal projectRevenue = getProjectRevenue(monthStart, monthEnd);
        
        // 本月产品业绩（产品销售金额）
        BigDecimal productRevenue = getProductRevenue(monthStart, monthEnd);
        
        // 本月卡业绩（充值金额 + 套餐购买金额）
        BigDecimal cardRevenue = getCardRevenue(monthStart, monthEnd);
        
        // 营业收入 = 项目业绩 + 产品业绩（实际消费产生的收入）
        BigDecimal totalRevenue = projectRevenue.add(productRevenue);
        
        // 总业绩 = 营业收入 + 卡业绩（营业收入 + 售卡收入）
        BigDecimal monthRevenue = totalRevenue.add(cardRevenue);
        
        // ========== 卡耗指标 ==========
        // 本月总卡耗（次卡消费 + 储值消费金额）
        BigDecimal cardConsume = getCardConsume(monthStart, monthEnd);
        
        // 卡耗业绩比
        BigDecimal cardConsumeRatio = BigDecimal.ZERO;
        if (cardRevenue.compareTo(BigDecimal.ZERO) > 0) {
            cardConsumeRatio = cardConsume.divide(cardRevenue, 2, RoundingMode.HALF_UP);
        }
        
        // ========== 客流指标 ==========
        // 本月到店人次
        long visitTimes = getVisitTimes(monthStart, monthEnd);
        
        // 今日到店人次
        LocalDateTime todayStartTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEndTime = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        long todayVisitTimes = getVisitTimes(todayStartTime, todayEndTime);
        
        // 本月到店人数（去重客户）
        long visitCustomers = getVisitCustomers(monthStart, monthEnd);
        
        // 本月老客到店人数（消费次数>=2的客户）
        long oldCustomerVisits = getOldCustomerVisits(monthStart, monthEnd);
        
        // 计算环比数据
        int customerTrend = calculateCustomerTrend();
        int revenueTrend = calculateRevenueTrend();
        int appointmentTrend = calculateAppointmentTrend();
        int memberTrend = calculateMemberTrend();
        
        // 基础数据
        result.put("totalCustomers", totalCustomers);
        result.put("totalMembers", totalMembers);
        result.put("todayNewCustomers", todayNewCustomers);
        result.put("todayAppointments", todayAppointments);
        
        // 收入数据
        result.put("todayRevenue", todayRevenue);
        result.put("monthRevenue", monthRevenue);
        result.put("projectRevenue", projectRevenue);
        result.put("cardRevenue", cardRevenue);
        result.put("productRevenue", productRevenue);
        result.put("totalRevenue", totalRevenue);
        
        // 卡耗数据
        result.put("cardConsume", cardConsume);
        result.put("cardConsumeRatio", cardConsumeRatio);
        
        // 客流数据
        result.put("visitTimes", visitTimes);
        result.put("todayVisitTimes", todayVisitTimes);
        result.put("visitCustomers", visitCustomers);
        result.put("oldCustomerVisits", oldCustomerVisits);
        
        // 环比数据
        result.put("customerTrend", customerTrend);
        result.put("revenueTrend", revenueTrend);
        result.put("appointmentTrend", appointmentTrend);
        result.put("memberTrend", memberTrend);
        
        return result;
    }

    @Override
    public Map<String, Object> getRevenueTrend(Integer days, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        // 如果指定了日期范围，使用日期范围
        if (startDate != null && endDate != null) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                LocalDateTime start = current.atTime(0, 0, 0);
                LocalDateTime end = current.atTime(23, 59, 59);
                
                BigDecimal revenue = consumeRecordMapper.selectList(
                        new LambdaQueryWrapper<ConsumeRecord>()
                                .eq(ConsumeRecord::getDeleted, 0)
                                .between(ConsumeRecord::getConsumeTime, start, end)
                ).stream().map(ConsumeRecord::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                
                dates.add(current.format(formatter));
                revenues.add(revenue);
                current = current.plusDays(1);
            }
        } else {
            // 使用天数参数
            int actualDays = days != null ? days : 7;
            for (int i = actualDays - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime start = date.atTime(0, 0, 0);
                LocalDateTime end = date.atTime(23, 59, 59);
                
                BigDecimal revenue = consumeRecordMapper.selectList(
                        new LambdaQueryWrapper<ConsumeRecord>()
                                .eq(ConsumeRecord::getDeleted, 0)
                                .between(ConsumeRecord::getConsumeTime, start, end)
                ).stream().map(ConsumeRecord::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                
                dates.add(date.format(formatter));
                revenues.add(revenue);
            }
        }
        
        result.put("dates", dates);
        result.put("revenues", revenues);
        
        return result;
    }

    @Override
    public Map<String, Object> getCustomerSource(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .eq(Customer::getDeleted, 0);
        
        // 如果指定了日期范围，按创建时间筛选
        if (startDate != null && endDate != null) {
            wrapper.between(Customer::getCreateTime, 
                    startDate.atTime(0, 0, 0), 
                    endDate.atTime(23, 59, 59));
        }
        
        List<Customer> customers = customerMapper.selectList(wrapper);
        
        Map<String, Long> sourceCount = customers.stream()
                .filter(c -> c.getSource() != null)
                .collect(Collectors.groupingBy(Customer::getSource, Collectors.counting()));
        
        result.put("data", sourceCount);
        
        return result;
    }

    @Override
    public Map<String, Object> getCustomerCategory() {
        Map<String, Object> result = new HashMap<>();
        
        List<Customer> customers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>().eq(Customer::getDeleted, 0));
        
        Map<String, Long> categoryCount = customers.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(Customer::getCategory, Collectors.counting()));
        
        result.put("data", categoryCount);
        
        return result;
    }

    @Override
    public Map<String, Object> getEmployeePerformance(Integer limit, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime queryStart;
        LocalDateTime queryEnd;
        
        if (startDate != null && endDate != null) {
            queryStart = startDate.atTime(0, 0, 0);
            queryEnd = endDate.atTime(23, 59, 59);
        } else {
            // 默认本月
            queryStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            queryEnd = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        }
        
        List<ConsumeRecord> records = consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, queryStart, queryEnd)
                        .isNotNull(ConsumeRecord::getBeauticianId));
        
        // 使用amount统计业绩（包含次卡消费的金额）
        Map<String, BigDecimal> performance = records.stream()
                .filter(r -> r.getBeauticianName() != null && r.getAmount() != null)
                .collect(Collectors.groupingBy(
                        ConsumeRecord::getBeauticianName,
                        Collectors.reducing(BigDecimal.ZERO, ConsumeRecord::getAmount, BigDecimal::add)
                ));
        
        // 排序取前N名，转换为Map列表便于JSON序列化
        List<Map<String, Object>> sortedPerformance = performance.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        result.put("data", sortedPerformance);
        
        return result;
    }

    @Override
    public Map<String, Object> getProjectSales(Integer limit, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime queryStart;
        LocalDateTime queryEnd;
        
        if (startDate != null && endDate != null) {
            queryStart = startDate.atTime(0, 0, 0);
            queryEnd = endDate.atTime(23, 59, 59);
        } else {
            // 默认本月
            queryStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            queryEnd = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        }
        
        List<ConsumeRecord> records = consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, queryStart, queryEnd));
        
        Map<String, Long> projectCount = records.stream()
                .filter(r -> r.getProjectName() != null)
                .collect(Collectors.groupingBy(ConsumeRecord::getProjectName, Collectors.counting()));
        
        // 排序取前N名，转换为Map列表便于JSON序列化
        List<Map<String, Object>> sortedProjects = projectCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        result.put("data", sortedProjects);
        
        return result;
    }

    /**
     * 获取今日营收
     */
    private BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        return consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, startOfDay, endOfDay)
        ).stream().map(ConsumeRecord::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取本月营收
     */
    private BigDecimal getMonthRevenue() {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        return consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, monthStart, monthEnd)
        ).stream().map(ConsumeRecord::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算客户环比（本月新增 vs 上月新增）
     */
    private int calculateCustomerTrend() {
        // 本月开始和结束
        LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thisMonthEnd = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        // 上月开始和结束
        LocalDateTime lastMonthStart = LocalDateTime.now().minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthEnd = LocalDateTime.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        long thisMonthCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getDeleted, 0)
                        .between(Customer::getCreateTime, thisMonthStart, thisMonthEnd));
        
        long lastMonthCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getDeleted, 0)
                        .between(Customer::getCreateTime, lastMonthStart, lastMonthEnd));
        
        return calculateTrendPercent(thisMonthCount, lastMonthCount);
    }

    /**
     * 计算营收环比（本月 vs 上月）
     */
    private int calculateRevenueTrend() {
        // 本月
        LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thisMonthEnd = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        // 上月
        LocalDateTime lastMonthStart = LocalDateTime.now().minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthEnd = LocalDateTime.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        BigDecimal thisMonthRevenue = consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, thisMonthStart, thisMonthEnd))
                .stream().map(ConsumeRecord::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal lastMonthRevenue = consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, lastMonthStart, lastMonthEnd))
                .stream().map(ConsumeRecord::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return calculateTrendPercent(thisMonthRevenue, lastMonthRevenue);
    }

    /**
     * 计算预约环比（本月 vs 上月）
     */
    private int calculateAppointmentTrend() {
        LocalDate thisMonthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate thisMonthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        LocalDate lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth());
        
        long thisMonthCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .between(Appointment::getAppointmentDate, thisMonthStart, thisMonthEnd)
                        .ne(Appointment::getStatus, "已取消"));
        
        long lastMonthCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .between(Appointment::getAppointmentDate, lastMonthStart, lastMonthEnd)
                        .ne(Appointment::getStatus, "已取消"));
        
        return calculateTrendPercent(thisMonthCount, lastMonthCount);
    }

    /**
     * 计算会员环比（本月新开储值卡 vs 上月新开储值卡）
     */
    private int calculateMemberTrend() {
        LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thisMonthEnd = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        LocalDateTime lastMonthStart = LocalDateTime.now().minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthEnd = LocalDateTime.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        // 本月新开储值卡的客户数
        long thisMonthCount = memberCardMapper.selectCount(
                new LambdaQueryWrapper<MemberCard>()
                        .eq(MemberCard::getCardType, "储值卡")
                        .eq(MemberCard::getDeleted, 0)
                        .between(MemberCard::getCreateTime, thisMonthStart, thisMonthEnd));
        
        // 上月新开储值卡的客户数
        long lastMonthCount = memberCardMapper.selectCount(
                new LambdaQueryWrapper<MemberCard>()
                        .eq(MemberCard::getCardType, "储值卡")
                        .eq(MemberCard::getDeleted, 0)
                        .between(MemberCard::getCreateTime, lastMonthStart, lastMonthEnd));
        
        return calculateTrendPercent(thisMonthCount, lastMonthCount);
    }

    /**
     * 计算环比百分比
     */
    private int calculateTrendPercent(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100 : 0;
        }
        return (int) ((current - previous) * 100 / previous);
    }

    /**
     * 计算环比百分比（BigDecimal版本）
     */
    private int calculateTrendPercent(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }
        return current.subtract(previous).multiply(new BigDecimal(100))
                .divide(previous, 0, java.math.RoundingMode.HALF_UP).intValue();
    }

    /**
     * 获取项目业绩（有项目ID的消费金额）
     */
    private BigDecimal getProjectRevenue(LocalDateTime start, LocalDateTime end) {
        return consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, start, end)
                        .isNotNull(ConsumeRecord::getProjectId))
                .stream()
                .map(ConsumeRecord::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取卡业绩（充值金额 + 套餐购买金额）
     */
    private BigDecimal getCardRevenue(LocalDateTime start, LocalDateTime end) {
        // 充值金额
        BigDecimal rechargeAmount = rechargeRecordMapper.selectList(
                new LambdaQueryWrapper<RechargeRecord>()
                        .between(RechargeRecord::getCreateTime, start, end)
                        .eq(RechargeRecord::getStatus, 1))
                .stream()
                .map(RechargeRecord::getRechargeAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 套餐购买金额
        BigDecimal packageAmount = packagePurchaseMapper.selectList(
                new LambdaQueryWrapper<PackagePurchase>()
                        .between(PackagePurchase::getCreateTime, start, end)
                        .eq(PackagePurchase::getStatus, 1))
                .stream()
                .map(PackagePurchase::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return rechargeAmount.add(packageAmount);
    }

    /**
     * 获取产品业绩（购买产品的消费金额）
     */
    private BigDecimal getProductRevenue(LocalDateTime start, LocalDateTime end) {
        return consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, start, end)
                        .eq(ConsumeRecord::getConsumeType, "product"))
                .stream()
                .map(ConsumeRecord::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取总卡耗（次卡消费 + 储值消费金额）
     */
    private BigDecimal getCardConsume(LocalDateTime start, LocalDateTime end) {
        return consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, start, end)
                        .in(ConsumeRecord::getConsumeType, "times", "balance"))
                .stream()
                .map(ConsumeRecord::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取到店人次（消费记录数）
     */
    private long getVisitTimes(LocalDateTime start, LocalDateTime end) {
        return consumeRecordMapper.selectCount(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, start, end));
    }

    /**
     * 获取到店人数（去重客户）
     */
    private long getVisitCustomers(LocalDateTime start, LocalDateTime end) {
        List<ConsumeRecord> records = consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, start, end)
                        .isNotNull(ConsumeRecord::getCustomerId));
        
        return records.stream()
                .map(ConsumeRecord::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    /**
     * 获取老客到店人数（消费次数>=2的客户）
     */
    private long getOldCustomerVisits(LocalDateTime start, LocalDateTime end) {
        List<ConsumeRecord> records = consumeRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumeRecord>()
                        .eq(ConsumeRecord::getDeleted, 0)
                        .between(ConsumeRecord::getConsumeTime, start, end)
                        .isNotNull(ConsumeRecord::getCustomerId));
        
        // 统计每个客户的历史消费次数
        Map<Long, Long> customerConsumeCount = records.stream()
                .collect(Collectors.groupingBy(ConsumeRecord::getCustomerId, Collectors.counting()));
        
        // 筛选消费次数>=2的客户
        return customerConsumeCount.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .count();
    }

    @Override
    public Map<String, Object> getConsumptionRatioTrend(Integer days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Double> productRatios = new ArrayList<>();
        List<Double> consumableRatios = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        int actualDays = days != null ? days : 7;
        
        for (int i = actualDays - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atTime(0, 0, 0);
            LocalDateTime end = date.atTime(23, 59, 59);
            
            // 当天到店人次
            long visitCount = consumeRecordMapper.selectCount(
                    new LambdaQueryWrapper<ConsumeRecord>()
                            .eq(ConsumeRecord::getDeleted, 0)
                            .between(ConsumeRecord::getConsumeTime, start, end));
            
            // 当天产品/耗材消耗数量
            int productConsumption = 0;
            int consumableConsumption = 0;
            
            List<ConsumeRecord> records = consumeRecordMapper.selectList(
                    new LambdaQueryWrapper<ConsumeRecord>()
                            .eq(ConsumeRecord::getDeleted, 0)
                            .between(ConsumeRecord::getConsumeTime, start, end)
                            .isNotNull(ConsumeRecord::getProductConsumptions));
            
            for (ConsumeRecord record : records) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<java.util.Map<String, Object>> consumptions = mapper.readValue(
                            record.getProductConsumptions(),
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
                    );
                    
                    for (java.util.Map<String, Object> item : consumptions) {
                        int quantity = Integer.valueOf(item.get("quantity").toString());
                        String type = item.get("type").toString();
                        if ("product".equals(type)) {
                            productConsumption += quantity;
                        } else if ("consumable".equals(type)) {
                            consumableConsumption += quantity;
                        }
                    }
                } catch (Exception e) {
                    log.error("解析产品消耗记录失败", e);
                }
            }
            
            // 计算比例
            double productRatio = visitCount > 0 ? (double) productConsumption / visitCount : 0;
            double consumableRatio = visitCount > 0 ? (double) consumableConsumption / visitCount : 0;
            
            dates.add(date.format(formatter));
            productRatios.add(Math.round(productRatio * 100.0) / 100.0);
            consumableRatios.add(Math.round(consumableRatio * 100.0) / 100.0);
        }
        
        result.put("dates", dates);
        result.put("productRatios", productRatios);
        result.put("consumableRatios", consumableRatios);
        
        return result;
    }

    @Override
    public Map<String, Object> getCustomerSourceTrend(Integer days, String channel) {
        Map<String, Object> result = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        Map<String, List<Integer>> dataMap = new HashMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        int actualDays = days != null ? days : 7;
        
        // 获取所有客户来源
        List<Customer> allCustomers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getDeleted, 0)
                        .isNotNull(Customer::getSource));
        
        // 统计每天各渠道的客户数
        for (int i = actualDays - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atTime(0, 0, 0);
            LocalDateTime end = date.atTime(23, 59, 59);
            
            dateList.add(date.format(formatter));
            
            // 筛选该日期新增的客户
            Map<String, Integer> dayCount = new HashMap<>();
            for (Customer customer : allCustomers) {
                if (customer.getCreateTime() != null 
                        && customer.getCreateTime().isAfter(start) 
                        && customer.getCreateTime().isBefore(end)) {
                    String source = customer.getSource() != null ? customer.getSource() : "其他";
                    // 如果指定了渠道，只统计该渠道
                    if (channel != null && !channel.isEmpty() && !channel.equals(source)) {
                        continue;
                    }
                    dayCount.put(source, dayCount.getOrDefault(source, 0) + 1);
                }
            }
            
            // 将该天的数据添加到dataMap
            for (Map.Entry<String, Integer> entry : dayCount.entrySet()) {
                if (!dataMap.containsKey(entry.getKey())) {
                    dataMap.put(entry.getKey(), new ArrayList<>());
                }
                // 补齐之前的天数
                while (dataMap.get(entry.getKey()).size() < dateList.size() - 1) {
                    dataMap.get(entry.getKey()).add(0);
                }
                dataMap.get(entry.getKey()).add(entry.getValue());
            }
        }
        
        // 补齐所有渠道的数据
        for (List<Integer> list : dataMap.values()) {
            while (list.size() < dateList.size()) {
                list.add(0);
            }
        }
        
        result.put("dates", dateList);
        result.put("data", dataMap);
        
        return result;
    }
}
