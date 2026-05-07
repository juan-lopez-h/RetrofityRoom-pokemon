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

    private val _types = MutableStateFlow<List<String>>(emptyList())
    val types = _types.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType = _selectedType.asStateFlow()

    private val _habitats = MutableStateFlow<List<String>>(emptyList())
    val habitats = _habitats.asStateFlow()

    private val _selectedHabitat = MutableStateFlow<String?>(null)
    val selectedHabitat = _selectedHabitat.asStateFlow()

    private val _selectedPokemon = MutableStateFlow<Pokemon?>(null)
    val selectedPokemon = _selectedPokemon.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail = _isLoadingDetail.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError = _detailError.asStateFlow()

    init {
        observeNetwork()
        fetchFilters()
    }

    private fun fetchFilters() {
        viewModelScope.launch {
            repository.getTypes().onSuccess {
                _types.value = listOf("all") + it
            }
            repository.getHabitats().onSuccess {
                _habitats.value = listOf("all") + it
            }
        }
    }

    fun onTypeSelected(type: String?) {
        _selectedType.value = if (type == "all") null else type
        _selectedHabitat.value = null // Clear habitat when type selected
    }

    fun onHabitatSelected(habitat: String?) {
        _selectedHabitat.value = if (habitat == "all") null else habitat
        _selectedType.value = null // Clear type when habitat selected
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemons: Flow<PagingData<Pokemon>> = kotlinx.coroutines.flow.combine(
        _searchQuery,
        _selectedType,
        _selectedHabitat
    ) { query, type, habitat ->
        Triple(query, type, habitat)
    }.flatMapLatest { (query, type, habitat) ->
        repository.getPokemonPagingData(query, type, habitat)
    }.cachedIn(viewModelScope)

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectPokemon(name: String) {
        _isLoadingDetail.value = true
        _selectedPokemon.value = null
        _detailError.value = null
        viewModelScope.launch {
            repository.getPokemonDetail(name).onSuccess {
                _selectedPokemon.value = it
            }.onFailure {
                _detailError.value = "Failed to load $name. Please check your connection."
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
