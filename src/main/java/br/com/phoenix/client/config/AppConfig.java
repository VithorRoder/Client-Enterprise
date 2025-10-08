package br.com.phoenix.client.config;

public final class AppConfig {

    public static final String API_BASE_URL =
        System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8081");

    private AppConfig() {
    }
}
