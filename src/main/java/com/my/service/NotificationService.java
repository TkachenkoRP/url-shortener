package com.my.service;

import com.my.model.ShortUrl;

public class NotificationService {

    public void notifyExpired(ShortUrl shortUrl) {
        System.out.printf("[NOTIFICATION] Ссылка %s истекла. Время жизни закончилось.%n",
                shortUrl.getShortCode());
    }

    public void notifyClickLimitExceeded(ShortUrl shortUrl) {
        System.out.printf("[NOTIFICATION] Ссылка %s достигла лимита переходов (%d).%n",
                shortUrl.getShortCode(), shortUrl.getMaxClicks());
    }

    public void notifyUrlCreated(ShortUrl shortUrl) {
        System.out.printf("[NOTIFICATION] Создана новая ссылка: %s -> %s%n",
                shortUrl.getShortCode(), shortUrl.getOriginalUrl());
    }
}
