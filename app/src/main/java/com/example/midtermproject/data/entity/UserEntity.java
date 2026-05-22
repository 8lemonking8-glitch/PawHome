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
    private String avatarUri;
    private String role;        // USER, ADMIN
    private long createdAt;

    private String gender;
    private int age;
    private String address;
    private String housingCondition;
    private String monthlyIncome;
    private String petExperience;

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
    
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getAddress() { return address; }
    public String getHousingCondition() { return housingCondition; }
    public String getMonthlyIncome() { return monthlyIncome; }
    public String getPetExperience() { return petExperience; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }
    public String getAvatarUri() { return avatarUri; }
    public void setAvatarUri(String avatarUri) { this.avatarUri = avatarUri; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public void setGender(String gender) { this.gender = gender; }
    public void setAge(int age) { this.age = age; }
    public void setAddress(String address) { this.address = address; }
    public void setHousingCondition(String housingCondition) { this.housingCondition = housingCondition; }
    public void setMonthlyIncome(String monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public void setPetExperience(String petExperience) { this.petExperience = petExperience; }

    /** Check if this user has completed their profile details */
    public boolean isProfileComplete() {
        return getMissingProfileFields().isEmpty();
    }

    /** Returns human-readable names of every incomplete Adopter Profile field */
    public java.util.List<String> getMissingProfileFields() {
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (email == null || email.trim().isEmpty()) missing.add("Email");
        if (phone == null || phone.trim().isEmpty()) missing.add("Phone");
        if (age <= 0) missing.add("Age");
        if (gender == null || gender.trim().isEmpty()) missing.add("Gender");
        if (address == null || address.trim().isEmpty()) missing.add("Home Address");
        if (housingCondition == null || housingCondition.trim().isEmpty()) missing.add("Housing Status");
        if (monthlyIncome == null || monthlyIncome.trim().isEmpty()) missing.add("Monthly Income");
        if (petExperience == null || petExperience.trim().isEmpty()) missing.add("Pet Care Experience");
        return missing;
    }

    /** Check if this user is an admin */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
