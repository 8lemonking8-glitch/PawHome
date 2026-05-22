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

            // Create admin account with a generated password
            String adminPassword = generateInitialPassword();
            Log.i(TAG, "========================================");
            Log.i(TAG, "  Admin password: " + adminPassword);
            Log.i(TAG, "  Change after first login!");
            Log.i(TAG, "========================================");

            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPassword(PasswordUtils.hashPassword(adminPassword));
            admin.setNickname("Administrator");
            admin.setEmail("admin@pawhome.com");
            admin.setPhone("1234567890");
            admin.setRole("ADMIN");
            admin.setAvatarResId(R.drawable.ic_person);
            db.userDao().insert(admin);

            // Seed pets
            List<PetEntity> pets = new ArrayList<>();

            // ===== DOGS (15) =====
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

            pets.add(createPet("Max", "DOG", "German Shepherd", "Black & Tan", "LARGE",
                    "3 years", "MALE",
                    "Max was a retired police dog whose handler could no longer keep him. He's highly intelligent, well-trained, and protective. He needs an experienced owner who can give him the structure and exercise he craves.",
                    R.drawable.img_dog));

            pets.add(createPet("Bella", "DOG", "Poodle", "White", "MEDIUM",
                    "2 years", "FEMALE",
                    "Bella was rescued from a neglectful breeder. She's a gentle soul who's blossomed in foster care. She's hypoallergenic, loves to learn new tricks, and enjoys cuddling on the couch after a good walk.",
                    R.drawable.img_dog));

            pets.add(createPet("Rocky", "DOG", "Boxer", "Fawn", "LARGE",
                    "2.5 years", "MALE",
                    "Rocky was found as a stray with a broken leg that has since healed perfectly. He's a goofy, energetic boxer who thinks he's a lap dog. Great with older kids and loves playing tug-of-war.",
                    R.drawable.img_dog));

            pets.add(createPet("Daisy", "DOG", "Dachshund", "Red", "SMALL",
                    "4 years", "FEMALE",
                    "Daisy was surrendered when her family had to relocate overseas. She's a sweet, long-bodied girl with a big personality. She loves burrowing under blankets and going on short, sniff-intensive walks.",
                    R.drawable.img_dog));

            pets.add(createPet("Bear", "DOG", "Chow Chow", "Cream", "LARGE",
                    "3 years", "MALE",
                    "Bear was found wandering in a forest reserve. His fluffy mane makes him look like a little lion. He's calm and dignified but needs an owner who understands the Chow Chow's independent nature.",
                    R.drawable.img_dog));

            pets.add(createPet("Pepper", "DOG", "Border Collie", "Black & White", "MEDIUM",
                    "1.5 years", "FEMALE",
                    "Pepper was given up because her owners couldn't keep up with her intelligence. She knows over 15 commands, loves agility courses, and needs a home that can give her both mental and physical exercise.",
                    R.drawable.img_dog));

            pets.add(createPet("Rosie", "DOG", "Cavalier King Charles Spaniel", "Blenheim", "SMALL",
                    "2 years", "FEMALE",
                    "Rosie was rescued from a hoarding situation. She's a gentle, affectionate spaniel who just wants to be near people. Great with cats and other small dogs. Perfect for apartment living.",
                    R.drawable.img_dog));

            pets.add(createPet("Duke", "DOG", "Great Dane", "Harlequin", "LARGE",
                    "2 years", "MALE",
                    "Duke was surrendered when he grew too big for his family's apartment. Don't let his size intimidate you — he's a gentle giant who thinks he's a lap dog. He's well-mannered and walks beautifully on leash.",
                    R.drawable.img_dog));

            pets.add(createPet("Penny", "DOG", "Pomeranian", "Orange Sable", "SMALL",
                    "3 years", "FEMALE",
                    "Penny was found abandoned in a park with severe matting. After grooming and care, she's transformed into a fluffy princess. She's sassy, confident, and loves being the center of attention.",
                    R.drawable.img_dog));

            pets.add(createPet("Rex", "DOG", "Rottweiler", "Black & Mahogany", "LARGE",
                    "3.5 years", "MALE",
                    "Rex was rescued from a junkyard where he was used as a guard dog. With patience and love, he's become a loyal and gentle companion. He bonds deeply and is fiercely protective of his family.",
                    R.drawable.img_dog));

            // ===== CATS (13) =====
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

            pets.add(createPet("Willow", "CAT", "Siamese", "Seal Point", "SMALL",
                    "1.5 years", "FEMALE",
                    "Willow was found meowing outside a temple. She's a talkative, elegant Siamese with striking blue eyes. She'll follow you around the house and 'chat' about her day. Very people-oriented.",
                    R.drawable.img_cat));

            pets.add(createPet("Cleo", "CAT", "Persian", "White", "MEDIUM",
                    "3 years", "FEMALE",
                    "Cleo was surrendered due to her high grooming needs. She's a regal, flat-faced beauty who loves lounging on velvet cushions. She needs a dedicated owner who can maintain her luxurious coat.",
                    R.drawable.img_cat));

            pets.add(createPet("Nala", "CAT", "Bengal", "Spotted Brown", "MEDIUM",
                    "2 years", "FEMALE",
                    "Nala was rescued from an illegal breeder. She has stunning leopard-like spots and boundless energy. She loves climbing, exploring, and playing in water — very dog-like in personality!",
                    R.drawable.img_cat));

            pets.add(createPet("Loki", "CAT", "Maine Coon", "Brown Tabby", "LARGE",
                    "3 years", "MALE",
                    "Loki was found as a tiny kitten during a thunderstorm. He's grown into a massive, gentle giant with tufted ears and a magnificent tail. He chirps instead of meows and loves playing in water bowls.",
                    R.drawable.img_cat));

            pets.add(createPet("Misty", "CAT", "Russian Blue", "Silver Blue", "MEDIUM",
                    "2 years", "FEMALE",
                    "Misty was surrendered when her owner developed severe allergies. She's a shimmering beauty with emerald eyes. She's shy at first but once she trusts you, she's the most loyal companion.",
                    R.drawable.img_cat));

            pets.add(createPet("Ziggy", "CAT", "Sphynx", "Pink", "SMALL",
                    "2 years", "MALE",
                    "Ziggy was rescued from a breeder who couldn't sell him. He's a warm, wrinkly, hairless cat who loves sweaters and snuggling for warmth. His extroverted personality more than makes up for his unusual looks.",
                    R.drawable.img_cat));

            pets.add(createPet("Daisy", "CAT", "Scottish Fold", "Silver Tabby", "MEDIUM",
                    "1.5 years", "FEMALE",
                    "Daisy was found abandoned in a rental apartment. With her folded ears and round eyes, she looks like a living teddy bear. She's quiet, sweet, and enjoys perching on windowsills to watch birds.",
                    R.drawable.img_cat));

            pets.add(createPet("Pumpkin", "CAT", "Abyssinian", "Ruddy", "SMALL",
                    "1 year", "MALE",
                    "Pumpkin was rescued from a hoarding case with 40 other cats. He's a sleek, ticked-coat beauty with an adventurous spirit. He loves climbing to the highest point in any room and surveying his kingdom.",
                    R.drawable.img_cat));

            pets.add(createPet("Luna", "CAT", "Tuxedo", "Black & White", "MEDIUM",
                    "2 years", "FEMALE",
                    "Luna was born in a library basement and raised by the librarians. She's a dapper tuxedo cat who loves curling up on books and papers. She's litter-trained, well-mannered, and very photogenic.",
                    R.drawable.img_cat));

            // ===== BIRDS (8) =====
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

            pets.add(createPet("Coco", "BIRD", "Lovebird", "Peach-faced", "SMALL",
                    "1 year", "FEMALE",
                    "Coco was found perched on a fire escape during a cold winter night. She's a vibrant little lovebird who adores attention and cuddles. She loves shredding paper toys and taking baths in her water dish.",
                    R.drawable.img_bird));

            pets.add(createPet("Mango", "BIRD", "Sun Conure", "Orange & Yellow", "SMALL",
                    "2 years", "MALE",
                    "Mango was surrendered when his family moved into a no-pets building. True to his name, he's a burst of tropical color and personality. He's loud, playful, and loves dancing to music.",
                    R.drawable.img_bird));

            pets.add(createPet("Blue", "BIRD", "Parrotlet", "Blue", "SMALL",
                    "1 year", "MALE",
                    "Blue was found in a public park, probably escaped. He's a tiny parrot with a huge personality. Despite his small size, he's fearless and loves to learn tricks. He can already wave and spin on command.",
                    R.drawable.img_bird));

            pets.add(createPet("Sky", "BIRD", "Zebra Finch", "Gray & White", "SMALL",
                    "0.5 years", "FEMALE",
                    "Sky was part of an accidental litter at a local pet store's overcrowded enclosure. She's a delicate, beeping little finch who loves company. She does best in pairs — adopt her with a finch friend!",
                    R.drawable.img_bird));

            pets.add(createPet("Kiwi", "BIRD", "Green-cheeked Conure", "Green & Red", "SMALL",
                    "2 years", "MALE",
                    "Kiwi was rescued from an owner who didn't realize how long parrots live. He's a cuddly, clownish conure who loves hiding in hair and hoodie pockets. He's well-socialized and gets along with everyone.",
                    R.drawable.img_bird));

            db.petDao().insertAll(pets);
            Log.d(TAG, "Database seeded with " + pets.size() + " pets and 1 admin account.");
        });
    }

    private static String generateInitialPassword() {
        java.security.SecureRandom rng = new java.security.SecureRandom();
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb.toString();
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

        // Carousel: photo + 2 decorative type cards
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
}
