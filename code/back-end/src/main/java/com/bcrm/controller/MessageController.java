package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.Message;
import com.bcrm.security.LoginUser;
import com.bcrm.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "消息管理")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 分页查询消息
     */
    @Operation(summary = "分页查询消息")
    @GetMapping("/page")
    public Result<PageResult<Message>> page(PageRequest pageRequest, Message query,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        if (query.getReceiverId() == null) {
            query.setReceiverId(loginUser.getUser().getId());
        }
        Page<Message> page = messageService.pageMessages(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取未读消息
     */
    @Operation(summary = "获取未读消息")
    @GetMapping("/unread")
    public Result<List<Message>> getUnread(@AuthenticationPrincipal LoginUser loginUser) {
        List<Message> list = messageService.getUnreadMessages(loginUser.getUser().getId());
        return Result.success(list);
    }

    /**
     * 获取未读消息数量
     */
    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        long count = messageService.getUnreadCount(loginUser.getUser().getId());
        return Result.success(count);
    }

    /**
     * 标记消息已读
     */
    @Operation(summary = "标记消息已读")
    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    /**
     * 标记所有消息已读
     */
    @Operation(summary = "标记所有消息已读")
    @PostMapping("/read/all")
    public Result<Void> markAllAsRead(@AuthenticationPrincipal LoginUser loginUser) {
        messageService.markAllAsRead(loginUser.getUser().getId());
        return Result.success();
    }
}
