package models;

import java.util.List;

public class User {
    public String userId;
    public String username;
    public String roleName;
    public int devScore;
    public int qaScore;

    public User() {}

    public User(String userId, String username, String roleName, int devScore, int qaScore) {
        this.userId = userId;
        this.username = username;
        this.roleName = roleName;
        this.devScore = devScore;
        this.qaScore = qaScore;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRoleName() { return roleName; }
    public int getDevScore() { return devScore; }
    public int getQaScore() { return qaScore; }

    @Override
    public String toString() {
        return String.format("%s (%s) - Dev: %d | QA: %d", username, roleName, devScore, qaScore);
    }



    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    public static List<User> getUsers() {
        try {
            String json = Service.APIClient.get("/user/members?type=dev");
            return OBJECT_MAPPER.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<User>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public static User findUserById(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            String json = Service.APIClient.get("/profile?id=" + id);
            return OBJECT_MAPPER.readValue(json, User.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // get updated data
    public void fetch() {
        User updated = findUserById(this.userId);
        if (updated != null) {
            this.username = updated.username;
            this.roleName = updated.roleName;
            this.devScore = updated.devScore;
            this.qaScore = updated.qaScore;
        }
    }

    // push to server current changes
    public void push() {
        try {
            String jsonBody = String.format("{\"role\": \"%s\"}", this.roleName);
            Service.APIClient.patch("/user/" + this.userId, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
