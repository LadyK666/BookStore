package com.bookstore.web.dto;

/**
 * 登录请求 DTO，对应 LoginView 中的用户名/密码/用户类型。
 */
public class LoginRequest {
    private String username;
    private String password;
    /**
     * 顾客: CUSTOMER; 管理员: ADMIN
     */
    private String userType;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}


