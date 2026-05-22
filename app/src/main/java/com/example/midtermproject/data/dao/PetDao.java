package com.example.midtermproject.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midtermproject.data.entity.PetEntity;

import java.util.List;

@Dao
public interface PetDao {

    @Insert
    long insert(PetEntity pet);

    @Insert
    void insertAll(List<PetEntity> pets);

    @Update
    void update(PetEntity pet);

    @Delete
    void delete(PetEntity pet);

    @Query("SELECT * FROM pets ORDER BY createdAt DESC")
    LiveData<List<PetEntity>> getAllPets();

    @Query("SELECT * FROM pets ORDER BY createdAt DESC")
    List<PetEntity> getAllPetsSync();

    @Query("SELECT * FROM pets WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    LiveData<List<PetEntity>> getAvailablePets();

    @Query("SELECT * FROM pets WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    List<PetEntity> getAvailablePetsSync();

    @Query("SELECT * FROM pets WHERE type = :type AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    LiveData<List<PetEntity>> getAvailablePetsByType(String type);

    @Query("SELECT * FROM pets WHERE type = :type AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    List<PetEntity> getAvailablePetsByTypeSync(String type);

    @Query("SELECT * FROM pets WHERE type = :type ORDER BY createdAt DESC")
    LiveData<List<PetEntity>> getPetsByType(String type);

    @Query("SELECT * FROM pets WHERE id = :id")
    LiveData<PetEntity> getPetById(long id);

    @Query("SELECT * FROM pets WHERE id = :id")
    PetEntity getPetByIdSync(long id);

    @Query("SELECT * FROM pets WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<PetEntity>> getPetsByStatus(String status);

    @Query("SELECT * FROM pets WHERE name LIKE '%' || :query || '%' OR breed LIKE '%' || :query || '%'")
    LiveData<List<PetEntity>> searchPets(String query);

    @Query("SELECT COUNT(*) FROM pets")
    LiveData<Integer> getTotalPetCount();

    @Query("SELECT COUNT(*) FROM pets WHERE status = 'AVAILABLE'")
    LiveData<Integer> getAvailablePetCount();

    @Query("SELECT COUNT(*) FROM pets WHERE status = 'ADOPTED'")
    LiveData<Integer> getAdoptedPetCount();

    @Query("SELECT COUNT(*) FROM pets")
    int getTotalPetCountSync();

    @Query("UPDATE pets SET status = :status WHERE id = :petId")
    void updatePetStatus(long petId, String status);
}
