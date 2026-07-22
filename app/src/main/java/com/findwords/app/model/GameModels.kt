package com.findwords.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "level")
@Serializable
data class Level(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packId: Int = 0,
    val packName: String = "",
    val levelIndex: Int = 0,
    val words: String = "[]",
    val grid: String = "[]",
    val gridSize: Int = 10,
    var isCompleted: Boolean = false,
    var completedAt: Long = 0,
    var starsEarned: Int = 0,
    var timeElapsed: Long = 0,
    var hintsUsed: Int = 0
) {
    val wordList: List<WordPlacement>
        get() = com.google.gson.Gson().fromJson(
            words,
            object : com.google.gson.reflect.TypeToken<List<WordPlacement>>() {}.type
        )

    val gridArray: Array<CharArray>
        get() = com.google.gson.Gson().fromJson(
            grid,
            Array<CharArray>::class.java
        )
}

@Serializable
data class WordPlacement(
    val word: String,
    val startRow: Int,
    val startCol: Int,
    val direction: Int,
    var found: Boolean = false
)

@Entity(tableName = "game_state", primaryKeys = ["key"])
@Serializable
data class GameState(
    val key: String = "main",
    val coins: Int = 100,
    val totalStars: Int = 0,
    val completedLevels: List<Int> = emptyList(),
    val currentPackId: Int = 1,
    val currentLevelIndex: Int = 1,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val highScore: Int = 0,
    val totalPlayTime: Long = 0,
    val dailyStreak: Int = 0,
    val lastPlayedDate: Long = 0
)

data class WordPack(
    val id: Int,
    val name: String,
    val theme: String,
    val wordLists: List<List<String>>,
    val gridSize: Int,
    val difficulty: Int
)

data class PackInfo(
    val id: Int,
    val name: String,
    val theme: String,
    val totalLevels: Int,
    val completedLevels: Int,
    val difficulty: Int
) {
    val progress: Float get() = if (totalLevels > 0) completedLevels.toFloat() / totalLevels else 0f
}