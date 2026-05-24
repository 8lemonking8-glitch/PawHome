package com.example.midtermproject.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pets")
public class PetEntity {

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_ADOPTED = "ADOPTED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String type;        
    private String breed;
    private String color;
    private String size;        
    private String age;
    private String gender;      
    private String description;
    private int imageResId;
    private String imageResIds; 
    private String status;      
    private long createdAt;

    public PetEntity() {
        this.status = STATUS_AVAILABLE;
        this.createdAt = System.currentTimeMillis();
    }

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

    public boolean isAvailable() {
        return STATUS_AVAILABLE.equals(status);
    }
}
