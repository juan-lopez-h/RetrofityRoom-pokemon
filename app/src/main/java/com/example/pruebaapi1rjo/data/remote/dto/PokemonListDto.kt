package com.example.pruebaapi1rjo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PokemonListDto(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<PokemonListItemDto>
)

data class PokemonListItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)
