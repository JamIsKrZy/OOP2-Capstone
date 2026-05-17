package Service;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class APIClient {

    private static String baseUrl = "http://localhost:8080/api"; // Default
    private static final Path COOKIE_FILE = Paths.get("session_cookie.txt");
    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    static {
        // Try to load BASE_URL from .env if it exists, otherwise use default
        try {
            Dotenv dotenv = Dotenv.load();
            String envUrl = dotenv.get("BASE_URL");
            if (envUrl != null && !envUrl.isBlank()) {
                baseUrl = envUrl;
            }
        } catch (Exception e) {
            // Dotenv might fail if file missing, ignore
        }
        loadSession();
    }

    public static void setBaseUrl(String url) {
        if (url != null && !url.isBlank()) {
            baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        }
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String get(String endpoint) throws Exception {
        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        System.out.println("GET: " + fullUrl);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = getClient().send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        saveSession();
        return response.body();
    }

    public static String post(String endpoint, String jsonBody) throws Exception {
        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        System.out.println("POST: " + fullUrl);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = getClient().send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        saveSession();
        return response.body();
    }

    public static String patch(String endpoint, String jsonBody) throws Exception {
        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        System.out.println("PATCH: " + fullUrl);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = getClient().send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        saveSession();
        return response.body();
    }

    public static String delete(String endpoint) throws Exception {
        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        System.out.println("DELETE: " + fullUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Accept", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = getClient().send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        saveSession();
        return response.body();
    }

    public static HttpResponse<String> getRaw(String endpoint, boolean followRedirects) throws Exception {
        String fullUrl = baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .cookieHandler(cookieManager);
        
        if (!followRedirects) {
            builder.followRedirects(HttpClient.Redirect.NEVER);
        }

        HttpClient client = builder.build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        saveSession();
        return response;
    }

    public static void saveSession() {
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        for (HttpCookie cookie : cookies) {
            if ("sessionId".equals(cookie.getName())) {
                try {
                    Files.writeString(COOKIE_FILE, cookie.getValue(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.err.println("Failed to save session cookie: " + e.getMessage());
                }
                break;
            }
        }
    }

    public static void loadSession() {
        if (Files.exists(COOKIE_FILE)) {
            try {
                String sessionId = Files.readString(COOKIE_FILE, StandardCharsets.UTF_8).trim();
                if (!sessionId.isEmpty()) {
                    HttpCookie cookie = new HttpCookie("sessionId", sessionId);
                    cookie.setPath("/");
                    
                    try {
                        String host = URI.create(baseUrl).getHost();
                        if (host != null) cookie.setDomain(host);
                    } catch (Exception e) {}

                    cookieManager.getCookieStore().add(URI.create(baseUrl), cookie);
                }
            } catch (IOException e) {
                System.err.println("Failed to load session cookie: " + e.getMessage());
            }
        }
    }

    /**
     * Manually injects a sessionId cookie into the cookie store and persists it.
     * Used after OAuth when the Set-Cookie was received by the browser, not our app.
     */
    public static void setSession(String sessionId) {
        // Remove any stale sessionId cookies first
        cookieManager.getCookieStore().removeAll();

        HttpCookie cookie = new HttpCookie("sessionId", sessionId);
        cookie.setPath("/");
        cookie.setVersion(0);  // Netscape style — avoids strict domain matching
        // Associate with the base URI so it's sent on all /api/* requests
        cookieManager.getCookieStore().add(URI.create(baseUrl), cookie);
        saveSession();
    }

    public static void clearSession() {
        cookieManager.getCookieStore().removeAll();
        try {
            Files.deleteIfExists(COOKIE_FILE);
        } catch (IOException e) {
            System.err.println("Failed to clear session file: " + e.getMessage());
        }
    }

    private static class Holder {
        private static final HttpClient INSTANCE = HttpClient.newBuilder()
                .executor(java.util.concurrent.Executors.newFixedThreadPool(10))
                .connectTimeout(Duration.ofSeconds(10))
                .cookieHandler(cookieManager)
                .build();
    }

    public static HttpClient getClient() {
        return Holder.INSTANCE;
    }
}
