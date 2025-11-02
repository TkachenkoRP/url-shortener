package service;

import com.my.model.ShortUrl;
import com.my.model.User;
import com.my.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {
    private NotificationService notificationService;
    private User testUser;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        testUser = new User();
    }

    @Test
    void testNotificationCreation() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode("TEST123");
        shortUrl.setOriginalUrl("https://example.com");
        shortUrl.setUserId(testUser.getId());
        shortUrl.setMaxClicks(10);

        notificationService.notifyExpired(shortUrl);
        notificationService.notifyClickLimitExceeded(shortUrl);
        notificationService.notifyUrlCreated(shortUrl);

        assertThat(shortUrl.getShortCode()).isEqualTo("TEST123");
    }
}
