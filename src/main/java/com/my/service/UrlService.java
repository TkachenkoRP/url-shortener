package com.my.service;

import com.my.config.AppConfig;
import com.my.model.ShortUrl;
import com.my.model.User;
import com.my.util.UrlShortener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UrlService {
    private final Map<String, ShortUrl> urlRepository = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> userUrls = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    public UrlService(NotificationService notificationService) {
        this.notificationService = notificationService;
        startCleanupTask();
    }

    public ShortUrl createShortUrl(String originalUrl, User user) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        String shortCode = UrlShortener.generateShortCode(originalUrl, user.getId());

        while (urlRepository.containsKey(shortCode)) {
            shortCode = UrlShortener.generateShortCode(originalUrl + System.nanoTime(), user.getId());
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setUserId(user.getId());
        shortUrl.setClickCount(0);
        shortUrl.setMaxClicks(AppConfig.getDefaultMaxClicks());
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setExpiresAt(LocalDateTime.now().plusHours(AppConfig.getDefaultTTLHours()));
        shortUrl.setActive(true);

        urlRepository.put(shortCode, shortUrl);
        userUrls.computeIfAbsent(user.getId(), k -> new HashSet<>()).add(shortCode);

        return shortUrl;
    }

    public Optional<ShortUrl> redirect(String shortCode) {
        ShortUrl shortUrl = urlRepository.get(shortCode);

        if (shortUrl == null) {
            return Optional.empty();
        }

        if (LocalDateTime.now().isAfter(shortUrl.getExpiresAt())) {
            shortUrl.setActive(false);
            notificationService.notifyExpired(shortUrl);
            return Optional.empty();
        }

        if (shortUrl.getClickCount() >= shortUrl.getMaxClicks()) {
            shortUrl.setActive(false);
            notificationService.notifyClickLimitExceeded(shortUrl);
            return Optional.empty();
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);

        return Optional.of(shortUrl);
    }

    public List<ShortUrl> getUserUrls(UUID userId) {
        Set<String> userUrlCodes = userUrls.get(userId);
        if (userUrlCodes == null) {
            return Collections.emptyList();
        }

        return userUrlCodes.stream()
                .map(urlRepository::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean updateUrlMaxClicks(UUID userId, String shortCode, int newMaxClicks) {
        ShortUrl shortUrl = urlRepository.get(shortCode);

        if (shortUrl == null || !shortUrl.getUserId().equals(userId)) {
            return false;
        }

        shortUrl.setMaxClicks(newMaxClicks);
        return true;
    }

    public boolean deleteUrl(UUID userId, String shortCode) {
        ShortUrl shortUrl = urlRepository.get(shortCode);

        if (shortUrl == null || !shortUrl.getUserId().equals(userId)) {
            return false;
        }

        urlRepository.remove(shortCode);
        Set<String> urls = userUrls.get(userId);
        if (urls != null) {
            urls.remove(shortCode);
        }

        return true;
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void startCleanupTask() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredUrls();
            }
        }, 0, 60 * 1000);
    }

    private void cleanupExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredCodes = urlRepository.values().stream()
                .filter(url -> now.isAfter(url.getExpiresAt()))
                .map(ShortUrl::getShortCode)
                .toList();

        for (String code : expiredCodes) {
            ShortUrl expiredUrl = urlRepository.remove(code);
            if (expiredUrl != null) {
                userUrls.getOrDefault(expiredUrl.getUserId(), Collections.emptySet())
                        .remove(code);
                notificationService.notifyExpired(expiredUrl);
            }
        }
    }

    public boolean canUserAccessUrl(UUID userId, String shortCode) {
        ShortUrl shortUrl = urlRepository.get(shortCode);
        return shortUrl != null && shortUrl.getUserId().equals(userId);
    }

    public List<ShortUrl> getAllUrls() {
        return new ArrayList<>(urlRepository.values());
    }

    public int getTotalUrls() {
        return urlRepository.size();
    }

    public Map<UUID, Integer> getUserUrlStats() {
        Map<UUID, Integer> stats = new HashMap<>();
        for (ShortUrl url : urlRepository.values()) {
            stats.merge(url.getUserId(), 1, Integer::sum);
        }
        return stats;
    }
}