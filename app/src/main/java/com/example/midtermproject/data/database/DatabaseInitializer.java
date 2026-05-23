package com.example.midtermproject.data.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.AdoptionRequestEntity;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.util.PasswordUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initializes the database with seed data on first launch.
 * Includes sample pets (Dogs, Cats, Birds), a default admin account,
 * demo users with adoption history, and mock avatars / signatures.
 */
public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";

    public static void initialize(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        AppDatabase.databaseExecutor.execute(() -> {
            boolean isFreshInstall = db.petDao().getTotalPetCountSync() == 0;

            if (isFreshInstall) {
                Log.d(TAG, "Fresh install — seeding admin and pets...");

                UserEntity admin = new UserEntity();
                admin.setUsername("admin");
                admin.setPassword(PasswordUtils.hashPassword("admin"));
                admin.setNickname("Administrator");
                admin.setEmail("admin@pawhome.com");
                admin.setPhone("1234567890");
                admin.setRole("ADMIN");
                admin.setAvatarResId(R.drawable.ic_person);
                db.userDao().insert(admin);

                seedPets(db);
            }

            // Demo data: idempotent — skip if already seeded
            if (db.userDao().getUserByUsername("emma_wilson") != null) {
                Log.d(TAG, "Demo data already seeded, skipping.");
                return;
            }

            Log.d(TAG, "Seeding demo data...");

            // ===== Demo users =====
            long emmaId = seedDemoUsers(context, db);

            // ===== Demo adoption requests =====
            seedDemoRequests(context, db, emmaId);

            // ===== Demo favorites =====
            seedDemoFavorites(context, emmaId);

            Log.d(TAG, "Database seeded successfully with demo data.");
        });
    }

    // ==================== Pets ====================

    private static void seedPets(AppDatabase db) {
        String[][] petData = {
            // ===== DOGS (15) =====
            {"Buddy",   "DOG", "Golden Retriever",              "Golden",          "LARGE",  "3 years",   "MALE",   "Buddy was found wandering near a highway during a rainstorm. A kind truck driver brought him to our shelter. Despite his rough start, Buddy is incredibly friendly and loves playing fetch. He's great with kids and other dogs."},
            {"Mochi",   "DOG", "Shiba Inu",                     "Red & White",     "MEDIUM", "2 years",   "MALE",   "Mochi was surrendered by a family that could no longer care for him. He's a spirited and independent Shiba with a goofy personality. He loves walks in the park and will do anything for a treat."},
            {"Luna",    "DOG", "Husky",                          "Gray & White",    "LARGE",  "1.5 years", "FEMALE", "Luna was rescued from an abandoned property where she was left chained outside. She's a beautiful husky with striking blue eyes. She's energetic, playful, and needs an active family."},
            {"Charlie", "DOG", "Labrador",                       "Chocolate",       "LARGE",  "4 years",   "MALE",   "Charlie was found at a local park, thin and dehydrated. After weeks of care, he's now healthy and full of energy. He's a loyal companion who loves swimming and belly rubs."},
            {"Coco",    "DOG", "Corgi",                          "Tri-color",       "SMALL",  "2 years",   "FEMALE", "Coco was rescued from a puppy mill. Despite her past, she's incredibly sweet and trusting. Her short legs and big ears make everyone smile. She's house-trained and great with children."},
            {"Max",     "DOG", "German Shepherd",                "Black & Tan",     "LARGE",  "3 years",   "MALE",   "Max was a retired police dog whose handler could no longer keep him. He's highly intelligent, well-trained, and protective. He needs an experienced owner who can give him the structure and exercise he craves."},
            {"Bella",   "DOG", "Poodle",                         "White",           "MEDIUM", "2 years",   "FEMALE", "Bella was rescued from a neglectful breeder. She's a gentle soul who's blossomed in foster care. She's hypoallergenic, loves to learn new tricks, and enjoys cuddling on the couch after a good walk."},
            {"Rocky",   "DOG", "Boxer",                          "Fawn",            "LARGE",  "2.5 years", "MALE",   "Rocky was found as a stray with a broken leg that has since healed perfectly. He's a goofy, energetic boxer who thinks he's a lap dog. Great with older kids and loves playing tug-of-war."},
            {"Daisy",   "DOG", "Dachshund",                      "Red",             "SMALL",  "4 years",   "FEMALE", "Daisy was surrendered when her family had to relocate overseas. She's a sweet, long-bodied girl with a big personality. She loves burrowing under blankets and going on short, sniff-intensive walks."},
            {"Bear",    "DOG", "Chow Chow",                      "Cream",           "LARGE",  "3 years",   "MALE",   "Bear was found wandering in a forest reserve. His fluffy mane makes him look like a little lion. He's calm and dignified but needs an owner who understands the Chow Chow's independent nature."},
            {"Pepper",  "DOG", "Border Collie",                  "Black & White",   "MEDIUM", "1.5 years", "FEMALE", "Pepper was given up because her owners couldn't keep up with her intelligence. She knows over 15 commands, loves agility courses, and needs a home that can give her both mental and physical exercise."},
            {"Rosie",   "DOG", "Cavalier King Charles Spaniel",  "Blenheim",        "SMALL",  "2 years",   "FEMALE", "Rosie was rescued from a hoarding situation. She's a gentle, affectionate spaniel who just wants to be near people. Great with cats and other small dogs. Perfect for apartment living."},
            {"Duke",    "DOG", "Great Dane",                     "Harlequin",       "LARGE",  "2 years",   "MALE",   "Duke was surrendered when he grew too big for his family's apartment. Don't let his size intimidate you — he's a gentle giant who thinks he's a lap dog. He's well-mannered and walks beautifully on leash."},
            {"Penny",   "DOG", "Pomeranian",                     "Orange Sable",    "SMALL",  "3 years",   "FEMALE", "Penny was found abandoned in a park with severe matting. After grooming and care, she's transformed into a fluffy princess. She's sassy, confident, and loves being the center of attention."},
            {"Rex",     "DOG", "Rottweiler",                     "Black & Mahogany","LARGE",  "3.5 years", "MALE",   "Rex was rescued from a junkyard where he was used as a guard dog. With patience and love, he's become a loyal and gentle companion. He bonds deeply and is fiercely protective of his family."},
            // ===== CATS (13) =====
            {"Marmalade","CAT","Tabby",                          "Orange",          "MEDIUM", "3 years",   "MALE",   "Marmalade was found hiding behind a restaurant dumpster as a kitten. He's grown into a confident and affectionate cat who loves to curl up on laps and purr loudly. He gets along well with other cats."},
            {"Snow",    "CAT", "Ragdoll",                        "White & Blue",    "LARGE",  "2 years",   "FEMALE", "Snow was found in a cardboard box outside a vet clinic. She's a stunning ragdoll with piercing blue eyes. True to her breed, she's docile and loves being held like a baby."},
            {"Oliver",  "CAT", "British Shorthair",              "Blue Gray",       "MEDIUM", "4 years",   "MALE",   "Oliver was surrendered when his elderly owner moved to a nursing home. He's a calm and dignified gentleman who enjoys quiet company. He'll sit beside you while you read or work."},
            {"Shadow",  "CAT", "Domestic Shorthair",             "Black",           "MEDIUM", "1 year",    "FEMALE", "Shadow was rescued from a construction site where she was born. She's playful, curious, and loves chasing laser pointers. Don't let her dark coat fool you — she's full of sunshine."},
            {"Willow",  "CAT", "Siamese",                        "Seal Point",      "SMALL",  "1.5 years", "FEMALE", "Willow was found meowing outside a temple. She's a talkative, elegant Siamese with striking blue eyes. She'll follow you around the house and 'chat' about her day. Very people-oriented."},
            {"Cleo",    "CAT", "Persian",                        "White",           "MEDIUM", "3 years",   "FEMALE", "Cleo was surrendered due to her high grooming needs. She's a regal, flat-faced beauty who loves lounging on velvet cushions. She needs a dedicated owner who can maintain her luxurious coat."},
            {"Nala",    "CAT", "Bengal",                         "Spotted Brown",   "MEDIUM", "2 years",   "FEMALE", "Nala was rescued from an illegal breeder. She has stunning leopard-like spots and boundless energy. She loves climbing, exploring, and playing in water — very dog-like in personality!"},
            {"Loki",    "CAT", "Maine Coon",                     "Brown Tabby",     "LARGE",  "3 years",   "MALE",   "Loki was found as a tiny kitten during a thunderstorm. He's grown into a massive, gentle giant with tufted ears and a magnificent tail. He chirps instead of meows and loves playing in water bowls."},
            {"Misty",   "CAT", "Russian Blue",                   "Silver Blue",     "MEDIUM", "2 years",   "FEMALE", "Misty was surrendered when her owner developed severe allergies. She's a shimmering beauty with emerald eyes. She's shy at first but once she trusts you, she's the most loyal companion."},
            {"Ziggy",   "CAT", "Sphynx",                         "Pink",            "SMALL",  "2 years",   "MALE",   "Ziggy was rescued from a breeder who couldn't sell him. He's a warm, wrinkly, hairless cat who loves sweaters and snuggling for warmth. His extroverted personality more than makes up for his unusual looks."},
            {"Daisy",   "CAT", "Scottish Fold",                  "Silver Tabby",    "MEDIUM", "1.5 years", "FEMALE", "Daisy was found abandoned in a rental apartment. With her folded ears and round eyes, she looks like a living teddy bear. She's quiet, sweet, and enjoys perching on windowsills to watch birds."},
            {"Pumpkin", "CAT", "Abyssinian",                     "Ruddy",           "SMALL",  "1 year",    "MALE",   "Pumpkin was rescued from a hoarding case with 40 other cats. He's a sleek, ticked-coat beauty with an adventurous spirit. He loves climbing to the highest point in any room and surveying his kingdom."},
            {"Luna",    "CAT", "Tuxedo",                         "Black & White",   "MEDIUM", "2 years",   "FEMALE", "Luna was born in a library basement and raised by the librarians. She's a dapper tuxedo cat who loves curling up on books and papers. She's litter-trained, well-mannered, and very photogenic."},
            // ===== BIRDS (8) =====
            {"Rio",     "BIRD","Budgerigar",                     "Green & Yellow",  "SMALL",  "1 year",    "MALE",   "Rio was found in someone's backyard, likely an escaped pet. He's a cheerful little budgie who loves to chirp and sing. He can mimic simple tunes and enjoys sitting on your finger."},
            {"Pearl",   "BIRD","Cockatiel",                      "Gray & Yellow",   "SMALL",  "2 years",   "FEMALE", "Pearl was surrendered by a college student who was moving abroad. She's a gentle cockatiel who loves head scratches and whistling melodies. She's hand-tame and very social."},
            {"Sunny",   "BIRD","Canary",                         "Bright Yellow",   "SMALL",  "1.5 years", "MALE",   "Sunny was rescued from an overcrowded aviary. True to his name, his bright yellow feathers light up any room. He has a beautiful singing voice and brings joy to everyone who hears him."},
            {"Coco",    "BIRD","Lovebird",                       "Peach-faced",     "SMALL",  "1 year",    "FEMALE", "Coco was found perched on a fire escape during a cold winter night. She's a vibrant little lovebird who adores attention and cuddles. She loves shredding paper toys and taking baths in her water dish."},
            {"Mango",   "BIRD","Sun Conure",                     "Orange & Yellow", "SMALL",  "2 years",   "MALE",   "Mango was surrendered when his family moved into a no-pets building. True to his name, he's a burst of tropical color and personality. He's loud, playful, and loves dancing to music."},
            {"Blue",    "BIRD","Parrotlet",                      "Blue",            "SMALL",  "1 year",    "MALE",   "Blue was found in a public park, probably escaped. He's a tiny parrot with a huge personality. Despite his small size, he's fearless and loves to learn tricks. He can already wave and spin on command."},
            {"Sky",     "BIRD","Zebra Finch",                    "Gray & White",    "SMALL",  "0.5 years", "FEMALE", "Sky was part of an accidental litter at a local pet store's overcrowded enclosure. She's a delicate, beeping little finch who loves company. She does best in pairs — adopt her with a finch friend!"},
            {"Kiwi",    "BIRD","Green-cheeked Conure",           "Green & Red",     "SMALL",  "2 years",   "MALE",   "Kiwi was rescued from an owner who didn't realize how long parrots live. He's a cuddly, clownish conure who loves hiding in hair and hoodie pockets. He's well-socialized and gets along with everyone."},
        };

        List<PetEntity> pets = new ArrayList<>();
        for (String[] d : petData) {
            int imgRes;
            switch (d[1]) {
                case "CAT":  imgRes = R.drawable.img_cat; break;
                case "BIRD": imgRes = R.drawable.img_bird; break;
                default:     imgRes = R.drawable.img_dog; break;
            }
            pets.add(createPet(d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], imgRes));
        }

        db.petDao().insertAll(pets);
        Log.d(TAG, "Seeded " + pets.size() + " pets.");
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

        int cardRes;
        switch (type) {
            case "CAT": cardRes = R.drawable.bg_carousel_cat; break;
            case "BIRD": cardRes = R.drawable.bg_carousel_bird; break;
            default: cardRes = R.drawable.bg_carousel_dog; break;
        }
        pet.setImageResIds("[" + imageResId + "," + cardRes + "," + cardRes + "]");

        pet.setStatus("AVAILABLE");
        pet.setCreatedAt(System.currentTimeMillis());
        return pet;
    }

    // ==================== Demo Users ====================

    private static long seedDemoUsers(Context context, AppDatabase db) {
        // Emma — complete profile, experienced adopter
        UserEntity emma = new UserEntity();
        emma.setUsername("emma_wilson");
        emma.setPassword(PasswordUtils.hashPassword("password123"));
        emma.setNickname("Emma");
        emma.setEmail("emma.wilson@email.com");
        emma.setPhone("555-0100");
        emma.setRole("USER");
        emma.setGender("Female");
        emma.setAge(28);
        emma.setAddress("123 Maple Street, Portland OR 97201");
        emma.setHousingCondition("Own House with Garden");
        emma.setMonthlyIncome("$3,500 - $5,000");
        emma.setPetExperience("Has owned dogs for 5 years, previously fostered 2 cats. Completed basic pet training course at Portland Humane Society.");
        emma.setAvatarResId(R.drawable.ic_person);

        // Jack — complete profile, first-time owner
        UserEntity jack = new UserEntity();
        jack.setUsername("jack_chen");
        jack.setPassword(PasswordUtils.hashPassword("password123"));
        jack.setNickname("Jack");
        jack.setEmail("jack.chen@email.com");
        jack.setPhone("555-0200");
        jack.setRole("USER");
        jack.setGender("Male");
        jack.setAge(32);
        jack.setAddress("45 Oak Avenue Apt 12B, Seattle WA 98101");
        jack.setHousingCondition("Apartment (Pet-Friendly)");
        jack.setMonthlyIncome("$2,000 - $3,500");
        jack.setPetExperience("First-time pet owner. Completed online pet care course. Has a flexible work-from-home schedule.");
        jack.setAvatarResId(R.drawable.ic_person);

        // Sophie — complete profile, recent grad
        UserEntity sophie = new UserEntity();
        sophie.setUsername("sophie_liu");
        sophie.setPassword(PasswordUtils.hashPassword("password123"));
        sophie.setNickname("Sophie");
        sophie.setEmail("sophie.liu@email.com");
        sophie.setPhone("555-0300");
        sophie.setRole("USER");
        sophie.setGender("Female");
        sophie.setAge(24);
        sophie.setAddress("200 University Ave Apt 5C, Austin TX 78705");
        sophie.setHousingCondition("Shared Apartment (Pet-Friendly)");
        sophie.setMonthlyIncome("$1,500 - $2,500");
        sophie.setPetExperience("Grew up with a family cat. Looking for a companion pet as a young professional. Researched pet care extensively.");
        sophie.setAvatarResId(R.drawable.ic_person);

        // David — complete profile, experienced with multiple previous adoptions
        UserEntity david = new UserEntity();
        david.setUsername("david_kim");
        david.setPassword(PasswordUtils.hashPassword("password123"));
        david.setNickname("David");
        david.setEmail("david.kim@email.com");
        david.setPhone("555-0400");
        david.setRole("USER");
        david.setGender("Male");
        david.setAge(35);
        david.setAddress("78 Pine Road, San Francisco CA 94102");
        david.setHousingCondition("Townhouse with Private Yard");
        david.setMonthlyIncome("$5,000+");
        david.setPetExperience("Lifelong pet owner. Currently has one rescue cat. Experienced with large breeds — previously owned a Great Dane and a Labrador.");
        david.setAvatarResId(R.drawable.ic_person);

        long emmaId = db.userDao().insert(emma);
        long jackId = db.userDao().insert(jack);
        long sophieId = db.userDao().insert(sophie);
        long davidId = db.userDao().insert(david);

        // Generate distinct colored avatars for each demo user
        emma.setAvatarUri(generateAvatar(context, emmaId, "E", 0xFFE8734A));     // warm orange
        jack.setAvatarUri(generateAvatar(context, jackId, "J", 0xFF5CB8A5));     // teal
        sophie.setAvatarUri(generateAvatar(context, sophieId, "S", 0xFF7C6FBF)); // purple
        david.setAvatarUri(generateAvatar(context, davidId, "D", 0xFF4A90D9));   // blue

        db.userDao().update(emma);
        db.userDao().update(jack);
        db.userDao().update(sophie);
        db.userDao().update(david);

        Log.d(TAG, "Seeded 4 demo users (emma_wilson, jack_chen, sophie_liu, david_kim).");
        return emmaId;
    }

    // ==================== Demo Adoption Requests ====================

    private static void seedDemoRequests(Context context, AppDatabase db, long emmaId) {
        UserEntity jack = db.userDao().getUserByUsername("jack_chen");
        UserEntity sophie = db.userDao().getUserByUsername("sophie_liu");
        UserEntity david = db.userDao().getUserByUsername("david_kim");
        if (jack == null || sophie == null || david == null) return;

        long jackId = jack.getId();
        long sophieId = sophie.getId();
        long davidId = david.getId();

        // Pet IDs from seed order:
        //  1=Buddy  2=Mochi  3=Luna(dog)  4=Charlie  5=Coco  6=Max  7=Bella
        //  8=Rocky  9=Daisy  10=Bear  11=Pepper  12=Rosie  13=Duke  14=Penny  15=Rex
        //  16=Marmalade  17=Snow  18=Oliver  19=Shadow  20=Willow
        //  21=Cleo  22=Loki  23=Misty  24=Nala  25=Ziggy  26=Daisy(cat)  27=Pumpkin  28=Luna(cat)
        //  29=Rio  30=Pearl  31=Sunny  32=Coco(bird)  33=Mango  34=Blue  35=Sky  36=Kiwi

        long now = System.currentTimeMillis();
        long d1 = now - 86400000L;      // 1 day ago
        long d2 = now - 172800000L;     // 2 days
        long d3 = now - 259200000L;     // 3 days
        long d4 = now - 345600000L;     // 4 days
        long d5 = now - 432000000L;     // 5 days
        long d6 = now - 518400000L;     // 6 days
        long d7 = now - 604800000L;     // 7 days
        long d10 = now - 864000000L;    // 10 days
        long d14 = now - 1209600000L;   // 14 days

        // --- Emma's requests ---
        // 1. Emma → Buddy(id=1): PENDING (today)
        insertRequest(db, context, emmaId, 1, "PENDING", "Emma Wilson", d1, d1, 0);

        // 2. Emma → Mochi(id=2): APPROVED 3d ago → ADOPTED
        insertRequest(db, context, emmaId, 2, "APPROVED", "Emma Wilson", d3, d1, d3);
        db.petDao().updatePetStatus(2, "ADOPTED");

        // 3. Emma → Coco(id=5): APPROVED 10d ago → ADOPTED
        insertRequest(db, context, emmaId, 5, "APPROVED", "Emma Wilson", d10, d7, d10);
        db.petDao().updatePetStatus(5, "ADOPTED");

        // 4. Emma → Bella(id=7): APPROVED 7d ago → ADOPTED
        insertRequest(db, context, emmaId, 7, "APPROVED", "Emma Wilson", d7, d5, d7);
        db.petDao().updatePetStatus(7, "ADOPTED");

        // 5. Emma → Rosie(id=12): PENDING (3d ago)
        insertRequest(db, context, emmaId, 12, "PENDING", "Emma Wilson", d3, d3, 0);

        // --- Jack's requests ---
        // 6. Jack → Snow(id=17): PENDING (2d ago)
        insertRequest(db, context, jackId, 17, "PENDING", "Jack Chen", d2, d2, 0);

        // 7. Jack → Oliver(id=18): REJECTED (4d ago)
        insertRequest(db, context, jackId, 18, "REJECTED", "Jack Chen", d4, d3, d4);

        // 8. Jack → Max(id=6): REJECTED (5d ago)
        insertRequest(db, context, jackId, 6, "REJECTED", "Jack Chen", d5, d4, d5);

        // 9. Jack → Loki(id=22): APPROVED 14d ago → ADOPTED
        insertRequest(db, context, jackId, 22, "APPROVED", "Jack Chen", d14, d10, d14);
        db.petDao().updatePetStatus(22, "ADOPTED");

        // 10. Jack → Marmalade(id=16): PENDING (1d ago)
        insertRequest(db, context, jackId, 16, "PENDING", "Jack Chen", d1, d1, 0);

        // --- Sophie's requests (incomplete profile) ---
        // 11. Sophie → Luna dog(id=3): PENDING (today)
        insertRequest(db, context, sophieId, 3, "PENDING", "Sophie Liu", now, now, 0);

        // 12. Sophie → Misty(id=23): PENDING (2d ago)
        insertRequest(db, context, sophieId, 23, "PENDING", "Sophie Liu", d2, d2, 0);

        // --- David's requests ---
        // 13. David → Pepper(id=11): APPROVED 10d ago → ADOPTED
        insertRequest(db, context, davidId, 11, "APPROVED", "David Kim", d10, d7, d10);
        db.petDao().updatePetStatus(11, "ADOPTED");

        // 14. David → Duke(id=13): PENDING (1d ago)
        insertRequest(db, context, davidId, 13, "PENDING", "David Kim", d1, d1, 0);

        Log.d(TAG, "Seeded 14 demo adoption requests (5 PENDING, 5 APPROVED/ADOPTED, 2 REJECTED, 2 PENDING+incomplete).");
    }

    private static void insertRequest(AppDatabase db, Context context,
                                       long userId, long petId, String status,
                                       String fullName, long createdAt,
                                       long signatureTimestamp, long reviewedAt) {
        AdoptionRequestEntity req = new AdoptionRequestEntity();
        req.setUserId(userId);
        req.setPetId(petId);
        req.setStatus(status);
        req.setAgreementAccepted(true);
        req.setSignaturePath(generateSignature(context, fullName, userId, signatureTimestamp));
        req.setSignatureTimestamp(signatureTimestamp);
        req.setCreatedAt(createdAt);
        if (reviewedAt > 0) req.setReviewedAt(reviewedAt);
        db.adoptionRequestDao().insert(req);
    }

    // ==================== Demo Favorites ====================

    private static void seedDemoFavorites(Context context, long emmaId) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(
                "PawHomeFavorites", Context.MODE_PRIVATE);

        // Emma favorited: Mochi(2), Coco(5), Bella(7), Buddy(1), Snow(17)
        java.util.Set<String> emmaFavs = new java.util.HashSet<>();
        emmaFavs.add("2");
        emmaFavs.add("5");
        emmaFavs.add("7");
        emmaFavs.add("1");
        emmaFavs.add("17");
        prefs.edit().putStringSet("favorite_pet_ids_" + emmaId, emmaFavs).apply();

        Log.d(TAG, "Seeded 5 favorites for emma_wilson.");
    }

    // ==================== Avatar Generation ====================

    /**
     * Generates a circular avatar bitmap with a colored background and white initial letter.
     * Each user gets a distinct color for strong visual differentiation.
     */
    private static String generateAvatar(Context context, long userId, String initial, int bgColor) {
        int size = 256;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Colored circle background
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(bgColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint);

        // White initial letter
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.45f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        Rect textBounds = new Rect();
        textPaint.getTextBounds(initial, 0, initial.length(), textBounds);
        float textY = size / 2f - (textBounds.top + textBounds.bottom) / 2f;
        canvas.drawText(initial, size / 2f, textY, textPaint);

        // Save to file
        FileOutputStream fos = null;
        try {
            File dir = context.getFilesDir();
            // Clean up old avatars for this user
            File[] old = dir.listFiles((d, name) -> name.startsWith("avatar_" + userId));
            if (old != null) {
                for (File f : old) f.delete();
            }
            File file = new File(dir, "avatar_" + userId + "_seed.jpg");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            return android.net.Uri.fromFile(file).toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate avatar for user " + userId, e);
            return null;
        } finally {
            if (fos != null) { try { fos.close(); } catch (java.io.IOException ignored) {} }
            bitmap.recycle();
        }
    }

    // ==================== Signature Generation ====================

    /**
     * Generates a realistic-looking cursive signature using letterform simulation
     * with ascender loops, descender tails, pressure effects, and a natural slant.
     * Deterministic — same name always produces the same signature.
     */
    private static String generateSignature(Context context, String name, long userId, long timestamp) {
        int width = 720;
        int height = 300;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Random rng = new Random(name.hashCode());

        // Ink color with slight warm undertone + per-user variation
        int ink = Color.rgb(
            30 + rng.nextInt(15),
            20 + rng.nextInt(12),
            15 + rng.nextInt(10)
        );

        float slant = 0.25f + rng.nextFloat() * 0.1f; // slight rightward slant
        float midlineY = height * 0.38f;              // baseline for regular letters
        float ascenderTop = height * 0.08f;           // top of tall letters
        float descenderBottom = height * 0.72f;       // bottom of tails
        float letterW = width / (name.length() * 1.3f + 6f);

        // Build the cursive path — letter by letter
        Path path = new Path();
        float x = 40;
        float y = midlineY + rng.nextFloat() * 10 - 5;

        // Entry stroke
        float ex = x + letterW * 0.4f;
        path.moveTo(x - letterW * 0.5f, y + height * 0.1f);
        path.cubicTo(x - letterW * 0.2f, y - height * 0.05f, x + letterW * 0.15f, y - 3, x, y);

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            boolean ascender = "bdfhkltBDFHKLT".indexOf(ch) >= 0;
            boolean descender = "gjpqyGJPQY".indexOf(ch) >= 0;
            boolean isCapital = Character.isUpperCase(ch);
            boolean isSpace = ch == ' ';

            if (isSpace) {
                x += letterW * 1.5f;
                y = midlineY + rng.nextFloat() * 10 - 5;
                path.moveTo(x, y);
                continue;
            }

            // Character width varies
            float cw = letterW * (0.7f + rng.nextFloat() * 0.6f);
            if (isCapital) cw *= 1.6f;

            if (ascender) {
                // Ascender loop — up and around, then down to baseline
                float topY = ascenderTop + rng.nextFloat() * height * 0.05f;
                float cp1x = x + cw * 0.15f + slant * (y - topY);
                float cp1y = y - (y - topY) * 0.5f;
                float cp2x = x + cw * 0.2f + slant * (y - topY) * 0.6f;
                float cp2y = topY;
                float topX = x + cw * 0.5f;
                path.cubicTo(cp1x, cp1y, cp2x, cp2y, topX, topY);

                // Downstroke back to baseline
                float downX = x + cw;
                float downY = midlineY + rng.nextFloat() * 8 - 4;
                float dcp1x = topX + cw * 0.15f + slant * (topY - downY) * 0.3f;
                float dcp1y = topY + (downY - topY) * 0.3f;
                float dcp2x = topX + cw * 0.3f + slant * (topY - downY) * 0.6f;
                float dcp2y = downY - (downY - topY) * 0.3f;
                path.cubicTo(dcp1x, dcp1y, dcp2x, dcp2y, downX, downY);
                x = downX;
                y = downY;
            } else if (descender) {
                // Regular part first, then descender tail
                float midX = x + cw * 0.5f;
                float midY = midlineY - rng.nextFloat() * height * 0.06f;
                path.cubicTo(
                    x + cw * 0.3f, y - height * 0.05f,
                    x + cw * 0.4f, midY + 2,
                    midX, midY
                );

                // Tail loop down
                float tailBottom = descenderBottom - rng.nextFloat() * height * 0.06f;
                float tailX = x + cw;
                float tailY = midlineY + rng.nextFloat() * 4 - 2;
                path.cubicTo(
                    midX + cw * 0.1f, midY + (tailBottom - midY) * 0.2f,
                    midX + cw * 0.3f, tailBottom,
                    midX + cw * 0.5f, tailBottom + rng.nextFloat() * 4
                );
                path.cubicTo(
                    midX + cw * 0.7f, tailBottom + rng.nextFloat() * 4,
                    tailX - cw * 0.15f, midY + (tailBottom - midY) * 0.3f,
                    tailX, tailY
                );
                x = tailX;
                y = tailY;
            } else {
                // Regular letter: small undulation
                float endX = x + cw;
                float endY = midlineY + rng.nextFloat() * 8 - 4;
                float midX = x + cw * 0.5f;
                float midY = midlineY - rng.nextFloat() * height * 0.07f;
                if (isCapital) midY -= height * 0.08f;

                path.cubicTo(
                    x + cw * 0.3f, y - (y - midY) * 0.6f,
                    x + cw * 0.3f, midY + rng.nextFloat() * 2,
                    midX, midY
                );
                path.cubicTo(
                    x + cw * 0.7f, midY + rng.nextFloat() * 2,
                    x + cw * 0.7f, endY - (endY - midY) * 0.4f,
                    endX, endY
                );
                x = endX;
                y = endY;
            }

            // Dot for 'i' and 'j'
            if (ch == 'i' || ch == 'j') {
                float dotX = x - cw * 0.5f;
                float dotY = midlineY - height * 0.22f + rng.nextFloat() * 4;
                path.addCircle(dotX, dotY, 2.5f + rng.nextFloat(), Path.Direction.CW);
            }

            // Cross stroke for 't'
            if (ch == 't') {
                float crossX = x - cw * 0.6f;
                float crossY = midlineY - height * 0.1f + rng.nextFloat() * 4;
                path.moveTo(crossX - cw * 0.15f, crossY);
                path.lineTo(crossX + cw * 0.4f, crossY + rng.nextFloat() * 2);
            }
        }

        // Exit flourish
        float fx1 = x;
        float fy1 = y;
        float fx2 = x + letterW * 2f + rng.nextFloat() * letterW;
        float fy2 = y + height * 0.06f;
        path.cubicTo(
            fx1 + letterW * 0.5f, fy1 - height * 0.04f,
            fx2 - letterW * 0.3f, fy2 + height * 0.03f,
            fx2, fy2
        );

        // ---- Render with pressure simulation (3 passes) ----
        // Pass 1: ink-spread layer (wider, slightly lighter)
        Paint p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p1.setStyle(Paint.Style.STROKE);
        p1.setStrokeCap(Paint.Cap.ROUND);
        p1.setStrokeJoin(Paint.Join.ROUND);
        p1.setStrokeWidth(5.8f);
        p1.setColor(Color.argb(140, Color.red(ink), Color.green(ink), Color.blue(ink)));
        canvas.drawPath(path, p1);

        // Pass 2: main ink layer
        Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setStyle(Paint.Style.STROKE);
        p2.setStrokeCap(Paint.Cap.ROUND);
        p2.setStrokeJoin(Paint.Join.ROUND);
        p2.setStrokeWidth(3.8f);
        p2.setColor(Color.argb(210, Color.red(ink), Color.green(ink), Color.blue(ink)));
        canvas.drawPath(path, p2);

        // Pass 3: pen-tip layer (thin, dark, slight offset for texture)
        Paint p3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p3.setStyle(Paint.Style.STROKE);
        p3.setStrokeCap(Paint.Cap.ROUND);
        p3.setStrokeJoin(Paint.Join.ROUND);
        p3.setStrokeWidth(2.0f);
        p3.setColor(ink);
        canvas.drawPath(path, p3);

        // Underline accent (matching the flourish direction)
        Paint uline = new Paint(Paint.ANTI_ALIAS_FLAG);
        uline.setStyle(Paint.Style.STROKE);
        uline.setStrokeCap(Paint.Cap.ROUND);
        uline.setStrokeWidth(2.2f);
        uline.setColor(Color.argb(160, Color.red(ink), Color.green(ink), Color.blue(ink)));
        Path ulPath = new Path();
        float ux = 30;
        float uy = midlineY + height * 0.25f;
        ulPath.moveTo(ux, uy);
        for (int i = 0; i < 3; i++) {
            float uex = ux + (x + letterW * 2f - ux) / 3f;
            float uey = uy + rng.nextFloat() * 10 - 5;
            float ucx = (ux + uex) / 2f;
            float ucy = uy + (i % 2 == 0 ? -15 : 15) + rng.nextFloat() * 5;
            ulPath.quadTo(ucx, ucy, uex, uey);
            ux = uex;
            uy = uey;
        }
        canvas.drawPath(ulPath, uline);

        // Save
        FileOutputStream fos = null;
        try {
            File dir = new File(context.getFilesDir(), "signatures");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "sig_" + userId + "_" + timestamp + ".png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate signature", e);
            return "signature_placeholder_" + timestamp;
        } finally {
            if (fos != null) { try { fos.close(); } catch (java.io.IOException ignored) {} }
            bitmap.recycle();
        }
    }
}
