package com.example.midtermproject.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.midtermproject.data.dao.UserDao;
import com.example.midtermproject.data.database.AppDatabase;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.util.PasswordUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    // ===== Authentication =====

    public UserEntity login(String username, String password) {
        try {
            Future<UserEntity> future = AppDatabase.databaseExecutor.submit(() -> {
                UserEntity user = userDao.getUserByUsername(username);
                if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
                    return user;
                }
                return null;
            });
            return future.get();
        } catch (ExecutionException e) {
            Log.e("UserRepository", "Login query failed", e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public long register(String username, String password, String nickname, String email) {
        String hashedPassword = PasswordUtils.hashPassword(password);
        try {
            Future<Long> future = AppDatabase.databaseExecutor.submit(() -> {
                if (userDao.isUsernameExists(username) > 0) {
                    return -1L;
                }
                UserEntity user = new UserEntity();
                user.setUsername(username);
                user.setPassword(hashedPassword);
                user.setNickname(nickname);
                user.setEmail(email);
                user.setRole("USER");
                return userDao.insert(user);
            });
            return future.get();
        } catch (ExecutionException e) {
            Log.e("UserRepository", "Register failed", e);
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    public boolean isUsernameExists(String username) {
        try {
            Future<Boolean> future = AppDatabase.databaseExecutor.submit(() -> {
                return userDao.isUsernameExists(username) > 0;
            });
            return future.get();
        } catch (ExecutionException e) {
            Log.e("UserRepository", "Username check failed", e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // ===== Read Operations =====

    public LiveData<UserEntity> getUserById(long id) {
        return userDao.getUserById(id);
    }

    public UserEntity getUserByIdSync(long id) {
        try {
            Future<UserEntity> future = AppDatabase.databaseExecutor.submit(
                () -> userDao.getUserByIdSync(id)
            );
            return future.get();
        } catch (ExecutionException e) {
            Log.e("UserRepository", "Get user failed", e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public LiveData<List<UserEntity>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public LiveData<Integer> getUserCount() {
        return userDao.getUserCount();
    }

    // ===== Write Operations =====

    public void update(UserEntity user) {
        AppDatabase.databaseExecutor.execute(() -> userDao.update(user));
    }
}
