package com.example.pruebaapi1rjo.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pruebaapi1rjo.data.local.entity.PokemonEntity
import com.example.pruebaapi1rjo.data.local.entity.RemoteKeyEntity

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemons: List<PokemonEntity>)

    @Query("SELECT * FROM pokemon_entity WHERE name LIKE :query OR types LIKE :query")
    fun getPokemons(query: String): PagingSource<Int, PokemonEntity>

    @Query("SELECT * FROM pokemon_entity WHERE name = :name")
    suspend fun getPokemonByName(name: String): PokemonEntity?

    @Query("DELETE FROM pokemon_entity")
    suspend fun clearAll()

    // Remote Keys
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeys(remoteKey: List<RemoteKeyEntity>)

    @Query("SELECT * FROM remote_keys WHERE pokemonId = :id")
    suspend fun getRemoteKeysForPokemon(id: Int): RemoteKeyEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}
