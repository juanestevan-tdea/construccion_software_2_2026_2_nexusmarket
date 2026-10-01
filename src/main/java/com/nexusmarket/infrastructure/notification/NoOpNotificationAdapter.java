package com.nexusmarket.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpNotificationAdapter implements NotificationPort {

    @Override
    public void sendNotification(String recipient, String subject, String message) {
        log.debug("NoOpNotificationAdapter: notification dropped - recipient={}, subject={}, message={}",
                recipient, subject, message);
    }
}
