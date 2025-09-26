package br.com.phoenix.client.net;

import br.com.phoenix.client.config.AppConfig;
import okhttp3.*;

import java.io.IOException;

public class ApiHttpClient {

    private final OkHttpClient client = new OkHttpClient();
    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    private Request.Builder base(String path) {
        Request.Builder b = new Request.Builder()
                .url(AppConfig.API_BASE_URL + path);
        if (token != null && !token.isBlank()) {
            b.header("Authorization", "Bearer " + token);
        }
        return b;
    }

    private static String bodyToString(Response resp) throws IOException {
        ResponseBody rb = resp.body();
        return rb == null ? "" : rb.string();
    }

    public String postJson(String path, String json) throws IOException {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request req = base(path).post(body).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String msg = "HTTP " + resp.code() + " - " + resp.message();
                String respBody = bodyToString(resp);
                throw new IOException(msg + (respBody.isEmpty() ? "" : (": " + respBody)));
            }
            return bodyToString(resp);
        }
    }

    public String get(String path) throws IOException {
        Request req = base(path).get().build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String msg = "HTTP " + resp.code() + " - " + resp.message();
                String respBody = bodyToString(resp);
                throw new IOException(msg + (respBody.isEmpty() ? "" : (": " + respBody)));
            }
            return bodyToString(resp);
        }
    }

    public String putJson(String path, String json) throws IOException {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request req = base(path).put(body).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String msg = "HTTP " + resp.code() + " - " + resp.message();
                String respBody = bodyToString(resp);
                throw new IOException(msg + (respBody.isEmpty() ? "" : (": " + respBody)));
            }
            return bodyToString(resp);
        }
    }

    public void delete(String path) throws IOException {
        Request req = base(path).delete().build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String msg = "HTTP " + resp.code() + " - " + resp.message();
                String respBody = bodyToString(resp);
                throw new IOException(msg + (respBody.isEmpty() ? "" : (": " + respBody)));
            }
        }
    }

}
