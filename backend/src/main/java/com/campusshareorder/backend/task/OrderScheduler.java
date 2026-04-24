package com.campusshareorder.backend.task;

import com.campusshareorder.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderService orderService;

    /**
     * 每分钟执行一次：未成团超时取消
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelExpiredOpenOrders() {
        orderService.processTimeoutCancel();
    }

    /**
     * 每分钟执行一次：成团后超时未上传凭证开放投诉
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void openReceiptTimeoutComplaints() {
        // TODO: 实现此方法
    }

    /**
     * 每分钟执行一次：送达超时开放投诉
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void openDeliveryTimeoutComplaints() {
        // TODO: 实现此方法
    }

    /**
     * 每分钟执行一次：自动确认收货
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void autoConfirmReceivedMembers() {
        // TODO: 实现此方法
    }

    /**
     * 每分钟执行一次：完成订单兜底扫描
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void recoverCompletedOrders() {
        // TODO: 实现此方法
    }
}
