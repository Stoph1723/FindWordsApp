package com.findwords.app.data

import android.content.Context
import com.findwords.app.model.GameModels.GameState
import com.findwords.app.model.GameModels.Level
import com.findwords.app.model.GameModels.PackInfo
import com.findwords.app.model.GameModels.WordPack
import com.findwords.app.model.GameModels.WordPlacement
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class GameRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val gson = Gson()

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow().distinctUntilChanged()

    init {
        loadGameState()
        initializeLevels()
    }

    private fun loadGameState() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob()).launch {
            val state = db.gameStateDao().getStateSync("main") ?: GameState()
            _gameState.value = state
        }
    }

    private fun saveGameState() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob()).launch {
            db.gameStateDao().insert(_gameState.value)
        }
    }

    private fun initializeLevels() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob()).launch {
            val count = db.levelDao().getCompletedCount(1)
            if (count == 0) {
                generateAllLevels()
            }
        }
    }

    private suspend fun generateAllLevels() {
        val json = context.assets.open("word_packs.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<WordPack>>() {}.type
        val packs = gson.fromJson<List<WordPack>>(json, type)

        val allLevels = mutableListOf<Level>()
        var levelId = 1

        for (pack in packs) {
            for (levelIndex in pack.wordLists.indices) {
                val words = pack.wordLists[levelIndex]
                val result = WordSearchGenerator.generate(words, pack.gridSize)

                val level = Level(
                    id = levelId,
                    packId = pack.id,
                    packName = pack.name,
                    levelIndex = levelIndex + 1,
                    words = gson.toJson(result.words),
                    grid = gson.toJson(result.grid),
                    gridSize = pack.gridSize
                )
                allLevels.add(level)
                levelId++
            }
        }

        db.levelDao().insertAll(allLevels)
    }

    suspend fun getPacks(): List<PackInfo> {
        return withContext(Dispatchers.IO) {
            val json = context.assets.open("word_packs.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<WordPack>>() {}.type
            val packs = gson.fromJson<List<WordPack>>(json, type)

            packs.map { pack ->
                val completed = db.levelDao().getCompletedCount(pack.id)
                PackInfo(pack.id, pack.name, pack.theme, pack.wordLists.size, completed, pack.difficulty)
            }
        }
    }

    suspend fun getLevel(packId: Int, levelIndex: Int): Level? {
        return db.levelDao().getLevel(packId, levelIndex)
    }

    suspend fun getLevelById(levelId: Int): Level? {
        return db.levelDao().getLevelById(levelId)
    }

    suspend fun completeLevel(levelId: Int, timeElapsed: Long, hintsUsed: Int) {
        db.levelDao().markCompleted(levelId, System.currentTimeMillis(), 3, timeElapsed, hintsUsed)

        val currentState = _gameState.value
        val stars = when {
            hintsUsed == 0 && timeElapsed < 60000 -> 3
            hintsUsed <= 1 && timeElapsed < 120000 -> 2
            else -> 1
        }
        val coinReward = when (stars) {
            3 -> 50
            2 -> 30
            else -> 10
        }

        _gameState.value = currentState.copy(
            completedLevels = currentState.completedLevels + levelId,
            totalStars = currentState.totalStars + stars,
            coins = currentState.coins + coinReward
        )
        saveGameState()
    }

    fun addCoins(amount: Int) {
        _gameState.value = _gameState.value.copy(coins = _gameState.value.coins + amount)
        saveGameState()
    }

    fun spendCoins(amount: Int): Boolean {
        if (_gameState.value.coins >= amount) {
            _gameState.value = _gameState.value.copy(coins = _gameState.value.coins - amount)
            saveGameState()
            return true
        }
        return false
    }

    suspend fun getLevelsForPack(packId: Int): List<LevelInfo> {
        return withContext(Dispatchers.IO) {
            db.levelDao().getLevelsByPack(packId).map { level ->
                LevelInfo(
                    id = level.id,
                    levelNumber = level.levelIndex,
                    isUnlocked = true,
                    starsEarned = level.starsEarned,
                    isCompleted = level.isCompleted
                )
            }
        }
    }

    data class LevelInfo(
        val id: Int,
        val levelNumber: Int,
        val isUnlocked: Boolean,
        val starsEarned: Int,
        val isCompleted: Boolean
    )
}