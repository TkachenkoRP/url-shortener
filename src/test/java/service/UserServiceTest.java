package service;

import com.my.model.User;
import com.my.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void testCreateNewUser() {
        User user = userService.createNewUser();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(userService.userExists(user.getId())).isTrue();
    }

    @Test
    void testGetExistingUser() {
        User originalUser = userService.createNewUser();
        UUID userId = originalUser.getId();

        User retrievedUser = userService.getUser(userId);

        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(originalUser.getId());
    }

    @Test
    void testGetNonExistentUser() {
        UUID nonExistentId = UUID.randomUUID();
        User user = userService.getUser(nonExistentId);

        assertThat(user).isNull();
    }

    @Test
    void testGetOrCreateUser() {
        UUID userId = UUID.randomUUID();

        User user1 = userService.getOrCreateUser(userId);
        User user2 = userService.getOrCreateUser(userId);

        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();
        assertThat(user1.getId()).isEqualTo(user2.getId());
        assertThat(userService.getTotalUsers()).isEqualTo(1);
    }

    @Test
    void testMultipleUsers() {
        User user1 = userService.createNewUser();
        User user2 = userService.createNewUser();

        assertThat(user1.getId()).isNotEqualTo(user2.getId());
        assertThat(userService.getTotalUsers()).isEqualTo(2);
    }
}
