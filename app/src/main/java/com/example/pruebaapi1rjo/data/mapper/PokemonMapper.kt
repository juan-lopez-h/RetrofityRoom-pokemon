package com.example.pruebaapi1rjo.data.mapper

import com.example.pruebaapi1rjo.data.local.entity.PokemonEntity
import com.example.pruebaapi1rjo.data.remote.dto.PokemonDetailDto
import com.example.pruebaapi1rjo.domain.model.Pokemon
import com.example.pruebaapi1rjo.domain.model.PokemonStat

fun PokemonDetailDto.toPokemonEntity(description: String = ""): PokemonEntity {
    return PokemonEntity(
        id = id,
        name = name,
        imageUrl = sprites.other.officialArtwork.frontDefault ?: "",
        types = types.joinToString(",") { it.type.name },
        height = height,
        weight = weight,
        description = description,
        stats = stats.joinToString(",") { "${it.stat.name}:${it.baseStat}" }
    )
}

fun PokemonEntity.toPokemon(): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types.split(",").filter { it.isNotBlank() },
        height = height,
        weight = weight,
        description = description,
        stats = stats.split(",").filter { it.contains(":") }.map {
            val parts = it.split(":")
            PokemonStat(parts[0], parts[1].toInt())
        }
    )
}

fun PokemonDetailDto.toPokemon(description: String = ""): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        imageUrl = sprites.other.officialArtwork.frontDefault ?: "",
        types = types.map { it.type.name },
        height = height,
        weight = weight,
        stats = stats.map { PokemonStat(it.stat.name, it.baseStat) },
        description = description
    )
}
