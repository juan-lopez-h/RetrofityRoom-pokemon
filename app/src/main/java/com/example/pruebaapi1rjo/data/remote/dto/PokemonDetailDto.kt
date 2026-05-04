package com.example.pruebaapi1rjo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PokemonDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("sprites") val sprites: SpritesDto,
    @SerializedName("types") val types: List<TypeSlotDto>,
    @SerializedName("stats") val stats: List<StatSlotDto>
)

data class SpritesDto(
    @SerializedName("other") val other: OtherSpritesDto
)

data class OtherSpritesDto(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtworkDto
)

data class OfficialArtworkDto(
    @SerializedName("front_default") val frontDefault: String?
)

data class TypeSlotDto(
    @SerializedName("type") val type: TypeDto
)

data class TypeDto(
    @SerializedName("name") val name: String
)

data class StatSlotDto(
    @SerializedName("base_stat") val baseStat: Int,
    @SerializedName("stat") val stat: StatDto
)

data class StatDto(
    @SerializedName("name") val name: String
)
