package com.example.pruebaapi1rjo.data.mapper

import com.example.pruebaapi1rjo.data.remote.dto.*
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonMapperTest {

    @Test
    fun `toPokemon maps DTO to Domain model correctly`() {
        val dto = PokemonDetailDto(
            id = 1,
            name = "bulbasaur",
            height = 7,
            weight = 69,
            sprites = SpritesDto(
                other = OtherSpritesDto(
                    officialArtwork = OfficialArtworkDto(
                        frontDefault = "url_to_image"
                    )
                )
            ),
            types = listOf(
                TypeSlotDto(type = TypeDto(name = "grass")),
                TypeSlotDto(type = TypeDto(name = "poison"))
            ),
            stats = listOf(
                StatSlotDto(baseStat = 45, stat = StatDto(name = "hp")),
                StatSlotDto(baseStat = 49, stat = StatDto(name = "attack"))
            )
        )

        val domain = dto.toPokemon()

        assertEquals(1, domain.id)
        assertEquals("bulbasaur", domain.name)
        assertEquals("url_to_image", domain.imageUrl)
        assertEquals(listOf("grass", "poison"), domain.types)
        assertEquals(45, domain.stats[0].value)
        assertEquals("hp", domain.stats[0].name)
    }
}
