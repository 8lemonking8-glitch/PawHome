package com.example.midtermproject.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midtermproject.data.dao.PetDao;
import com.example.midtermproject.data.database.AppDatabase;
import com.example.midtermproject.data.entity.PetEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PetRepository {

    private final PetDao petDao;

    public PetRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        petDao = db.petDao();
    }

    // ===== Read Operations =====

    public LiveData<List<PetEntity>> getAllPets() {
        return petDao.getAllPets();
    }

    public LiveData<List<PetEntity>> getAvailablePets() {
        return petDao.getAvailablePets();
    }

    public LiveData<List<PetEntity>> getAvailablePetsByType(String type) {
        return petDao.getAvailablePetsByType(type);
    }

    public List<PetEntity> getAvailablePetsSync() {
        try {
            Future<List<PetEntity>> future = AppDatabase.databaseExecutor.submit(
                () -> petDao.getAvailablePetsSync()
            );
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PetEntity> getAvailablePetsByTypeSync(String type) {
        try {
            Future<List<PetEntity>> future = AppDatabase.databaseExecutor.submit(
                () -> petDao.getAvailablePetsByTypeSync(type)
            );
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<PetEntity>> getPetsByType(String type) {
        return petDao.getPetsByType(type);
    }

    public LiveData<PetEntity> getPetById(long id) {
        return petDao.getPetById(id);
    }

    public LiveData<List<PetEntity>> getPetsByStatus(String status) {
        return petDao.getPetsByStatus(status);
    }

    public LiveData<List<PetEntity>> searchPets(String query) {
        return petDao.searchPets(query);
    }

    public LiveData<Integer> getTotalPetCount() {
        return petDao.getTotalPetCount();
    }

    public LiveData<Integer> getAvailablePetCount() {
        return petDao.getAvailablePetCount();
    }

    public LiveData<Integer> getAdoptedPetCount() {
        return petDao.getAdoptedPetCount();
    }

    // ===== Write Operations =====

    public void insert(PetEntity pet) {
        AppDatabase.databaseExecutor.execute(() -> petDao.insert(pet));
    }

    public void update(PetEntity pet) {
        AppDatabase.databaseExecutor.execute(() -> petDao.update(pet));
    }

    public void delete(PetEntity pet) {
        AppDatabase.databaseExecutor.execute(() -> petDao.delete(pet));
    }

    public void updatePetStatus(long petId, String status) {
        AppDatabase.databaseExecutor.execute(() -> petDao.updatePetStatus(petId, status));
    }
}
