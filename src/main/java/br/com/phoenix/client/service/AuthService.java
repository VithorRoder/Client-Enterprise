package br.com.phoenix.client.service;

import br.com.phoenix.client.net.ApiHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AuthService {

    private final ApiHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthService(ApiHttpClient http) {
        this.http = http;
    }

    public String login(String username, String password) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("username", username);
        payload.put("password", password);

        String resp = http.postJson("/api/auth/login", mapper.writeValueAsString(payload));
        JsonNode node = mapper.readTree(resp);
        String token = node.get("token").asText();
        http.setToken(token);
        return token;
    }
}
