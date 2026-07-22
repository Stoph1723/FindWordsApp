package com.findwords.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.findwords.app.data.GameRepository
import com.findwords.app.data.LevelRepository
import com.findwords.app.model.GameModels.GameState
import com.findwords.app.model.GameModels.PackInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _packs = MutableStateFlow<List<PackInfo>>(emptyList())
    val packs = _packs.asStateFlow()

    private val _gameState = MutableStateFlow(repository.gameState.value)
    val gameState = _gameState.asStateFlow()

    init {
        loadPacks()
        observeGameState()
    }

    private fun loadPacks() {
        viewModelScope.launch {
            _packs.value = repository.getPacks()
        }
    }

    private fun observeGameState() {
        viewModelScope.launch {
            repository.gameState.collect { state ->
                _gameState.value = state
            }
        }
    }

    fun refreshPacks() {
        loadPacks()
    }

    fun addCoins(amount: Int) {
        repository.addCoins(amount)
    }

    fun spendCoins(amount: Int): Boolean {
        return repository.spendCoins(amount)
    }
}