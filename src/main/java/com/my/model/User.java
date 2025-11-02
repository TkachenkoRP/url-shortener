package com.my.model;

import java.util.UUID;

public class User {
    private final UUID id;

    public User() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               '}';
    }
}
