package com.example.pruebaapi1rjo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_entity")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val types: String, // Comma-separated
    val height: Int,
    val weight: Int,
    val description: String = ""
)
