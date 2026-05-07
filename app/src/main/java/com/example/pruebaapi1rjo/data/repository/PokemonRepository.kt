package com.example.pruebaapi1rjo.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.pruebaapi1rjo.data.local.PokemonDao
import com.example.pruebaapi1rjo.data.local.PokemonDatabase
import com.example.pruebaapi1rjo.data.mapper.toPokemon
import com.example.pruebaapi1rjo.data.mapper.toPokemonEntity
import com.example.pruebaapi1rjo.domain.model.Pokemon
import com.example.pruebaapi1rjo.data.remote.PokemonApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val pokemonDb: PokemonDatabase,
    private val pokemonDao: PokemonDao,
    private val pokemonApi: PokemonApi
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getPokemonPagingData(
        query: String, 
        typeFilter: String? = null,
        habitatFilter: String? = null
    ): Flow<PagingData<Pokemon>> {
        val pagingSourceFactory = { 
            if (typeFilter != null) {
                pokemonDao.getPokemons("%$typeFilter%")
            } else if (habitatFilter != null) {
                // Habitat filter doesn't map easily to local search since habitat isn't in entity
                // For simplicity in this workshop, we use name search if no type
                pokemonDao.getPokemons("%%")
            } else {
                pokemonDao.getPokemons("%$query%")
            }
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = PokemonRemoteMediator(
                pokemonDb = pokemonDb,
                pokemonApi = pokemonApi,
                typeFilter = typeFilter,
                habitatFilter = habitatFilter
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toPokemon() }
        }
    }

    suspend fun getHabitats(): Result<List<String>> {
        return try {
            val response = pokemonApi.getHabitatList()
            Result.success(response.results.map { it.name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPokemonDetail(name: String): Result<Pokemon> {
        return try {
            val local = pokemonDao.getPokemonByName(name)
            
            // If we have local and it has a description AND stats, return it immediately
            if (local != null && local.description.isNotBlank() && local.stats.isNotBlank()) {
                Result.success(local.toPokemon())
            } else {
                val remoteDetail = pokemonApi.getPokemonDetail(name)
                
                // If we don't have description, fetch it
                val description = if (local?.description.isNullOrBlank()) {
                    val species = pokemonApi.getPokemonSpecies(name)
                    species.flavorTextEntries
                        .firstOrNull { it.language.name == "en" }
                        ?.flavorText?.replace("\n", " ") ?: ""
                } else {
                    local!!.description
                }
                
                // Save to local for next time
                val entity = remoteDetail.toPokemonEntity(description)
                pokemonDao.insertAll(listOf(entity))
                
                Result.success(remoteDetail.toPokemon(description))
            }
        } catch (e: Exception) {
            val local = pokemonDao.getPokemonByName(name)
            if (local != null) {
                Result.success(local.toPokemon())
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getTypes(): Result<List<String>> {
        return try {
            val response = pokemonApi.getTypeList()
            Result.success(response.results.map { it.name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
