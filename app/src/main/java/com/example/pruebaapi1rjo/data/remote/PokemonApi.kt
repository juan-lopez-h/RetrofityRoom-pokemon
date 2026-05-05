package com.example.pruebaapi1rjo.data.remote

import com.example.pruebaapi1rjo.data.remote.dto.PokemonDetailDto
import com.example.pruebaapi1rjo.data.remote.dto.PokemonListDto
import com.example.pruebaapi1rjo.data.remote.dto.PokemonSpeciesDto
import com.example.pruebaapi1rjo.data.remote.dto.TypeDetailResponseDto
import com.example.pruebaapi1rjo.data.remote.dto.TypeListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApi {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonListDto

    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(
        @Path("name") name: String
    ): PokemonDetailDto

    @GET("pokemon-species/{name}")
    suspend fun getPokemonSpecies(
        @Path("name") name: String
    ): PokemonSpeciesDto

    @GET("type")
    suspend fun getTypeList(): TypeListResponseDto

    @GET("type/{name}")
    suspend fun getTypeDetail(
        @Path("name") name: String
    ): TypeDetailResponseDto

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
