package com.my.controller;

import com.my.model.ShortUrl;
import com.my.model.User;
import com.my.service.UrlService;
import com.my.service.UserService;
import com.my.util.UrlShortener;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class CliController {
    private final UrlService urlService;
    private final UserService userService;
    private User currentUser;
    private final Scanner scanner;
    private boolean applicationRunning = true;

    public CliController(UrlService urlService, UserService userService) {
        this.urlService = urlService;
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Сервис коротких ссылок ===");

        while (applicationRunning) {
            if (currentUser == null) {
                handleUserSelection();
            } else {
                runUserSession();
            }
        }
    }

    private void handleUserSelection() {
        System.out.println("\n=== Выбор пользователя ===");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Войти по UUID");
        System.out.println("3. Выйти из приложения");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                createNewUser();
                break;
            case "2":
                loginByUuid();
                break;
            case "3":
                applicationRunning = false;
                break;
            default:
                System.out.println("Неверный выбор. Попробуйте снова.");
        }
    }

    private void createNewUser() {
        currentUser = userService.createNewUser();
        System.out.println("Создан новый пользователь с UUID: " + currentUser.getId());
        System.out.println("Запомните этот UUID для будущего входа!");
    }

    private void loginByUuid() {
        System.out.print("Введите ваш UUID: ");
        String uuidInput = scanner.nextLine().trim();

        if (uuidInput.isEmpty()) {
            System.out.println("UUID не может быть пустым.");
            return;
        }

        try {
            UUID userId = UUID.fromString(uuidInput);
            User user = userService.getUser(userId);

            if (user != null) {
                currentUser = user;
                System.out.println("Успешный вход! Добро пожаловать назад.");

                showUserStats();
            } else {
                System.out.println("Пользователь с таким UUID не найден.");
                System.out.println("Хотите создать нового пользователя? (y/n)");
                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("y") || response.equals("yes")) {
                    createNewUser();
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Неверный формат UUID.");
        }
    }

    private void runUserSession() {
        printWelcome();

        while (currentUser != null && applicationRunning) {
            printMenu();
            String command = scanner.nextLine().trim();

            try {
                handleCommand(command);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printWelcome() {
        System.out.println("\n=== Рабочая сессия ===");
        System.out.println("Текущий пользователь: " + currentUser.getId());
        showUserStats();
        System.out.println();
    }

    private void showUserStats() {
        List<ShortUrl> urls = urlService.getUserUrls(currentUser.getId());
        long activeUrls = urls.stream().filter(ShortUrl::isActive).count();

        System.out.printf("Статистика: %d ссылок (%d активных)%n",
                urls.size(), activeUrls);
    }

    private void printMenu() {
        System.out.println("Доступные команды:");
        System.out.println("1. shorten <URL> - Сократить ссылку");
        System.out.println("2. list - Показать мои ссылки");
        System.out.println("3. open <short-code> - Открыть ссылку");
        System.out.println("4. update <short-code> <new-clicks-limit> - Обновить лимит кликов");
        System.out.println("5. delete <short-code> - Удалить ссылку");
        System.out.println("6. stats - Показать статистику");
        System.out.println("7. logout - Выйти из текущей сессии");
        System.out.println("8. exit - Выйти из приложения");
        System.out.println("9. help - Показать справку");
        System.out.print("Введите команду: ");
    }

    private void handleCommand(String command) {
        if (command.equals("exit")) {
            applicationRunning = false;
            currentUser = null;
            return;
        }

        if (command.equals("logout")) {
            System.out.println("👋 Выход из сессии пользователя: " + currentUser.getId());
            currentUser = null;
            return;
        }

        String[] parts = command.split("\\s+", 2);
        String action = parts[0].toLowerCase();

        switch (action) {
            case "shorten":
                if (parts.length < 2) {
                    System.out.println("Использование: shorten <URL>");
                } else {
                    shortenUrl(parts[1]);
                }
                break;

            case "list":
                listUserUrls();
                break;

            case "open":
                if (parts.length < 2) {
                    System.out.println("Использование: open <short-code>");
                } else {
                    openUrl(parts[1]);
                }
                break;

            case "update":
                if (parts.length < 2) {
                    System.out.println("Использование: update <short-code> <new-clicks-limit>");
                } else {
                    String[] updateParts = parts[1].split("\\s+", 2);
                    if (updateParts.length < 2) {
                        System.out.println("Использование: update <short-code> <new-clicks-limit>");
                    } else {
                        updateUrl(updateParts[0], updateParts[1]);
                    }
                }
                break;

            case "delete":
                if (parts.length < 2) {
                    System.out.println("Использование: delete <short-code>");
                } else {
                    deleteUrl(parts[1]);
                }
                break;

            case "stats":
                showUserStats();
                break;

            case "help":
                printHelp();
                break;

            default:
                System.out.println("Неизвестная команда. Введите 'help' для справки.");
        }
    }

    private void shortenUrl(String originalUrl) {
        try {
            ShortUrl shortUrl = urlService.createShortUrl(originalUrl, currentUser);
            String fullShortUrl = UrlShortener.buildShortUrl(shortUrl.getShortCode());

            System.out.println("Ссылка успешно сокращена!");
            System.out.println("Оригинальная: " + originalUrl);
            System.out.println("Сокращенная: " + fullShortUrl);
            System.out.println("Код: " + shortUrl.getShortCode());
            System.out.println("Лимит переходов: " + shortUrl.getMaxClicks());
            System.out.println("Истекает: " + shortUrl.getExpiresAt());

        } catch (Exception e) {
            System.out.println("Ошибка при сокращении ссылки: " + e.getMessage());
        }
    }

    private void listUserUrls() {
        List<ShortUrl> urls = urlService.getUserUrls(currentUser.getId());

        if (urls.isEmpty()) {
            System.out.println("У вас нет сокращенных ссылок.");
            return;
        }

        System.out.println("Ваши сокращенные ссылки:");
        for (int i = 0; i < urls.size(); i++) {
            ShortUrl url = urls.get(i);
            String status = url.isActive() ? "Активна" : "Неактивна";
            String statusReason = "";

            if (!url.isActive()) {
                if (url.getClickCount() >= url.getMaxClicks()) {
                    statusReason = " (лимит переходов)";
                } else if (java.time.LocalDateTime.now().isAfter(url.getExpiresAt())) {
                    statusReason = " (время истекло)";
                }
            }

            System.out.printf("%d. %s -> %s%n", i + 1, url.getShortCode(), url.getOriginalUrl());
            System.out.printf("   Переходы: %d/%d | %s%s%n",
                    url.getClickCount(), url.getMaxClicks(), status, statusReason);
            System.out.printf("   Создана: %s | Истекает: %s%n",
                    url.getCreatedAt(), url.getExpiresAt());
            System.out.println();
        }
    }

    private void openUrl(String shortCode) {
        try {
            Optional<ShortUrl> shortUrlOpt = urlService.redirect(shortCode);

            if (shortUrlOpt.isPresent()) {
                ShortUrl shortUrl = shortUrlOpt.get();
                String originalUrl = shortUrl.getOriginalUrl();

                System.out.println("Перенаправление на: " + originalUrl);

                if (!shortUrl.getUserId().equals(currentUser.getId())) {
                    System.out.println("Внимание: эта ссылка принадлежит другому пользователю");
                }

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(originalUrl));
                        System.out.println("Ссылка открыта в браузере");
                    } catch (Exception e) {
                        System.out.println("Не удалось открыть в браузере: " + e.getMessage());
                    }
                } else {
                    System.out.println("Автоматическое открытие в браузере не поддерживается.");
                    System.out.println("Скопируйте ссылку: " + originalUrl);
                }
            } else {
                System.out.println("Ссылка не найдена или недоступна.");
            }

        } catch (Exception e) {
            System.out.println("Ошибка при открытии ссылки: " + e.getMessage());
        }
    }

    private void updateUrl(String shortCode, String newLimitStr) {
        try {
            int newLimit = Integer.parseInt(newLimitStr);

            if (newLimit <= 0) {
                System.out.println("Лимит должен быть положительным числом.");
                return;
            }

            boolean success = urlService.updateUrlMaxClicks(currentUser.getId(), shortCode, newLimit);

            if (success) {
                System.out.println("Лимит переходов для ссылки " + shortCode + " обновлен на " + newLimit);
            } else {
                System.out.println("Ссылка не найдена или у вас нет прав для ее изменения.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Неверный формат числа. Используйте целое число.");
        }
    }

    private void deleteUrl(String shortCode) {
        boolean success = urlService.deleteUrl(currentUser.getId(), shortCode);

        if (success) {
            System.out.println("Ссылка " + shortCode + " удалена.");
        } else {
            System.out.println("Ссылка не найдена или у вас нет прав для ее удаления.");
        }
    }

    private void printHelp() {
        System.out.println("=== Справка по командам ===");
        System.out.println("shorten <URL> - Создает короткую ссылку для указанного URL");
        System.out.println("list - Показывает все ваши сокращенные ссылки");
        System.out.println("open <code> - Открывает ссылку в браузере по короткому коду");
        System.out.println("update <code> <limit> - Изменяет лимит переходов для ссылки");
        System.out.println("delete <code> - Удаляет вашу ссылку");
        System.out.println("stats - Показывает статистику текущего пользователя");
        System.out.println("logout - Выход из текущей сессии (можно войти снова)");
        System.out.println("exit - Полный выход из приложения");
        System.out.println("help - Показывает эту справку");
    }
}