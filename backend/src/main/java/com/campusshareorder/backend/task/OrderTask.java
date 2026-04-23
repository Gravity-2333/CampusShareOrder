package com.campusshareorder.backend.task;

import com.campusshareorder.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTask {

    private final OrderService orderService;

    /**
     * 每5分钟执行一次自动成团处理
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void processAutoGroup() {
        orderService.processAutoGroup();
    }

    /**
     * 每3分钟执行一次超时取消处理
     */
    @Scheduled(cron = "0 */3 * * * ?")
    public void processTimeoutCancel() {
        orderService.processTimeoutCancel();
    }
}