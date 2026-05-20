package com.example.midtermproject.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midtermproject.data.dao.AdoptionRequestDao;
import com.example.midtermproject.data.dao.PetDao;
import com.example.midtermproject.data.database.AppDatabase;
import com.example.midtermproject.data.entity.AdoptionRequestEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AdoptionRepository {

    private final AdoptionRequestDao adoptionDao;
    private final PetDao petDao;

    public AdoptionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        adoptionDao = db.adoptionRequestDao();
        petDao = db.petDao();
    }

    // ===== Create =====

    public long createRequest(long userId, long petId, String signaturePath, long signatureTimestamp) {
        try {
            Future<Long> future = Executors.newSingleThreadExecutor().submit(() -> {
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
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ===== Admin Actions =====

    public void approveRequest(long requestId, long petId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            adoptionDao.updateRequestStatus(requestId, "APPROVED", System.currentTimeMillis());
            // Automatically mark pet as adopted
            petDao.updatePetStatus(petId, "ADOPTED");
        });
    }

    public void rejectRequest(long requestId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            adoptionDao.updateRequestStatus(requestId, "REJECTED", System.currentTimeMillis());
        });
    }

    // ===== Read Operations =====

    public LiveData<List<AdoptionRequestEntity>> getAllRequests() {
        return adoptionDao.getAllRequests();
    }

    public LiveData<List<AdoptionRequestEntity>> getPendingRequests() {
        return adoptionDao.getPendingRequests();
    }

    public LiveData<List<AdoptionRequestEntity>> getRequestsByUser(long userId) {
        return adoptionDao.getRequestsByUser(userId);
    }

    public List<AdoptionRequestEntity> getRequestsByUserSync(long userId) {
        try {
            Future<List<AdoptionRequestEntity>> future = Executors.newSingleThreadExecutor().submit(
                () -> adoptionDao.getRequestsByUserSync(userId)
            );
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
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
