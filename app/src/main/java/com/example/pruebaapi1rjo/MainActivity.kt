package com.example.pruebaapi1rjo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.pruebaapi1rjo.ui.PokemonViewModel
import com.example.pruebaapi1rjo.ui.screens.PokemonDetailScreen
import com.example.pruebaapi1rjo.ui.screens.PokemonListScreen
import com.example.pruebaapi1rjo.ui.theme.PruebaApi1RjoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PruebaApi1RjoTheme {
                PokedexApp()
            }
        }
    }
}

@Composable
fun PokedexApp() {
    val navController = rememberNavController()
    val viewModel: PokemonViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            PokemonListScreen(
                viewModel = viewModel,
                onPokemonClick = { name ->
                    navController.navigate("detail/$name")
                }
            )
        }
        composable(
            route = "detail/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            PokemonDetailScreen(
                viewModel = viewModel,
                pokemonName = name,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
