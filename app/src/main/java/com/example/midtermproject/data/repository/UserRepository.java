package com.example.midtermproject.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midtermproject.data.dao.UserDao;
import com.example.midtermproject.data.database.AppDatabase;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.util.PasswordUtils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    // ===== Authentication =====

    /**
     * Attempts to log in a user. Runs on a background thread.
     * @return UserEntity if credentials are valid, null otherwise.
     */
    public UserEntity login(String username, String password) {
        String hashedPassword = PasswordUtils.hashPassword(password);
        try {
            Future<UserEntity> future = AppDatabase.databaseExecutor.submit(
                () -> userDao.login(username, hashedPassword)
            );
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registers a new user. Runs on a background thread.
     * @return The new user's ID, or -1 if registration failed (e.g., username exists).
     */
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
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Checks if a username already exists. Runs on a background thread.
     */
    public boolean isUsernameExists(String username) {
        try {
            Future<Boolean> future = AppDatabase.databaseExecutor.submit(() -> {
                return userDao.isUsernameExists(username) > 0;
            });
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
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
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
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
