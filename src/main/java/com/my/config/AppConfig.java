package com.my.config;

import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    private AppConfig() {
    }

    static {
        try {
            props.load(AppConfig.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception e) {
            props.setProperty("url.ttl.hours", "24");
            props.setProperty("url.ttl.max.hours", "720");
            props.setProperty("url.max.clicks.default", "100");
            props.setProperty("url.short.code.length", "6");
            props.setProperty("base.url", "my");
        }
    }

    public static int getDefaultTTLHours() {
        return Integer.parseInt(props.getProperty("url.ttl.hours"));
    }

    public static int getMaxTTLHours() {
        return Integer.parseInt(props.getProperty("url.ttl.max.hours"));
    }

    public static int getDefaultMaxClicks() {
        return Integer.parseInt(props.getProperty("url.max.clicks.default"));
    }

    public static int getShortCodeLength() {
        return Integer.parseInt(props.getProperty("url.short.code.length"));
    }

    public static String getBaseUrl() {
        return props.getProperty("base.url");
    }
}
