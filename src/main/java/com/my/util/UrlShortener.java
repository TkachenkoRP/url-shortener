package com.my.util;

import com.my.config.AppConfig;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

public class UrlShortener {

    private UrlShortener() {
    }

    public static String generateShortCode(String originalUrl, UUID userId) {
        try {
            String input = originalUrl + userId.toString() + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.substring(0, AppConfig.getShortCodeLength());
        } catch (Exception e) {
            return Math.abs((originalUrl + userId).hashCode()) + "" + System.currentTimeMillis() % 10000;
        }
    }

    public static String buildShortUrl(String shortCode) {
        return AppConfig.getBaseUrl() + "/" + shortCode;
    }
}
