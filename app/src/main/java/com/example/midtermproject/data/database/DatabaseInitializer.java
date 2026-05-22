package com.example.midtermproject.data.database;

import android.content.Context;
import android.util.Log;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.util.PasswordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Initializes the database with seed data on first launch.
 * Includes sample pets (Dogs, Cats, Birds) and a default admin account.
 */
public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";

    public static void initialize(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        AppDatabase.databaseExecutor.execute(() -> {
            // Only seed if database is empty
            if (db.petDao().getTotalPetCountSync() > 0) {
                Log.d(TAG, "Database already initialized, skipping seed.");
                return;
            }

            Log.d(TAG, "Seeding database with initial data...");

            // Create admin account
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPassword(PasswordUtils.hashPassword("admin123"));
            admin.setNickname("Administrator");
            admin.setEmail("admin@pawhome.com");
            admin.setPhone("1234567890");
            admin.setRole("ADMIN");
            admin.setAvatarResId(R.drawable.ic_person);
            db.userDao().insert(admin);

            // Seed pets
            List<PetEntity> pets = new ArrayList<>();

            // ===== DOGS =====
            pets.add(createPet("Buddy", "DOG", "Golden Retriever", "Golden", "LARGE",
                    "3 years", "MALE",
                    "Buddy was found wandering near a highway during a rainstorm. A kind truck driver brought him to our shelter. Despite his rough start, Buddy is incredibly friendly and loves playing fetch. He's great with kids and other dogs.",
                    R.drawable.img_dog));

            pets.add(createPet("Mochi", "DOG", "Shiba Inu", "Red & White", "MEDIUM",
                    "2 years", "MALE",
                    "Mochi was surrendered by a family that could no longer care for him. He's a spirited and independent Shiba with a goofy personality. He loves walks in the park and will do anything for a treat.",
                    R.drawable.img_dog));

            pets.add(createPet("Luna", "DOG", "Husky", "Gray & White", "LARGE",
                    "1.5 years", "FEMALE",
                    "Luna was rescued from an abandoned property where she was left chained outside. She's a beautiful husky with striking blue eyes. She's energetic, playful, and needs an active family.",
                    R.drawable.img_dog));

            pets.add(createPet("Charlie", "DOG", "Labrador", "Chocolate", "LARGE",
                    "4 years", "MALE",
                    "Charlie was found at a local park, thin and dehydrated. After weeks of care, he's now healthy and full of energy. He's a loyal companion who loves swimming and belly rubs.",
                    R.drawable.img_dog));

            pets.add(createPet("Coco", "DOG", "Corgi", "Tri-color", "SMALL",
                    "2 years", "FEMALE",
                    "Coco was rescued from a puppy mill. Despite her past, she's incredibly sweet and trusting. Her short legs and big ears make everyone smile. She's house-trained and great with children.",
                    R.drawable.img_dog));

            // ===== CATS =====
            pets.add(createPet("Marmalade", "CAT", "Tabby", "Orange", "MEDIUM",
                    "3 years", "MALE",
                    "Marmalade was found hiding behind a restaurant dumpster as a kitten. He's grown into a confident and affectionate cat who loves to curl up on laps and purr loudly. He gets along well with other cats.",
                    R.drawable.img_cat));

            pets.add(createPet("Snow", "CAT", "Ragdoll", "White & Blue", "LARGE",
                    "2 years", "FEMALE",
                    "Snow was found in a cardboard box outside a vet clinic. She's a stunning ragdoll with piercing blue eyes. True to her breed, she's docile and loves being held like a baby.",
                    R.drawable.img_cat));

            pets.add(createPet("Oliver", "CAT", "British Shorthair", "Blue Gray", "MEDIUM",
                    "4 years", "MALE",
                    "Oliver was surrendered when his elderly owner moved to a nursing home. He's a calm and dignified gentleman who enjoys quiet company. He'll sit beside you while you read or work.",
                    R.drawable.img_cat));

            pets.add(createPet("Shadow", "CAT", "Domestic Shorthair", "Black", "MEDIUM",
                    "1 year", "FEMALE",
                    "Shadow was rescued from a construction site where she was born. She's playful, curious, and loves chasing laser pointers. Don't let her dark coat fool you — she's full of sunshine.",
                    R.drawable.img_cat));

            // ===== BIRDS =====
            pets.add(createPet("Rio", "BIRD", "Budgerigar", "Green & Yellow", "SMALL",
                    "1 year", "MALE",
                    "Rio was found in someone's backyard, likely an escaped pet. He's a cheerful little budgie who loves to chirp and sing. He can mimic simple tunes and enjoys sitting on your finger.",
                    R.drawable.img_bird));

            pets.add(createPet("Pearl", "BIRD", "Cockatiel", "Gray & Yellow", "SMALL",
                    "2 years", "FEMALE",
                    "Pearl was surrendered by a college student who was moving abroad. She's a gentle cockatiel who loves head scratches and whistling melodies. She's hand-tame and very social.",
                    R.drawable.img_bird));

            pets.add(createPet("Sunny", "BIRD", "Canary", "Bright Yellow", "SMALL",
                    "1.5 years", "MALE",
                    "Sunny was rescued from an overcrowded aviary. True to his name, his bright yellow feathers light up any room. He has a beautiful singing voice and brings joy to everyone who hears him.",
                    R.drawable.img_bird));

            db.petDao().insertAll(pets);
            Log.d(TAG, "Database seeded with " + pets.size() + " pets and 1 admin account.");
        });
    }

    private static PetEntity createPet(String name, String type, String breed, String color,
                                        String size, String age, String gender,
                                        String description, int imageResId) {
        PetEntity pet = new PetEntity();
        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setColor(color);
        pet.setSize(size);
        pet.setAge(age);
        pet.setGender(gender);
        pet.setDescription(description);
        pet.setImageResId(imageResId);
        // For carousel, we'll use the same image repeated for now.
        // Will be updated with real images later.
        pet.setImageResIds("[" + imageResId + "]");
        pet.setStatus("AVAILABLE");
        pet.setCreatedAt(System.currentTimeMillis());
        return pet;
    }
}
