package com.example.midtermproject.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)})
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String username;
    private String password;    // SHA-256 hashed
    private String nickname;
    private String email;
    private String phone;
    private int avatarResId;
    private String role;        // USER, ADMIN
    private long createdAt;

    public UserEntity() {
        this.role = "USER";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public int getAvatarResId() { return avatarResId; }
    public String getRole() { return role; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /** Check if this user is an admin */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
