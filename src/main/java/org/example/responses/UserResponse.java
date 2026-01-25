package org.example.responses;

import org.example.Classes.User;

public class UserResponse extends BasicResponse{
    private User user;
    private String token;

    public UserResponse(boolean success, Integer errorCode, User user, String token) {
        super(success, errorCode);
        this.user = user;
        this.token = token;
    }
    public String getToken() {
        return token;
    }
}
