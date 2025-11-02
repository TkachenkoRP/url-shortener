package com.my.model;

import java.time.LocalDateTime;
import java.util.UUID;


public class ShortUrl {
    private String shortCode;
    private String originalUrl;
    private UUID userId;
    private int clickCount;
    private int maxClicks;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public void setMaxClicks(int maxClicks) {
        this.maxClicks = maxClicks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ShortUrl{" +
               "shortCode='" + shortCode + '\'' +
               ", originalUrl='" + originalUrl + '\'' +
               ", userId=" + userId +
               ", clickCount=" + clickCount +
               ", maxClicks=" + maxClicks +
               ", createdAt=" + createdAt +
               ", expiresAt=" + expiresAt +
               ", active=" + active +
               '}';
    }
}
