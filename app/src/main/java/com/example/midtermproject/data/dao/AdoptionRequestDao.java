package com.example.midtermproject.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midtermproject.data.entity.AdoptionRequestEntity;
import com.example.midtermproject.data.entity.AdoptionRequestWithDetails;

import java.util.List;

@Dao
public interface AdoptionRequestDao {

    @Insert
    long insert(AdoptionRequestEntity request);

    @Update
    void update(AdoptionRequestEntity request);

    @Query("SELECT * FROM adoption_requests ORDER BY createdAt DESC")
    LiveData<List<AdoptionRequestEntity>> getAllRequests();

    @androidx.room.Transaction
    @Query("SELECT * FROM adoption_requests ORDER BY createdAt DESC")
    LiveData<List<AdoptionRequestWithDetails>> getAllRequestsWithDetails();

    @Query("SELECT * FROM adoption_requests WHERE status = 'PENDING' ORDER BY createdAt DESC")
    LiveData<List<AdoptionRequestEntity>> getPendingRequests();

    @androidx.room.Transaction
    @Query("SELECT * FROM adoption_requests WHERE status = 'PENDING' ORDER BY createdAt DESC")
    LiveData<List<AdoptionRequestWithDetails>> getPendingRequestsWithDetails();

    @Query("SELECT * FROM adoption_requests WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<AdoptionRequestEntity>> getRequestsByUser(long userId);

    @Query("SELECT * FROM adoption_requests WHERE userId = :userId ORDER BY createdAt DESC")
    List<AdoptionRequestEntity> getRequestsByUserSync(long userId);

    @Query("SELECT * FROM adoption_requests WHERE petId = :petId ORDER BY createdAt DESC")
    LiveData<List<AdoptionRequestEntity>> getRequestsByPet(long petId);

    @Query("SELECT * FROM adoption_requests WHERE id = :id")
    LiveData<AdoptionRequestEntity> getRequestById(long id);

    @Query("SELECT * FROM adoption_requests WHERE id = :id")
    AdoptionRequestEntity getRequestByIdSync(long id);

    @Query("SELECT * FROM adoption_requests WHERE userId = :userId AND petId = :petId AND status = 'PENDING' LIMIT 1")
    AdoptionRequestEntity getPendingRequestForPet(long userId, long petId);

    @Query("SELECT COUNT(*) FROM adoption_requests WHERE status = 'PENDING'")
    LiveData<Integer> getPendingCount();

    @Query("SELECT COUNT(*) FROM adoption_requests")
    LiveData<Integer> getTotalRequestCount();

    @Query("SELECT COUNT(*) FROM adoption_requests WHERE status = 'APPROVED'")
    LiveData<Integer> getApprovedCount();

    @Query("UPDATE adoption_requests SET status = :status, reviewedAt = :reviewedAt WHERE id = :requestId")
    void updateRequestStatus(long requestId, String status, long reviewedAt);
}
