package com.example.pruebaapi1rjo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.pruebaapi1rjo.domain.model.Pokemon
import com.example.pruebaapi1rjo.ui.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    viewModel: PokemonViewModel,
    onPokemonClick: (String) -> Unit
) {
    val pokemons = viewModel.pokemons.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val types by viewModel.types.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val habitats by viewModel.habitats.collectAsState()
    val selectedHabitat by viewModel.selectedHabitat.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Pokedex") }
                )
                if (!isOnline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Offline Mode", color = Color.White, fontSize = 12.sp)
                    }
                }
                TextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = { Text("Search by name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                
                Text(
                    "Filter by Type:", 
                    style = MaterialTheme.typography.labelSmall, 
                    modifier = Modifier.padding(start = 8.dp)
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(types) { type ->
                        FilterChip(
                            selected = (selectedType ?: "all") == type,
                            onClick = { viewModel.onTypeSelected(type) },
                            label = { Text(type.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Text(
                    "Filter by Habitat:", 
                    style = MaterialTheme.typography.labelSmall, 
                    modifier = Modifier.padding(start = 8.dp)
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habitats) { habitat ->
                        FilterChip(
                            selected = (selectedHabitat ?: "all") == habitat,
                            onClick = { viewModel.onHabitatSelected(habitat) },
                            label = { Text(habitat.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (pokemons.loadState.refresh is LoadState.Error) {
                val error = (pokemons.loadState.refresh as LoadState.Error).error
                ErrorState(
                    message = "Could not load Pokedex. ${error.localizedMessage}",
                    onRetry = { pokemons.retry() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = pokemons.itemCount,
                        key = pokemons.itemKey { it.id }
                    ) { index ->
                        val pokemon = pokemons[index]
                        if (pokemon != null) {
                            PokemonItem(pokemon = pokemon) {
                                onPokemonClick(pokemon.name)
                            }
                        }
                    }

                    when (val state = pokemons.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Error loading more", color = MaterialTheme.colorScheme.error)
                                    Button(onClick = { pokemons.retry() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                if (pokemons.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Refresh, 
            contentDescription = null, 
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun PokemonItem(
    pokemon: Pokemon,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { type ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(type, fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}
