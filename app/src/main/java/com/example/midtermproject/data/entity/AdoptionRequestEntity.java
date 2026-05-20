package com.example.midtermproject.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "adoption_requests",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = PetEntity.class,
            parentColumns = "id",
            childColumns = "petId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("userId"),
        @Index("petId")
    }
)
public class AdoptionRequestEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long userId;
    private long petId;
    private String status;          // PENDING, APPROVED, REJECTED
    private boolean agreementAccepted;
    private String signaturePath;   // File path to signature bitmap
    private long signatureTimestamp;
    private long createdAt;
    private long reviewedAt;

    public AdoptionRequestEntity() {
        this.status = "PENDING";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getPetId() { return petId; }
    public String getStatus() { return status; }
    public boolean isAgreementAccepted() { return agreementAccepted; }
    public String getSignaturePath() { return signaturePath; }
    public long getSignatureTimestamp() { return signatureTimestamp; }
    public long getCreatedAt() { return createdAt; }
    public long getReviewedAt() { return reviewedAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setPetId(long petId) { this.petId = petId; }
    public void setStatus(String status) { this.status = status; }
    public void setAgreementAccepted(boolean agreementAccepted) { this.agreementAccepted = agreementAccepted; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }
    public void setSignatureTimestamp(long signatureTimestamp) { this.signatureTimestamp = signatureTimestamp; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setReviewedAt(long reviewedAt) { this.reviewedAt = reviewedAt; }

    /** Check if this request is still pending */
    public boolean isPending() {
        return "PENDING".equals(status);
    }
}
