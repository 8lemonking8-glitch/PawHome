package com.example.midtermproject.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class AdoptionRequestWithDetails {
    @Embedded
    public AdoptionRequestEntity request;

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    public UserEntity user;

    @Relation(
        parentColumn = "petId",
        entityColumn = "id"
    )
    public PetEntity pet;
}
