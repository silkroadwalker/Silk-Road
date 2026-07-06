package com.circlemarketplace.api;

import com.circlemarketplace.model.Ad;
import com.circlemarketplace.ui.Session;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final Gson gson = new Gson();

    public static class ApiResult {
        public boolean success;
        public String message;
        public String username;
        public String role;
        public String token;
    }

    public static ApiResult login(String username, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseAuthResponse(response);
    }

    public static ApiResult signup(String fullName, String username, String password, String phone, String email) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("fullName", fullName);
        body.addProperty("username", username);
        body.addProperty("password", password);
        body.addProperty("phone", phone);
        body.addProperty("email", email);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/auth/signup"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseAuthResponse(response);
    }

    private static ApiResult parseAuthResponse(HttpResponse<String> response) {
        ApiResult result = new ApiResult();
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);

        result.message = json.has("message") ? json.get("message").getAsString() : null;
        result.username = json.has("username") ? json.get("username").getAsString() : null;
        result.role = json.has("role") ? json.get("role").getAsString() : null;
        result.token = json.has("token") ? json.get("token").getAsString() : null;

        result.success = response.statusCode() >= 200 && response.statusCode() < 300;
        return result;
    }

    public static List<Ad> getAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads")) // not sure
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
        } else {
            throw new RuntimeException("Failed to load ads: " + response.statusCode());
        }
    }

    public static List<Ad> searchAds(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads/search?query=" + encodedQuery)) // not sure
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
        } else {
            throw new RuntimeException("Search failed: " + response.statusCode());
        }
    }
}