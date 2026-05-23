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
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN avatarUri TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN gender TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN age INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE users ADD COLUMN address TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN housingCondition TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN monthlyIncome TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN petExperience TEXT");
            database.execSQL("ALTER TABLE adoption_requests ADD COLUMN signatureTimestamp INTEGER NOT NULL DEFAULT 0");
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}
