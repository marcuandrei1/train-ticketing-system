package com.trainticket.model;

public class User {
    private final String id;
    private String name;
    private String email;
    private UserRole role;

    public enum UserRole {
        CUSTOMER, ADMIN
    }

    public User(String id, String name, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getId() {
        return id;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", name, email, role);
    }
}
