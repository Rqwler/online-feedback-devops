package com.example.feedback;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Minimal server that serves the feedback form and acknowledges submissions. */
public class App {

    private static final String FORM_RESOURCE = "/feedback.html";

    private static final String THANK_YOU_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Feedback received</title>
              <style>
                :root { color-scheme: light; }
                body { font-family: Arial, sans-serif; background: #eef3fb; margin: 0; padding: 2rem; color: #1f2937; }
                .card { max-width: 520px; margin: 0 auto; background: #fff; padding: 2rem; border-radius: 12px; box-shadow: 0 10px 30px rgba(15, 23, 42, .12); }
                h1 { margin-top: 0; color: #0f766e; }
                dl { display: grid; grid-template-columns: 120px 1fr; gap: .6rem 1rem; margin: 1.5rem 0; }
                dt { font-weight: bold; color: #475569; }
                dd { margin: 0; overflow-wrap: anywhere; }
                .button { display: inline-block; padding: .7rem 1.2rem; background: #1a73e8; color: #fff; text-decoration: none; border-radius: 6px; }
                .button:hover { background: #1558b0; }
              </style>
            </head>
            <body>
              <main class="card">
                <h1>Thank you, {{name}}!</h1>
                <p>Your feedback was received.</p>
                <dl>
                  <dt>Email</dt><dd>{{email}}</dd>
                  <dt>Category</dt><dd>{{category}}</dd>
                  <dt>Rating</dt><dd>{{rating}}</dd>
                  <dt>Comments</dt><dd>{{comments}}</dd>
                </dl>
                <a class="button" href="/">Submit another response</a>
              </main>
            </body>
            </html>
            """;

    public static String thankYouMessage(String name) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", name);
        return thankYouPage(fields);
    }

    public static String thankYouPage(Map<String, String> fields) {
        String name = value(fields, "name", "friend");
        String email = value(fields, "email", "Not provided");
        String category = value(fields, "category", "General");
        String rating = value(fields, "rating", "Not provided");
        String comments = value(fields, "comments", "No comments provided.");
        String ratingDisplay = rating.matches("[1-5]") ? rating + " / 5" : rating;

        return THANK_YOU_TEMPLATE
                .replace("{{name}}", escapeHtml(name))
                .replace("{{email}}", escapeHtml(email))
                .replace("{{category}}", escapeHtml(category))
                .replace("{{rating}}", escapeHtml(ratingDisplay))
                .replace("{{comments}}", escapeHtml(comments).replace("\n", "<br>"));
    }

    public static Map<String, String> parseForm(String form) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (form == null || form.isBlank()) {
            return fields;
        }
        for (String pair : form.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int separator = pair.indexOf('=');
            String rawKey = separator >= 0 ? pair.substring(0, separator) : pair;
            String rawValue = separator >= 0 ? pair.substring(separator + 1) : "";
            fields.put(decode(rawKey), decode(rawValue));
        }
        return fields;
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    private static String value(Map<String, String> fields, String key, String fallback) {
        String value = fields.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", App::handle);
        server.start();
        System.out.println("Feedback app running on http://localhost:" + port);
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("PORT");
        return envPort == null || envPort.isBlank() ? 8080 : Integer.parseInt(envPort);
    }

    private static void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            switch (path) {
                case "/", "/index.html", "/feedback.html" -> serveForm(exchange);
                case "/health" -> send(exchange, 200, "application/json", "{\"status\":\"UP\"}");
                case "/submit" -> handleSubmit(exchange);
                default -> send(exchange, 404, "text/html; charset=utf-8", "<h1>404 Not Found</h1>");
            }
        } finally {
            exchange.close();
        }
    }

    private static void serveForm(HttpExchange exchange) throws IOException {
        try (InputStream in = App.class.getResourceAsStream(FORM_RESOURCE)) {
            if (in == null) {
                send(exchange, 500, "text/plain; charset=utf-8", "feedback.html is missing from the jar");
                return;
            }
            send(exchange, 200, "text/html; charset=utf-8", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static void handleSubmit(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "POST");
            send(exchange, 405, "text/html; charset=utf-8", "<h1>405 Method Not Allowed</h1>");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        send(exchange, 200, "text/html; charset=utf-8", thankYouPage(parseForm(body)));
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
