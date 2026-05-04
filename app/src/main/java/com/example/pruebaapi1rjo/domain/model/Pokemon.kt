package com.example.pruebaapi1rjo.domain.model

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String> = emptyList(),
    val height: Int = 0,
    val weight: Int = 0,
    val stats: List<PokemonStat> = emptyList()
)

data class PokemonStat(
    val name: String,
    val value: Int
)
