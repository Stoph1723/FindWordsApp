package com.findwords.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.findwords.app.data.GameRepository
import com.findwords.app.data.LevelRepository
import com.findwords.app.model.GameModels.GameState
import com.findwords.app.model.GameModels.Level
import com.findwords.app.model.GameModels.WordPlacement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _currentLevel = MutableStateFlow<Level?>(null)
    val currentLevel = _currentLevel.asStateFlow()

    private val _foundWords = MutableStateFlow<List<WordPlacement>>(emptyList())
    val foundWords = _foundWords.asStateFlow()

    private val _selectedCells = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val selectedCells = _selectedCells.asStateFlow()

    private val _gameCompleted = MutableStateFlow(false)
    val gameCompleted = _gameCompleted.asStateFlow()

    private val _startTime = System.currentTimeMillis()
    private var _hintsUsed = 0

    var currentPackId = 1
    var currentLevelIndex = 1

    fun loadLevel(packId: Int, levelIndex: Int) {
        viewModelScope.launch {
            currentPackId = packId
            currentLevelIndex = levelIndex
            val level = repository.getLevel(packId, levelIndex)
            _currentLevel.value = level

            if (level != null) {
                val words = com.google.gson.Gson().fromJson(
                    level.words,
                    object : com.google.gson.reflect.TypeToken<List<WordPlacement>>() {}.type
                )
                _foundWords.value = words.filter { it.found }
                _selectedCells.value = emptyList()
                _gameCompleted.value = false
                _startTime = System.currentTimeMillis()
                _hintsUsed = 0
            }
        }
    }

    fun onCellSelected(row: Int, col: Int) {
        val current = _selectedCells.value.toMutableList()
        if (current.contains(row to col)) {
            current.remove(row to col)
        } else {
            current.add(row to col)
        }
        _selectedCells.value = current
        checkWordFound()
    }

    fun clearSelection() {
        _selectedCells.value = emptyList()
    }

    private fun checkWordFound() {
        val level = _currentLevel.value ?: return
        val selected = _selectedCells.value
        if (selected.size < 3) return

        val words = com.google.gson.Gson().fromJson(
            level.words,
            object : com.google.gson.reflect.TypeToken<List<WordPlacement>>() {}.type
        )

        val found = words.find { w ->
            !w.found && getWordCells(w) == selected
        }

        if (found != null) {
            found.found = true
            val updated = words.map { if (it.word == found.word) found else it }
            _foundWords.value = updated
            _selectedCells.value = emptyList()

            if (updated.all { it.found }) {
                completeLevel()
            }
        }
    }

    private fun getWordCells(word: WordPlacement): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        val dRow = when (word.direction) {
            2, 3, 4 -> 1
            6, 7, 8 -> -1
            else -> 0
        }
        val dCol = when (word.direction) {
            1, 3, 7 -> 1
            4, 5, 8 -> -1
            else -> 0
        }

        var row = word.startRow
        var col = word.startCol
        repeat(word.word.length) {
            cells.add(row to col)
            row += dRow
            col += dCol
        }
        return cells
    }

    fun useHint(): Boolean {
        val state = repository.gameState.value
        if (state.coins < 50) return false

        val level = _currentLevel.value ?: return false
        val words = com.google.gson.Gson().fromJson(
            level.words,
            object : com.google.gson.reflect.TypeToken<List<WordPlacement>>() {}.type
        )

        val unfound = words.firstOrNull { !it.found } ?: return false

        // Reveal first letter
        val firstCell = getWordCells(unfound).first()
        val newFound = words.map { w ->
            if (w.word == unfound.word) w.copy(found = true) else w
        }
        _foundWords.value = newFound
        _hintsUsed++

        repository.spendCoins(50)
        return true
    }

    private fun completeLevel() {
        val timeElapsed = System.currentTimeMillis() - _startTime
        _gameCompleted.value = true

        val level = _currentLevel.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeLevel(level.id, timeElapsed, _hintsUsed)
        }
    }

    fun getCurrentState(): GameState {
        return repository.gameState.value
    }
}