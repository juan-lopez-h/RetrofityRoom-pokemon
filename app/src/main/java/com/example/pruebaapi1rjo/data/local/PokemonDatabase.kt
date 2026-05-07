package com.example.pruebaapi1rjo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pruebaapi1rjo.data.local.entity.PokemonEntity
import com.example.pruebaapi1rjo.data.local.entity.RemoteKeyEntity

@Database(
    entities = [PokemonEntity::class, RemoteKeyEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {
    abstract val dao: PokemonDao
}
