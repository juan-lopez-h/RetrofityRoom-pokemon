package com.example.pruebaapi1rjo.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.pruebaapi1rjo.data.local.PokemonDatabase
import com.example.pruebaapi1rjo.data.local.entity.PokemonEntity
import com.example.pruebaapi1rjo.data.local.entity.RemoteKeyEntity
import com.example.pruebaapi1rjo.data.mapper.toPokemonEntity
import com.example.pruebaapi1rjo.data.remote.PokemonApi
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val pokemonDb: PokemonDatabase,
    private val pokemonApi: PokemonApi
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextKey?.minus(state.config.pageSize) ?: 0
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            val response = pokemonApi.getPokemonList(
                limit = state.config.pageSize,
                offset = loadKey
            )

            // For each pokemon in list, we need the detail to get the image and types
            val pokemonEntities = response.results.map { listItem ->
                val detail = pokemonApi.getPokemonDetail(listItem.name)
                detail.toPokemonEntity()
            }

            val endOfPaginationReached = response.next == null

            pokemonDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    pokemonDb.dao.clearAll()
                    pokemonDb.dao.clearRemoteKeys()
                }
                val prevKey = if (loadKey == 0) null else loadKey - state.config.pageSize
                val nextKey = if (endOfPaginationReached) null else loadKey + state.config.pageSize
                val keys = pokemonEntities.map {
                    RemoteKeyEntity(pokemonId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                pokemonDb.dao.insertAllRemoteKeys(keys)
                pokemonDb.dao.insertAll(pokemonEntities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PokemonEntity>): RemoteKeyEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { pokemon ->
                pokemonDb.dao.getRemoteKeysForPokemon(pokemon.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, PokemonEntity>): RemoteKeyEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                pokemonDb.dao.getRemoteKeysForPokemon(id)
            }
        }
    }
}
