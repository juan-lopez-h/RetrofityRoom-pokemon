package com.example.pruebaapi1rjo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TypeListResponseDto(
    @SerializedName("results") val results: List<NamedResourceDto>
)

data class TypeDetailResponseDto(
    @SerializedName("pokemon") val pokemon: List<TypePokemonSlotDto>
)

data class TypePokemonSlotDto(
    @SerializedName("pokemon") val pokemon: NamedResourceDto
)

data class NamedResourceDto(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)
