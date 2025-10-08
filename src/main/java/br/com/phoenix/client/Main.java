package br.com.phoenix.client;

import br.com.phoenix.client.model.Customer;
import br.com.phoenix.client.net.ApiHttpClient;
import br.com.phoenix.client.service.AuthService;
import br.com.phoenix.client.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/customers", new CustomersHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.printf("Phoenix Web Client running at http://localhost:%d%n", port);
    }

    private static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "POST");
                return;
            }
            try (InputStream is = exchange.getRequestBody()) {
                ObjectNode node = MAPPER.readValue(is, ObjectNode.class);
                String username = node.path("username").asText("");
                String password = node.path("password").asText("");

                ApiHttpClient http = new ApiHttpClient();
                AuthService auth = new AuthService(http);
                String token = auth.login(username, password);

                Map<String, String> resp = Map.of("token", token);
                sendJson(exchange, 200, resp);
            } catch (Exception ex) {
                sendError(exchange, 401, ex.getMessage());
            }
        }
    }

    private static class CustomersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String token = extractToken(exchange.getRequestHeaders());
            if (token == null) {
                sendError(exchange, 401, "Missing or invalid Authorization header");
                return;
            }

            ApiHttpClient http = new ApiHttpClient();
            http.setToken(token);
            CustomerService service = new CustomerService(http);

            try {
                switch (method.toUpperCase()) {
                    case "GET" -> handleGet(exchange, service);
                    case "POST" -> handlePost(exchange, service);
                    case "PUT" -> handlePut(exchange, service, path);
                    case "DELETE" -> handleDelete(exchange, service, path);
                    case "OPTIONS" -> handleOptions(exchange);
                    default -> sendMethodNotAllowed(exchange, "GET, POST, PUT, DELETE, OPTIONS");
                }
            } catch (Exception ex) {
                sendError(exchange, 500, ex.getMessage());
            }
        }

        private void handleOptions(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Allow", "GET, POST, PUT, DELETE, OPTIONS");
            headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            sendNoContent(exchange);
        }

        private void handleGet(HttpExchange exchange, CustomerService service) throws Exception {
            List<Customer> list = service.list();
            sendJson(exchange, 200, list);
        }

        private void handlePost(HttpExchange exchange, CustomerService service) throws Exception {
            try (InputStream is = exchange.getRequestBody()) {
                Customer payload = MAPPER.readValue(is, Customer.class);
                Customer created = service.create(payload.name, payload.email);
                sendJson(exchange, 201, created);
            }
        }

        private void handlePut(HttpExchange exchange, CustomerService service, String path) throws Exception {
            Long id = resolveId(path);
            if (id == null) {
                sendError(exchange, 400, "Invalid customer id");
                return;
            }
            try (InputStream is = exchange.getRequestBody()) {
                Customer payload = MAPPER.readValue(is, Customer.class);
                Customer updated = service.update(id, payload.name, payload.email);
                sendJson(exchange, 200, updated);
            }
        }

        private void handleDelete(HttpExchange exchange, CustomerService service, String path) throws Exception {
            Long id = resolveId(path);
            if (id == null) {
                sendError(exchange, 400, "Invalid customer id");
                return;
            }
            service.delete(id);
            sendNoContent(exchange);
        }

        private Long resolveId(String path) {
            String[] parts = path.split("/");
            if (parts.length < 4) {
                return null;
            }
            try {
                return Long.parseLong(parts[3]);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private static final Map<String, String> MIME_TYPES = new HashMap<>();

        static {
            MIME_TYPES.put(".html", "text/html; charset=utf-8");
            MIME_TYPES.put(".js", "application/javascript; charset=utf-8");
            MIME_TYPES.put(".css", "text/css; charset=utf-8");
            MIME_TYPES.put(".json", "application/json; charset=utf-8");
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "GET");
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            String resourcePath = "web" + path;
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    sendNotFound(exchange);
                    return;
                }
                byte[] body = stream.readAllBytes();
                String mime = MIME_TYPES.getOrDefault(getExtension(path), URLConnection.guessContentTypeFromName(path));
                if (mime == null) {
                    mime = "application/octet-stream";
                }
                exchange.getResponseHeaders().set("Content-Type", mime);
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        }

        private String getExtension(String path) {
            int idx = path.lastIndexOf('.') ;
            return idx >= 0 ? path.substring(idx) : "";
        }
    }

    private static void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        byte[] body = MAPPER.writer().writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        ObjectNode error = MAPPER.createObjectNode();
        error.put("error", message == null ? "" : message);
        sendJson(exchange, status, error);
    }

    private static void sendNotFound(HttpExchange exchange) throws IOException {
        sendError(exchange, 404, "Not found");
    }

    private static void sendMethodNotAllowed(HttpExchange exchange, String allowed) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowed);
        sendError(exchange, 405, "Method not allowed");
    }

    private static void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static String extractToken(Headers headers) {
        String header = headers.getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
