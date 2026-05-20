package com.example.midtermproject.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pets")
public class PetEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String type;        // DOG, CAT, BIRD
    private String breed;
    private String color;
    private String size;        // SMALL, MEDIUM, LARGE
    private String age;
    private String gender;      // MALE, FEMALE
    private String description;
    private int imageResId;
    private String imageResIds; // JSON array of drawable resource IDs for carousel
    private String status;      // AVAILABLE, ADOPTED
    private long createdAt;

    public PetEntity() {
        this.status = "AVAILABLE";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getBreed() { return breed; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
    public String getImageResIds() { return imageResIds; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setColor(String color) { this.color = color; }
    public void setSize(String size) { this.size = size; }
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDescription(String description) { this.description = description; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setImageResIds(String imageResIds) { this.imageResIds = imageResIds; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /** Helper to check availability */
    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }
}
