package com.example.pruebaapi1rjo.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.pruebaapi1rjo.data.repository.PokemonRepository
import com.example.pruebaapi1rjo.domain.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private val _selectedPokemon = MutableStateFlow<Pokemon?>(null)
    val selectedPokemon = _selectedPokemon.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail = _isLoadingDetail.asStateFlow()

    init {
        observeNetwork()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemons: Flow<PagingData<Pokemon>> = _searchQuery
        .flatMapLatest { query ->
            repository.getPokemonPagingData(query)
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectPokemon(name: String) {
        _isLoadingDetail.value = true
        _selectedPokemon.value = null
        viewModelScope.launch {
            repository.getPokemonDetail(name).onSuccess {
                _selectedPokemon.value = it
            }.onFailure {
                // Handle error
            }
            _isLoadingDetail.value = false
        }
    }

    private fun observeNetwork() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isOnline.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
