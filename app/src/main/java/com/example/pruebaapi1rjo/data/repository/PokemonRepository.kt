package com.example.pruebaapi1rjo.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.pruebaapi1rjo.data.local.PokemonDatabase
import com.example.pruebaapi1rjo.data.mapper.toPokemon
import com.example.pruebaapi1rjo.domain.model.Pokemon
import com.example.pruebaapi1rjo.data.remote.PokemonApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val pokemonDb: PokemonDatabase,
    private val pokemonApi: PokemonApi
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getPokemonPagingData(query: String): Flow<PagingData<Pokemon>> {
        val pagingSourceFactory = { pokemonDb.dao.getPokemons("%$query%") }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = PokemonRemoteMediator(
                pokemonDb = pokemonDb,
                pokemonApi = pokemonApi
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toPokemon() }
        }
    }

    suspend fun getPokemonDetail(name: String): Result<Pokemon> {
        return try {
            // Try to get from local first
            val local = pokemonDb.dao.getPokemonByName(name)
            if (local != null) {
                // We might need extra details (stats) from API if not in entity
                // For simplicity, we always fetch detail for stats
                val remoteDetail = pokemonApi.getPokemonDetail(name)
                Result.success(remoteDetail.toPokemon())
            } else {
                val remoteDetail = pokemonApi.getPokemonDetail(name)
                Result.success(remoteDetail.toPokemon())
            }
        } catch (e: Exception) {
            // If offline and we have local, return local without stats
            val local = pokemonDb.dao.getPokemonByName(name)
            if (local != null) {
                Result.success(local.toPokemon())
            } else {
                Result.failure(e)
            }
        }
    }
}
