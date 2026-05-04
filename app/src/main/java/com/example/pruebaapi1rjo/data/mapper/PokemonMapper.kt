package com.example.pruebaapi1rjo.data.mapper

import com.example.pruebaapi1rjo.data.local.entity.PokemonEntity
import com.example.pruebaapi1rjo.data.remote.dto.PokemonDetailDto
import com.example.pruebaapi1rjo.domain.model.Pokemon
import com.example.pruebaapi1rjo.domain.model.PokemonStat

fun PokemonDetailDto.toPokemonEntity(): PokemonEntity {
    return PokemonEntity(
        id = id,
        name = name,
        imageUrl = sprites.other.officialArtwork.frontDefault ?: "",
        types = types.joinToString(",") { it.type.name },
        height = height,
        weight = weight
    )
}

fun PokemonEntity.toPokemon(): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types.split(",").filter { it.isNotBlank() },
        height = height,
        weight = weight
    )
}

fun PokemonDetailDto.toPokemon(): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        imageUrl = sprites.other.officialArtwork.frontDefault ?: "",
        types = types.map { it.type.name },
        height = height,
        weight = weight,
        stats = stats.map { PokemonStat(it.stat.name, it.baseStat) }
    )
}
