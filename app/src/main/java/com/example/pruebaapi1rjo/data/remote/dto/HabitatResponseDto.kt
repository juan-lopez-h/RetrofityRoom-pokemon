package com.example.pruebaapi1rjo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HabitatListResponseDto(
    @SerializedName("results") val results: List<NamedResourceDto>
)

data class HabitatDetailResponseDto(
    @SerializedName("pokemon_species") val pokemonSpecies: List<NamedResourceDto>
)
