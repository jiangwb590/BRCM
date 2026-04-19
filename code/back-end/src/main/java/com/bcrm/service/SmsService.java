package com.bcrm.service;

import java.util.List;

/**
 * 短信服务接口
 */
public interface SmsService {
    
    /**
     * 发送短信
     * @param phoneNumber 手机号
     * @param content 短信内容
     * @return 是否发送成功
     */
    boolean sendSms(String phoneNumber, String content);
    
    /**
     * 批量发送短信
     * @param phoneNumbers 手机号列表
     * @param content 短信内容
     * @return 成功发送的数量
     */
    int sendBatchSms(List<String> phoneNumbers, String content);
    
    /**
     * 发送生日祝福短信
     * @param phoneNumber 手机号
     * @param customerName 客户姓名
     * @return 是否发送成功
     */
    boolean sendBirthdaySms(String phoneNumber, String customerName);
    
    /**
     * 发送预约提醒短信
     * @param phoneNumber 手机号
     * @param customerName 客户姓名
     * @param appointmentTime 预约时间
     * @param serviceName 服务项目
     * @return 是否发送成功
     */
    boolean sendAppointmentSms(String phoneNumber, String customerName, String appointmentTime, String serviceName);
    
    /**
     * 发送消费关怀短信
     * @param phoneNumber 手机号
     * @param customerName 客户姓名
     * @param amount 消费金额
     * @param balance 剩余余额
     * @return 是否发送成功
     */
    boolean sendConsumeSms(String phoneNumber, String customerName, String amount, String balance);
    
    /**
     * 发送沉睡唤醒短信
     * @param phoneNumber 手机号
     * @param customerName 客户姓名
     * @return 是否发送成功
     */
    boolean sendWakeupSms(String phoneNumber, String customerName);
    
    /**
     * 发送消费回访短信
     * @param phoneNumber 手机号
     * @param customerName 客户姓名
     * @param projectName 项目/套餐名称
     * @param projectDescription 项目/套餐描述（用于短信内容）
     * @return 是否发送成功
     */
    boolean sendConsumeCareSms(String phoneNumber, String customerName, String projectName, String projectDescription);
    
    /**
     * 发送新客户回访短信
     * @param phoneNumber 手机号
     * @param customerName 客户姓名
     * @param gender 性别：1-男 2-女
     * @return 是否发送成功
     */
    boolean sendNewCustomerFollowUpSms(String phoneNumber, String customerName, Integer gender);
    
    /**
     * 发送会员消费满3次提醒短信
     * @param phoneNumber 手机号
     * @param customerName 客户姓名
     * @return 是否发送成功
     */
    boolean sendMemberConsumeRewardSms(String phoneNumber, String customerName);
}
