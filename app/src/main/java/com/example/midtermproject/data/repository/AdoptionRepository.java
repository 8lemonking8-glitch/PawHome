package com.example.midtermproject.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midtermproject.data.dao.AdoptionRequestDao;
import com.example.midtermproject.data.dao.PetDao;
import com.example.midtermproject.data.database.AppDatabase;
import com.example.midtermproject.data.entity.AdoptionRequestEntity;
import com.example.midtermproject.data.entity.AdoptionRequestWithDetails;

import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AdoptionRepository {

    private final AdoptionRequestDao adoptionDao;
    private final PetDao petDao;
    private final AppDatabase db;

    public AdoptionRepository(Application application) {
        db = AppDatabase.getInstance(application);
        adoptionDao = db.adoptionRequestDao();
        petDao = db.petDao();
    }

    // ===== Create =====

    public long createRequest(long userId, long petId, String signaturePath, long signatureTimestamp) {
        try {
            Future<Long> future = AppDatabase.databaseExecutor.submit(() -> {
                // Check if there's already a pending request
                AdoptionRequestEntity existing = adoptionDao.getPendingRequestForPet(userId, petId);
                if (existing != null) {
                    return -1L; // Already has a pending request
                }

                AdoptionRequestEntity request = new AdoptionRequestEntity();
                request.setUserId(userId);
                request.setPetId(petId);
                request.setAgreementAccepted(true);
                request.setSignaturePath(signaturePath);
                request.setSignatureTimestamp(signatureTimestamp);
                request.setStatus("PENDING");
                return adoptionDao.insert(request);
            });
            return future.get();
        } catch (ExecutionException e) {
            Log.e("AdoptionRepository", "Query failed", e);
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    // ===== Admin Actions =====

    public void approveRequest(long requestId, long petId) {
        AppDatabase.databaseExecutor.execute(() -> {
            db.runInTransaction(() -> {
                adoptionDao.updateRequestStatus(requestId, "APPROVED", System.currentTimeMillis());
                petDao.updatePetStatus(petId, "ADOPTED");
            });
        });
    }

    public void rejectRequest(long requestId) {
        AppDatabase.databaseExecutor.execute(() -> {
            adoptionDao.updateRequestStatus(requestId, "REJECTED", System.currentTimeMillis());
        });
    }

    // ===== Read Operations =====

    public LiveData<List<AdoptionRequestEntity>> getAllRequests() {
        return adoptionDao.getAllRequests();
    }

    public LiveData<List<AdoptionRequestWithDetails>> getAllRequestsWithDetails() {
        return adoptionDao.getAllRequestsWithDetails();
    }

    public LiveData<List<AdoptionRequestEntity>> getPendingRequests() {
        return adoptionDao.getPendingRequests();
    }

    public LiveData<List<AdoptionRequestWithDetails>> getPendingRequestsWithDetails() {
        return adoptionDao.getPendingRequestsWithDetails();
    }

    public LiveData<List<AdoptionRequestEntity>> getRequestsByUser(long userId) {
        return adoptionDao.getRequestsByUser(userId);
    }

    public LiveData<List<AdoptionRequestWithDetails>> getRequestsByUserWithDetails(long userId) {
        return adoptionDao.getRequestsByUserWithDetails(userId);
    }

    public List<AdoptionRequestEntity> getRequestsByUserSync(long userId) {
        try {
            Future<List<AdoptionRequestEntity>> future = AppDatabase.databaseExecutor.submit(
                () -> adoptionDao.getRequestsByUserSync(userId)
            );
            return future.get();
        } catch (ExecutionException e) {
            Log.e("AdoptionRepository", "Query failed", e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public LiveData<AdoptionRequestEntity> getRequestById(long id) {
        return adoptionDao.getRequestById(id);
    }

    public LiveData<Integer> getPendingCount() {
        return adoptionDao.getPendingCount();
    }

    public LiveData<Integer> getTotalRequestCount() {
        return adoptionDao.getTotalRequestCount();
    }

    public LiveData<Integer> getApprovedCount() {
        return adoptionDao.getApprovedCount();
    }
}
