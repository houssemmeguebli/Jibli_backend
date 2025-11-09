package com.backend.jibli.notification;

public class FCMTokenRequest {
    private int userId;
    private String fcmToken;

    public FCMTokenRequest() {}

    public FCMTokenRequest(int userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}