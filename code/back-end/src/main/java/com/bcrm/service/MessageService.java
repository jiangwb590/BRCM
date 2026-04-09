package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface MessageService extends IService<Message> {

    /**
     * 分页查询消息
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<Message> pageMessages(PageRequest pageRequest, Message query);

    /**
     * 获取用户未读消息
     *
     * @param receiverId 接收人ID
     * @return 未读消息列表
     */
    List<Message> getUnreadMessages(Long receiverId);

    /**
     * 获取未读消息数量
     *
     * @param receiverId 接收人ID
     * @return 未读消息数量
     */
    long getUnreadCount(Long receiverId);

    /**
     * 标记消息已读
     *
     * @param id 消息ID
     */
    void markAsRead(Long id);

    /**
     * 标记所有消息已读
     *
     * @param receiverId 接收人ID
     */
    void markAllAsRead(Long receiverId);

    /**
     * 发送消息
     *
     * @param message 消息信息
     */
    void sendMessage(Message message);
}
