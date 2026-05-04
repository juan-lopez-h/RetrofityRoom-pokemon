package com.example.pruebaapi1rjo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val pokemonId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
