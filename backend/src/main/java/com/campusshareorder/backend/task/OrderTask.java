package com.campusshareorder.backend.task;

import com.campusshareorder.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTask {

    private final OrderService orderService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelExpiredOpenOrders() {
        orderService.cancelExpiredOpenOrders();
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void openReceiptTimeoutComplaints() {
        orderService.openReceiptTimeoutComplaints();
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void openDeliveryTimeoutComplaints() {
        orderService.openDeliveryTimeoutComplaints();
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void autoConfirmReceivedMembers() {
        orderService.autoConfirmReceivedMembers();
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void recoverCompletedOrders() {
        orderService.recoverCompletedOrders();
    }
}
