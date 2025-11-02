package service;

import com.my.model.ShortUrl;
import com.my.model.User;
import com.my.service.NotificationService;
import com.my.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlServiceTest {
    private UrlService urlService;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        NotificationService notificationService = new NotificationService();
        urlService = new UrlService(notificationService);
        user1 = new User();
        user2 = new User();
    }

    @Test
    void testCreateShortUrl() {
        String originalUrl = "https://www.example.com";
        ShortUrl shortUrl = urlService.createShortUrl(originalUrl, user1);

        assertThat(shortUrl).isNotNull();
        assertThat(shortUrl.getShortCode()).isNotNull();
        assertThat(shortUrl.getOriginalUrl()).isEqualTo(originalUrl);
        assertThat(shortUrl.getUserId()).isEqualTo(user1.getId());
        assertThat(shortUrl.isActive()).isTrue();
    }

    @Test
    void testUniqueUrlsForDifferentUsers() {
        String originalUrl = "https://www.example.com";

        ShortUrl url1 = urlService.createShortUrl(originalUrl, user1);
        ShortUrl url2 = urlService.createShortUrl(originalUrl, user2);

        assertThat(url1.getShortCode()).isNotEqualTo(url2.getShortCode());
    }

    @Test
    void testRedirectWithinLimit() {
        String originalUrl = "https://www.example.com";
        ShortUrl shortUrl = urlService.createShortUrl(originalUrl, user1);

        for (int i = 0; i < 5; i++) {
            assertThat(urlService.redirect(shortUrl.getShortCode())).isPresent();
        }

        ShortUrl updated = urlService.getUserUrls(user1.getId()).get(0);
        assertThat(updated.getClickCount()).isEqualTo(5);
    }

    @Test
    void testRedirectExceedsLimit() {
        String originalUrl = "https://www.example.com";
        ShortUrl shortUrl = urlService.createShortUrl(originalUrl, user1);

        urlService.updateUrlMaxClicks(user1.getId(), shortUrl.getShortCode(), 2);

        assertThat(urlService.redirect(shortUrl.getShortCode())).isPresent();
        assertThat(urlService.redirect(shortUrl.getShortCode())).isPresent();
        assertThat(urlService.redirect(shortUrl.getShortCode())).isEmpty();
    }
}
