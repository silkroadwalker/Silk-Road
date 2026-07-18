package com.silkroad.api;

import com.silkroad.model.*;
import com.silkroad.ui.Session;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Path;


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

    public static List<Ad> searchAds(String keyword) throws Exception {
        AdFilter filter = new AdFilter();
        filter.keyword = keyword;
        return searchAds(filter);
    }

    public static List<Ad> searchAds(AdFilter filter) throws Exception {
        StringBuilder query = new StringBuilder("/api/ads?");

        if (filter.keyword != null && !filter.keyword.isBlank()) {
            query.append("keyword=").append(URLEncoder.encode(filter.keyword, StandardCharsets.UTF_8)).append("&");
        }
        if (filter.categoryId != null) {
            query.append("categoryId=").append(filter.categoryId).append("&");
        }
        if (filter.cityId != null) {
            query.append("cityId=").append(filter.cityId).append("&");
        }
        if (filter.minPrice != null && !filter.minPrice.isBlank()) {
            query.append("minPrice=").append(URLEncoder.encode(filter.minPrice, StandardCharsets.UTF_8)).append("&");
        }
        if (filter.maxPrice != null && !filter.maxPrice.isBlank()) {
            query.append("maxPrice=").append(URLEncoder.encode(filter.maxPrice, StandardCharsets.UTF_8)).append("&");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + query))
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

    public static List<Ad> getAds() throws Exception {
        return searchAds(new AdFilter());
    }

    public static List<Category> getCategories() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/categories"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), new TypeToken<List<Category>>() {}.getType());
        } else {
            throw new RuntimeException("Failed to load categories: " + response.statusCode());
        }
    }

    public static void createCategory(String name) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/categories"))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static void updateCategory(Long id, String name) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/categories/" + id))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .method("PUT", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static void deleteCategory(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/categories/" + id))
                .header("Authorization", "Bearer " + Session.getToken())
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static void createAd(String title, String description, String price, Long categoryId, Long cityId,
                                List<Path> imageFiles) throws Exception {
        String boundary = "SilkRoadBoundary" + System.currentTimeMillis();

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("title", title);
        fields.put("description", description);
        fields.put("price", price);
        fields.put("categoryId", String.valueOf(categoryId));
        fields.put("cityId", String.valueOf(cityId));

        byte[] body = buildMultipartBody(boundary, fields, "images", imageFiles);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ads"))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static List<Ad> getMyAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads/my"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
        } else {
            throw new RuntimeException("Failed to load your ads: " + response.statusCode());
        }
    }

    private static byte[] buildMultipartBody(String boundary, Map<String, String> fields,
                                             String fileFieldName, List<Path> files) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.write((entry.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }

        if (files != null) {
            for (Path file : files) {
                String fileName = file.getFileName().toString();
                String contentType = Files.probeContentType(file);
                if (contentType == null) contentType = "application/octet-stream";

                out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.write(("Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + fileName + "\"\r\n")
                        .getBytes(StandardCharsets.UTF_8));
                out.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                out.write(Files.readAllBytes(file));
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }
        }

        out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

public static List<ChatSummary> getChats() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/chat"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), new TypeToken<List<ChatSummary>>() {}.getType());
        } else {
            throw new RuntimeException("Failed to load conversations: " + response.statusCode());
        }
    }

    public static ChatDetail getChat(Long chatId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/chat/" + chatId))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), ChatDetail.class);
        } else {
            throw new RuntimeException("Failed to load conversation: " + response.statusCode());
        }
    }

    public static void sendMessage(Long advertisementId, String message) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("advertisementId", advertisementId);
        body.addProperty("message", message);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/chat"))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to send message: " + response.statusCode());
        }
    }

    public static void replyToChat(Long chatId, String message) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("message", message);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/chat/" + chatId))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to send reply: " + response.statusCode());
        }
    }

    public static List<Ad> getFavorites() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/favorites"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
        } else {
            throw new RuntimeException("Failed to load favorites: " + response.statusCode());
        }
    }

    public static void addFavorite(Long advertisementId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/favorites/" + advertisementId))
                .header("Authorization", "Bearer " + Session.getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to add favorite: " + response.statusCode());
        }
    }

    public static void removeFavorite(Long advertisementId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/favorites/" + advertisementId))
                .header("Authorization", "Bearer " + Session.getToken())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to remove favorite: " + response.statusCode());
        }
    }

    public static List<Ad> getPendingAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/ads/pending"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
        } else {
            throw new RuntimeException("Failed to load pending ads: " + response.statusCode());
        }
    }

    public static void approveAd(Long advertisementId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/ads/" + advertisementId + "/approve"))
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to approve ad: " + response.statusCode());
        }
    }

    public static void rejectAd(Long advertisementId, String reason) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("reason", reason);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/ads/" + advertisementId + "/reject"))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to reject ad: " + response.statusCode());
        }
    }

    public static void rateSeller(Long adId, int score, String comment) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("score", score);
        body.addProperty("comment", comment);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ads/" + adId + "/rating"))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to rate seller (" + response.statusCode() + "): " + response.body());
        }
    }

    public static AdDetail getAdDetails(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ads/" + adId))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load ad details: " + response.statusCode());
        }

        return gson.fromJson(response.body(), AdDetail.class);
    }

    public static byte[] getImageBytes(String imageUrl) throws Exception {
        String path = imageUrl.startsWith("http") ? imageUrl.substring(imageUrl.indexOf("/api")) : imageUrl;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
        return response.body();
    }

    public static void updateAd(Long adId, String title, String description, String price,
                                Long categoryId, Long cityId) throws Exception {
        JsonObject body = new JsonObject();
        if (title != null) body.addProperty("title", title);
        if (description != null) body.addProperty("description", description);
        if (price != null) body.addProperty("price", new java.math.BigDecimal(price));
        if (categoryId != null) body.addProperty("categoryId", categoryId);
        if (cityId != null) body.addProperty("cityId", cityId);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads/" + adId))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static List<City> getCities() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/cities"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
        return gson.fromJson(response.body(), new TypeToken<List<City>>() {}.getType());
    }

    public static class AdFilter {
        public String keyword;
        public Long categoryId;
        public Long cityId;
        public String minPrice;
        public String maxPrice;
    }

    public static void markAdSold(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads/" + adId + "/markSold"))
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static void deleteAd(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads/" + adId))
                .header("Authorization", "Bearer " + Session.getToken())
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static class AdminUser {
        public Long id;
        public String username;
        public String fullName;
        public String status; // ACTIVE / BLOCKED
    }

    public static List<AdminUser> getUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/users"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
        return gson.fromJson(response.body(), new TypeToken<List<AdminUser>>() {}.getType());
    }

    public static void blockUser(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/users/" + userId + "/block"))
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }

    public static void unblockUser(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/users/" + userId + "/unblock"))
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to load image (" + response.statusCode() + ")");
        }
    }
}

