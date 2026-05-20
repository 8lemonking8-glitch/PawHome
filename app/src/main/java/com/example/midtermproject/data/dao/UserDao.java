package com.example.midtermproject.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midtermproject.data.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    UserEntity login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<UserEntity> getUserById(long id);

    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getUserByIdSync(long id);

    @Query("SELECT * FROM users WHERE role = 'USER' ORDER BY createdAt DESC")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM users WHERE role = 'USER' ORDER BY createdAt DESC")
    List<UserEntity> getAllUsersSync();

    @Query("SELECT COUNT(*) FROM users WHERE role = 'USER'")
    LiveData<Integer> getUserCount();

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int isUsernameExists(String username);
}
