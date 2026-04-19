package com.bcrm.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bcrm.entity.*;
import com.bcrm.mapper.*;
import com.bcrm.service.MemberConsumeReminderService;
import com.bcrm.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 系统定时任务
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduledTask {

    private final AppointmentMapper appointmentMapper;
    private final ProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final MemberCardMapper memberCardMapper;
    private final MessageMapper messageMapper;
    private final CustomerCareMapper customerCareMapper;
    private final SmsService smsService;
    private final MemberConsumeReminderService memberConsumeReminderService;

    /**
     * 预约提醒 - 每小时执行一次
     * 提醒明天有预约的客户
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void appointmentReminder() {
        log.info("执行预约提醒定时任务...");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        List<Appointment> appointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getAppointmentDate, tomorrow)
                        .eq(Appointment::getStatus, "已确认"));
        
        int sentCount = 0;
        for (Appointment appointment : appointments) {
            // 检查是否已发送过提醒（防止重复发送）
            Long existCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getMessageType, "appointment")
                            .eq(Message::getRelatedId, appointment.getId())
                            .eq(Message::getRelatedType, "appointment_reminder"));
            
            if (existCount > 0) {
                continue;
            }
            
            Message message = new Message();
            message.setMessageType("appointment");
            message.setTitle("预约提醒");
            message.setContent(String.format("客户 %s 明天 %s 有 %s 预约，请注意安排",
                    appointment.getCustomerName(),
                    appointment.getStartTime(),
                    appointment.getProjectName()));
            message.setReceiverId(appointment.getBeauticianId());
            message.setRelatedId(appointment.getId());
            message.setRelatedType("appointment_reminder");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
            sentCount++;
        }
        log.info("预约提醒任务完成，共发送 {} 条提醒", sentCount);
    }

    /**
     * 预约短信二次提醒 - 每5分钟执行一次
     * 在预约开始时间前30分钟发送短信提醒
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void appointmentSmsReminder() {
        log.info("执行预约短信二次提醒定时任务...");
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        // 查询今天已确认的预约
        List<Appointment> appointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getAppointmentDate, today)
                        .eq(Appointment::getStatus, "已确认")
                        .isNotNull(Appointment::getCustomerPhone));
        
        int sentCount = 0;
        for (Appointment appointment : appointments) {
            // 检查是否已发送过二次提醒短信
            Long existCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getMessageType, "appointment")
                            .eq(Message::getRelatedId, appointment.getId())
                            .eq(Message::getRelatedType, "appointment_sms_reminder"));
            
            if (existCount > 0) {
                continue;
            }
            
            // 计算预约开始时间
            if (appointment.getStartTime() == null) {
                continue;
            }
            
            LocalTime startTime = appointment.getStartTime();
            LocalDateTime appointmentTime = today.atTime(startTime);
            
            // 检查是否在预约时间前30分钟内
            long minutesUntilAppointment = java.time.Duration.between(now, appointmentTime).toMinutes();
            
            if (minutesUntilAppointment > 0 && minutesUntilAppointment <= 30) {
                // 发送短信
                String appointmentTimeStr = String.format("%s %s", 
                        today.format(java.time.format.DateTimeFormatter.ofPattern("MM月dd日")),
                        startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                
                boolean smsSuccess = smsService.sendAppointmentSms(
                        appointment.getCustomerPhone(),
                        appointment.getCustomerName(),
                        appointmentTimeStr,
                        appointment.getProjectName()
                );
                
                if (smsSuccess) {
                    log.info("预约二次提醒短信发送成功，客户：{}，手机号：{}", 
                            appointment.getCustomerName(), appointment.getCustomerPhone());
                    
                    // 记录已发送
                    Message message = new Message();
                    message.setMessageType("appointment");
                    message.setTitle("预约短信二次提醒");
                    message.setContent(String.format("已向客户 %s 发送预约短信提醒", appointment.getCustomerName()));
                    message.setReceiverId(0L);
                    message.setRelatedId(appointment.getId());
                    message.setRelatedType("appointment_sms_reminder");
                    message.setIsRead(0);
                    message.setCreateTime(LocalDateTime.now());
                    messageMapper.insert(message);
                    
                    sentCount++;
                }
            }
        }
        log.info("预约短信二次提醒任务完成，共发送 {} 条短信", sentCount);
    }

    /**
     * 库存预警 - 每天早上8点执行
     * 检查库存低于预警值的产品
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void stockWarning() {
        log.info("执行库存预警定时任务...");
        LocalDate today = LocalDate.now();
        
        List<Product> lowStockProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .apply("stock <= stock_warning"));
        
        int sentCount = 0;
        for (Product product : lowStockProducts) {
            // 检查今天是否已发送过该产品的预警消息
            Long existCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getMessageType, "stock")
                            .eq(Message::getRelatedId, product.getId())
                            .eq(Message::getRelatedType, "stock_warning")
                            .ge(Message::getCreateTime, today.atStartOfDay()));
            
            if (existCount > 0) {
                continue;
            }
            
            Message message = new Message();
            message.setMessageType("stock");
            message.setTitle("库存预警");
            message.setContent(String.format("产品【%s】库存不足，当前库存：%d，预警值：%d，请及时补货",
                    product.getName(),
                    product.getStock(),
                    product.getStockWarning()));
            message.setReceiverId(0L);
            message.setRelatedId(product.getId());
            message.setRelatedType("stock_warning");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
            sentCount++;
        }
        log.info("库存预警任务完成，共发送 {} 条预警", sentCount);
    }

    /**
     * 生日关怀 - 每天早上9点执行
     * 检测当天生日的客户，发送生日祝福短信
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void birthdayCare() {
        log.info("执行生日关怀定时任务...");
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
        
        List<Customer> birthdayCustomers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getStatus, 1)
                        .isNotNull(Customer::getPhone)
                        .apply("MONTH(birthday) = {0} AND DAY(birthday) = {1}", month, day));
        
        int sentCount = 0;
        for (Customer customer : birthdayCustomers) {
            // 检查今天是否已发送过该客户的生日关怀消息
            Long existCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getMessageType, "member")
                            .eq(Message::getRelatedId, customer.getId())
                            .eq(Message::getRelatedType, "birthday_care")
                            .ge(Message::getCreateTime, today.atStartOfDay()));
            
            if (existCount > 0) {
                continue;
            }
            
            // 发送生日祝福短信
            if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
                boolean smsSuccess = smsService.sendBirthdaySms(customer.getPhone(), customer.getName());
                if (smsSuccess) {
                    log.info("生日祝福短信发送成功，客户：{}，手机号：{}", customer.getName(), customer.getPhone());
                } else {
                    log.warn("生日祝福短信发送失败，客户：{}，手机号：{}", customer.getName(), customer.getPhone());
                }
            }
            
            // 创建生日关怀提醒（系统内部消息）
            Message message = new Message();
            message.setMessageType("member");
            message.setTitle("生日关怀");
            message.setContent(String.format("客户【%s】今天过生日，已发送生日祝福短信", customer.getName()));
            message.setReceiverId(0L);
            message.setRelatedId(customer.getId());
            message.setRelatedType("birthday_care");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
            
            // 创建客户关怀记录
            Long careExistCount = customerCareMapper.selectCount(
                    new LambdaQueryWrapper<CustomerCare>()
                            .eq(CustomerCare::getCustomerId, customer.getId())
                            .eq(CustomerCare::getCareType, "生日关怀")
                            .eq(CustomerCare::getPlanDate, today));
            
            if (careExistCount == 0) {
                CustomerCare care = new CustomerCare();
                care.setCustomerId(customer.getId());
                care.setCustomerName(customer.getName());
                care.setCustomerPhone(customer.getPhone());
                care.setCareType("生日关怀");
                care.setContent("系统自动发送生日祝福短信");
                care.setPlanDate(today);
                care.setStatus("已执行");
                care.setExecuteTime(LocalDateTime.now());
                care.setCreateTime(LocalDateTime.now());
                customerCareMapper.insert(care);
            }
            sentCount++;
        }
        log.info("生日关怀任务完成，共发送 {} 条祝福", sentCount);
    }

    /**
     * 沉睡客户检测 - 每周一早上10点执行
     * 检测90天未消费的老客户
     */
    @Scheduled(cron = "0 0 10 ? * MON")
    public void sleepCustomerDetection() {
        log.info("执行沉睡客户检测定时任务...");
        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        LocalDate today = LocalDate.now();
        
        List<Customer> sleepCustomers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getStatus, 1)
                        .in(Customer::getCategory, "老客户", "VIP客户")
                        .lt(Customer::getLastConsumeTime, ninetyDaysAgo.atStartOfDay()));
        
        int sentCount = 0;
        for (Customer customer : sleepCustomers) {
            // 更新客户状态为沉睡
            customer.setCategory("沉睡客户");
            customerMapper.updateById(customer);
            
            // 检查本周是否已发送过唤醒提醒
            Long existCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getMessageType, "member")
                            .eq(Message::getRelatedId, customer.getId())
                            .eq(Message::getRelatedType, "sleep_wake")
                            .ge(Message::getCreateTime, today.minusDays(7).atStartOfDay()));
            
            if (existCount > 0) {
                continue;
            }
            
            Message message = new Message();
            message.setMessageType("member");
            message.setTitle("沉睡客户唤醒");
            message.setContent(String.format("客户【%s】已90天未消费，请及时跟进唤醒", customer.getName()));
            message.setReceiverId(0L);
            message.setRelatedId(customer.getId());
            message.setRelatedType("sleep_wake");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
            sentCount++;
        }
        log.info("沉睡客户检测任务完成，共发送 {} 条提醒", sentCount);
    }

    /**
     * 会员卡到期提醒 - 每天早上8点30分执行
     * 检测即将到期的会员卡
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void memberCardExpireReminder() {
        log.info("执行会员卡到期提醒定时任务...");
        LocalDate sevenDaysLater = LocalDate.now().plusDays(7);
        LocalDate today = LocalDate.now();
        
        List<MemberCard> expiringCards = memberCardMapper.selectList(
                new LambdaQueryWrapper<MemberCard>()
                        .eq(MemberCard::getStatus, 1)
                        .eq(MemberCard::getValidEndDate, sevenDaysLater));
        
        int sentCount = 0;
        for (MemberCard card : expiringCards) {
            // 检查是否已发送过该会员卡的到期提醒
            Long existCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getMessageType, "member")
                            .eq(Message::getRelatedId, card.getId())
                            .eq(Message::getRelatedType, "card_expire")
                            .ge(Message::getCreateTime, today.minusDays(7).atStartOfDay()));
            
            if (existCount > 0) {
                continue;
            }
            
            Message message = new Message();
            message.setMessageType("member");
            message.setTitle("会员卡到期提醒");
            message.setContent(String.format("客户【%s】的会员卡将于 %s 到期，请及时提醒续费",
                    card.getCustomerName(),
                    card.getValidEndDate()));
            message.setReceiverId(0L);
            message.setRelatedId(card.getId());
            message.setRelatedType("card_expire");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
            sentCount++;
        }
        log.info("会员卡到期提醒任务完成，共发送 {} 条提醒", sentCount);
    }
    
    /**
     * 消费关怀 - 每天早上10点执行
     * 检测当天需要回访的消费关怀记录，发送短信给客户
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void consumeCare() {
        log.info("执行消费关怀定时任务...");
        LocalDate today = LocalDate.now();
        
        // 查询今天需要执行的消费关怀记录
        List<CustomerCare> careList = customerCareMapper.selectList(
                new LambdaQueryWrapper<CustomerCare>()
                        .eq(CustomerCare::getCareType, "consume_care")
                        .eq(CustomerCare::getStatus, "待执行")
                        .eq(CustomerCare::getPlanDate, today));
        
        int sentCount = 0;
        for (CustomerCare care : careList) {
            try {
                // 发送消费回访短信
                if (care.getCustomerPhone() != null && !care.getCustomerPhone().isEmpty()) {
                    boolean success = smsService.sendConsumeCareSms(
                            care.getCustomerPhone(),
                            care.getCustomerName(),
                            care.getProjectName(),
                            care.getProjectDescription() != null ? care.getProjectDescription() : "感谢您的光临，期待再次为您服务！"
                    );
                    
                    if (success) {
                        // 更新关怀记录状态为已执行
                        CustomerCare updateCare = new CustomerCare();
                        updateCare.setId(care.getId());
                        updateCare.setStatus("已执行");
                        updateCare.setExecuteTime(LocalDateTime.now());
                        customerCareMapper.updateById(updateCare);
                        sentCount++;
                        log.info("消费关怀短信发送成功，客户：{}，手机号：{}", care.getCustomerName(), care.getCustomerPhone());
                    }
                } else {
                    log.warn("客户 {} 手机号为空，跳过发送短信", care.getCustomerName());
                    // 更新状态为已取消
                    CustomerCare updateCare = new CustomerCare();
                    updateCare.setId(care.getId());
                    updateCare.setStatus("已取消");
                    updateCare.setRemark("客户手机号为空，无法发送短信");
                    customerCareMapper.updateById(updateCare);
                }
            } catch (Exception e) {
                log.error("发送消费关怀短信失败，客户：{}，错误：{}", care.getCustomerName(), e.getMessage());
            }
        }
        log.info("消费关怀任务完成，共发送 {} 条短信", sentCount);
    }

    /**
     * 新客户回访短信 - 每天上午10点执行
     * 针对登记满7天的新客户发送回访短信
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void newCustomerFollowUp() {
        log.info("执行新客户回访短信定时任务...");
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        
        // 查询7天前登记的客户（新客户）
        LocalDateTime startOfDay = sevenDaysAgo.atTime(0, 0, 0);
        LocalDateTime endOfDay = sevenDaysAgo.atTime(23, 59, 59);
        
        List<Customer> newCustomers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getStatus, 1)
                        .eq(Customer::getCategory, "新客户")
                        .between(Customer::getCreateTime, startOfDay, endOfDay)
                        .isNotNull(Customer::getPhone));
        
        int sentCount = 0;
        for (Customer customer : newCustomers) {
            // 检查是否已发送过新客户回访短信
            Long existCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getMessageType, "follow_up")
                            .eq(Message::getRelatedId, customer.getId())
                            .eq(Message::getRelatedType, "new_customer_follow_up"));
            
            if (existCount > 0) {
                continue;
            }
            
            // 发送回访短信
            if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
                try {
                    boolean smsSuccess = smsService.sendNewCustomerFollowUpSms(
                            customer.getPhone(), 
                            customer.getName(),
                            customer.getGender()
                    );
                    
                    if (smsSuccess) {
                        log.info("新客户回访短信发送成功，客户：{}，手机号：{}", 
                                customer.getName(), customer.getPhone());
                        
                        // 记录消息
                        Message message = new Message();
                        message.setMessageType("follow_up");
                        message.setTitle("新客户回访");
                        message.setContent(String.format("已向新客户【%s】发送回访短信", customer.getName()));
                        message.setReceiverId(0L);
                        message.setRelatedId(customer.getId());
                        message.setRelatedType("new_customer_follow_up");
                        message.setIsRead(0);
                        message.setCreateTime(LocalDateTime.now());
                        messageMapper.insert(message);
                        
                        // 创建客户关怀记录
                        CustomerCare care = new CustomerCare();
                        care.setCustomerId(customer.getId());
                        care.setCustomerName(customer.getName());
                        care.setCustomerPhone(customer.getPhone());
                        care.setCareType("回访提醒");
                        care.setContent("新客户登记满7天自动发送回访短信");
                        care.setPlanDate(today);
                        care.setStatus("已执行");
                        care.setExecuteTime(LocalDateTime.now());
                        care.setCreateTime(LocalDateTime.now());
                        customerCareMapper.insert(care);
                        
                        sentCount++;
                    }
                } catch (Exception e) {
                    log.error("发送新客户回访短信失败，客户：{}，错误：{}", 
                            customer.getName(), e.getMessage());
                }
            }
        }
        log.info("新客户回访短信任务完成，共发送 {} 条短信", sentCount);
    }
    
    /**
     * 会员消费满3次提醒 - 每天上午11点执行
     * 扫描当月消费满3次的会员客户，发送提醒短信
     */
    @Scheduled(cron = "0 0 11 * * ?")
    public void memberConsumeRewardReminder() {
        log.info("执行会员消费满3次提醒定时任务...");
        memberConsumeReminderService.sendMemberConsumeReminder();
    }
}
