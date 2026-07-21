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

    // helper to check response status and throw readable exceptions
    private static void checkResponse(HttpResponse<?> response, String action) {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            String body = response.body() != null ? response.body().toString() : "";
            throw new RuntimeException(action + " failed (http " + code + "): " + body);
        }
    }

    public static class ApiResult {
        public boolean success;
        public String message;
        public String username;
        public String role;
        public String token;
    }

    /**
     * authenticate a user with username and password.
     *
     * @param username the user's login name
     * @param password the user's password
     * @return ApiResult containing token and user info
     * @throws Exception if the request fails
     */
    public static ApiResult login(String username, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "login");
        return parseAuthResponse(response);
    }

    /**
     * register a new user account.
     *
     * @param fullName the user's full name
     * @param username desired unique username
     * @param password account password
     * @param phone    contact phone number
     * @param email    contact email address
     * @return ApiResult containing token and user info
     * @throws Exception if the request fails
     */
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
        checkResponse(response, "signup");
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

    /**
     * advanced search with multiple filters (keyword, category, city, price range).
     *
     * @param filter the search criteria
     * @return list of matching active ads
     * @throws Exception if the request fails
     */
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
        checkResponse(response, "search ads");
        return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
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
        checkResponse(response, "load categories");
        return gson.fromJson(response.body(), new TypeToken<List<Category>>() {}.getType());
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
        checkResponse(response, "create category");
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
        checkResponse(response, "update category");
    }

    public static void deleteCategory(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/categories/" + id))
                .header("Authorization", "Bearer " + Session.getToken())
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "delete category");
    }

    /**
     * create a new advertisement with optional images.
     *
     * @param title       ad title
     * @param description ad description
     * @param price       price as string
     * @param categoryId  category id
     * @param cityId      city id
     * @param imageFiles  list of image file paths (can be empty)
     * @throws Exception if the request fails
     */
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
        checkResponse(response, "create ad");
    }

    /**
     * fetch all ads posted by the currently logged-in user.
     *
     * @return list of user's own ads (all statuses)
     * @throws Exception if the request fails
     */
    public static List<Ad> getMyAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads/my"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "get my ads");
        return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
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

    /**
     * fetch all conversations for the current user.
     *
     * @return list of chat summaries with last message preview
     * @throws Exception if the request fails
     */
    public static List<ChatSummary> getChats() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/chat"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "load conversations");
        return gson.fromJson(response.body(), new TypeToken<List<ChatSummary>>() {}.getType());
    }

    public static ChatDetail getChat(Long chatId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/chat/" + chatId))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "load conversation");
        return gson.fromJson(response.body(), ChatDetail.class);
    }

    /**
     * send a first message to a seller (creates a new conversation if needed).
     *
     * @param advertisementId the ad id
     * @param message         the message text
     * @throws Exception if the request fails
     */
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
        checkResponse(response, "send message");
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
        checkResponse(response, "reply to chat");
    }

    public static List<Ad> getFavorites() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/favorites"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "load favorites");
        return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
    }

    public static void addFavorite(Long advertisementId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/favorites/" + advertisementId))
                .header("Authorization", "Bearer " + Session.getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "add favorite");
    }

    public static void removeFavorite(Long advertisementId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/favorites/" + advertisementId))
                .header("Authorization", "Bearer " + Session.getToken())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "remove favorite");
    }

    /**
     * fetch all ads waiting for admin approval.
     *
     * @return list of pending ads
     * @throws Exception if the request fails
     */
    public static List<Ad> getPendingAds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/ads/pending"))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "load pending ads");
        return gson.fromJson(response.body(), new TypeToken<List<Ad>>() {}.getType());
    }

    /**
     * approve a pending ad (admin only).
     *
     * @param advertisementId the ad id
     * @throws Exception if the request fails
     */
    public static void approveAd(Long advertisementId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/ads/" + advertisementId + "/approve"))
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "approve ad");
    }

    /**
     * reject a pending ad with a reason (admin only).
     *
     * @param advertisementId the ad id
     * @param reason          rejection reason (required)
     * @throws Exception if the request fails
     */
    public static void rejectAd(Long advertisementId, String reason) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("reason", reason);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/ads/" + advertisementId + "/reject"))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "reject ad");
    }

    /**
     * submit a rating for a seller (1-5) with an optional comment.
     *
     * @param adId    the advertisement id
     * @param score   rating from 1 to 5
     * @param comment optional text (can be null)
     * @throws Exception if the request fails (e.g. duplicate rating)
     */
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
        checkResponse(response, "rate seller");
    }

    /**
     * fetch detailed information for a single ad (including seller info and rating).
     *
     * @param adId the advertisement id
     * @return detailed ad data
     * @throws Exception if the request fails or ad not found
     */
    public static AdDetail getAdDetails(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ads/" + adId))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "get ad details");
        return gson.fromJson(response.body(), AdDetail.class);
    }

    /**
     * admin-only endpoint to fetch any ad (including pending/rejected) by id.
     *
     * @param adId the advertisement id
     * @return detailed ad data with full admin visibility
     * @throws Exception if the request fails
     */
    public static AdDetail getAdminAdDetails(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/admin/ads/" + adId))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "get admin ad details");
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
        checkResponse(response, "load image");
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
        checkResponse(response, "update ad");
    }

    public static List<City> getCities() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/cities"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "load cities");
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
        checkResponse(response, "mark ad as sold");
    }

    public static void deleteAd(Long adId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/ads/" + adId))
                .header("Authorization", "Bearer " + Session.getToken())
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "delete ad");
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
        checkResponse(response, "load users");
        return gson.fromJson(response.body(), new TypeToken<List<AdminUser>>() {}.getType());
    }

    public static void blockUser(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/users/" + userId + "/block"))
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "block user");
    }

    public static void unblockUser(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/admin/users/" + userId + "/unblock"))
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response, "unblock user");
    }
}
