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
    fun getPokemonPagingData(query: String, typeFilter: String? = null): Flow<PagingData<Pokemon>> {
        val pagingSourceFactory = { 
            if (typeFilter != null) {
                pokemonDao.getPokemons("%$typeFilter%")
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
                typeFilter = typeFilter
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toPokemon() }
        }
    }

    suspend fun getPokemonDetail(name: String): Result<Pokemon> {
        return try {
            val local = pokemonDao.getPokemonByName(name)
            
            // If we have local but no description, we should fetch from remote
            if (local != null && local.description.isNotBlank()) {
                val remoteDetail = pokemonApi.getPokemonDetail(name)
                Result.success(remoteDetail.toPokemon(local.description))
            } else {
                val remoteDetail = pokemonApi.getPokemonDetail(name)
                val species = pokemonApi.getPokemonSpecies(name)
                val description = species.flavorTextEntries
                    .firstOrNull { it.language.name == "en" }
                    ?.flavorText?.replace("\n", " ") ?: ""
                
                // Update local with description
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
