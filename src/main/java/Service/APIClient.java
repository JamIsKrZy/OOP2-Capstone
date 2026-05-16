package Service;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class APIClient {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String BASE_URL = dotenv.get("BASE_URL");

//    public static String post(HttpRequest.Builder reqBuilder, String endpoint){
//        String fullUrl = BASE_URL + endpoint;
//
//        HttpRequest request = reqBuilder
//            .uri(URI.create(fullUrl))
//            .header("Accept", "application/json")
//            .POST()
//            .build();
//
//        HttpResponse<String> response = getClient().send(
//            request,
//            HttpResponse.BodyHandlers.ofString()
//        );
//
//        return response.body();
//    }

    public static String get(String endpoint) throws Exception {
        // Build the full URL by combining Base + Endpoint
        String fullUrl = BASE_URL + endpoint;
        System.out.println(fullUrl);

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        return response.body();
    }


    private static class Holder {
        private static final HttpClient INSTANCE = HttpClient.newBuilder()
                .executor(java.util.concurrent.Executors.newFixedThreadPool(10)) // Optional: Custom thread pool
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static HttpClient getClient() {
        return Holder.INSTANCE;
    }
}
