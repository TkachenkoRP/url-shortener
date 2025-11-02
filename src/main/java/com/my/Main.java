package com.my;

import com.my.controller.CliController;
import com.my.service.NotificationService;
import com.my.service.UrlService;
import com.my.service.UserService;

public class Main {
    public static void main(String[] args) {
        try {
            NotificationService notificationService = new NotificationService();
            UrlService urlService = new UrlService(notificationService);
            UserService userService = new UserService();

            CliController cliController = new CliController(urlService, userService);
            cliController.start();

        } catch (Exception e) {
            System.err.println("Критическая ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}