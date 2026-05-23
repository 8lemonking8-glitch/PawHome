package com.example.midtermproject.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midtermproject.data.dao.UserDao;
import com.example.midtermproject.data.database.AppDatabase;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.util.PasswordUtils;

import java.util.List;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    public UserEntity login(String username, String password) {
        UserEntity user = userDao.getUserByUsername(username);
        if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public long register(String username, String password, String nickname, String email) {
        if (userDao.isUsernameExists(username) > 0) {
            return -1L;
        }
        String hashedPassword = PasswordUtils.hashPassword(password);
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setRole("USER");
        return userDao.insert(user);
    }

    public boolean isUsernameExists(String username) {
        return userDao.isUsernameExists(username) > 0;
    }

    public LiveData<UserEntity> getUserById(long id) {
        return userDao.getUserById(id);
    }

    public UserEntity getUserByIdSync(long id) {
        return userDao.getUserByIdSync(id);
    }

    public LiveData<List<UserEntity>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public LiveData<Integer> getUserCount() {
        return userDao.getUserCount();
    }

    public void update(UserEntity user) {
        AppDatabase.databaseExecutor.execute(() -> userDao.update(user));
    }
}
