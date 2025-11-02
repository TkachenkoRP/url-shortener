package com.my.service;

import com.my.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    public User getOrCreateUser(UUID userId) {
        return users.computeIfAbsent(userId, k -> new User());
    }

    public User getUser(UUID userId) {
        return users.get(userId);
    }

    public User createNewUser() {
        User user = new User();
        users.put(user.getId(), user);
        return user;
    }

    public int getTotalUsers() {
        return users.size();
    }

    public boolean userExists(UUID userId) {
        return users.containsKey(userId);
    }
}
