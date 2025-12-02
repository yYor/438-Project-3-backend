package com.example.BirdApp.dto;

public class SignupRequest {
    private String name;
    private String email;
    private String oauthProvider;
    private String oauthId;
    private String profilePicture;
    
    public SignupRequest() {}
    
    public SignupRequest(String name, String email, String oauthProvider, String oauthId, String profilePicture) {
        this.name = name;
        this.email = email;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.profilePicture = profilePicture;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getOauthProvider() {
        return oauthProvider;
    }
    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }
    public String getOauthId() {
        return oauthId;
    }
    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }
    public String getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
