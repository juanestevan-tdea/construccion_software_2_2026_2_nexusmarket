package com.nexusmarket.infrastructure.notification;

public interface NotificationPort {

    void sendNotification(String recipient, String subject, String message);
}
