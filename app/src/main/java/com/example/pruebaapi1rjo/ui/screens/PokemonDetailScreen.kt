package com.example.pruebaapi1rjo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pruebaapi1rjo.ui.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    viewModel: PokemonViewModel,
    pokemonName: String,
    onBackClick: () -> Unit
) {
    val pokemon by viewModel.selectedPokemon.collectAsState()
    val isLoading by viewModel.isLoadingDetail.collectAsState()

    LaunchedEffect(pokemonName) {
        viewModel.selectPokemon(pokemonName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pokemonName.replaceFirstChar { it.uppercase() }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                pokemon?.let { p ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = p.imageUrl,
                            contentDescription = p.name,
                            modifier = Modifier.size(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Types",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            p.types.forEach { type ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(type) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DetailInfoItem(label = "Height", value = "${p.height / 10.0} m")
                            DetailInfoItem(label = "Weight", value = "${p.weight / 10.0} kg")
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (p.stats.isNotEmpty()) {
                            Text(
                                text = "Base Stats",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            p.stats.forEach { stat ->
                                StatRow(name = stat.name, value = stat.value)
                            }
                        }
                    }
                } ?: Text("Pokemon not found or no internet connection")
            }
        }
    }
}

@Composable
fun DetailInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun StatRow(name: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.uppercase(),
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = { value / 150f },
            modifier = Modifier.weight(0.5f).height(8.dp)
        )
        Text(
            text = value.toString(),
            modifier = Modifier.weight(0.2f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
