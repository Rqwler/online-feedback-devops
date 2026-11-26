package com.example.feedback;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Minimal server that serves the feedback form and acknowledges submissions. */
public class App {

    public static String thankYouMessage(String name) {
        String safe = (name == null || name.isBlank()) ? "friend" : name.replaceAll("[<>&\"']", "");
        return "<html><body><h2>Thank you, " + safe + "!</h2><p>Your feedback was received.</p>"
                + "<a href=\"/\">Back to form</a></body></html>";
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            byte[] body;
            try (InputStream in = App.class.getResourceAsStream("/feedback.html")) {
                body = in.readAllBytes();
            }
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.createContext("/submit", exchange -> {
            String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String name = "";
            for (String pair : form.split("&")) {
                if (pair.startsWith("name=")) {
                    name = java.net.URLDecoder.decode(pair.substring(5), StandardCharsets.UTF_8);
                }
            }
            byte[] body = thankYouMessage(name).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.start();
        System.out.println("Feedback app running on http://localhost:" + port);
    }
}
