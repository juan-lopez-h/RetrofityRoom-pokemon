package com.example.pruebaapi1rjo.data.remote

import com.example.pruebaapi1rjo.data.remote.dto.PokemonDetailDto
import com.example.pruebaapi1rjo.data.remote.dto.PokemonListDto
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

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
