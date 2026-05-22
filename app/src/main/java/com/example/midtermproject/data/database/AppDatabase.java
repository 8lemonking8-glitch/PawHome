package com.example.midtermproject.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.midtermproject.data.dao.AdoptionRequestDao;
import com.example.midtermproject.data.dao.PetDao;
import com.example.midtermproject.data.dao.UserDao;
import com.example.midtermproject.data.entity.AdoptionRequestEntity;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        PetEntity.class,
        UserEntity.class,
        AdoptionRequestEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN avatarUri TEXT");
        }
    };

    private static volatile AppDatabase INSTANCE;
    
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseExecutor =
        Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract PetDao petDao();
    public abstract UserDao userDao();
    public abstract AdoptionRequestDao adoptionRequestDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "pawhome_database"
                        )
                        .addMigrations(MIGRATION_1_2)
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}
