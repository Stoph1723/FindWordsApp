package com.findwords.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.findwords.app.model.GameModels.GameState
import com.findwords.app.model.GameModels.Level
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<Level>)

    @Query("SELECT * FROM level WHERE packId = :packId ORDER BY levelIndex")
    fun getLevelsByPack(packId: Int): Flow<List<Level>>

    @Query("SELECT * FROM level WHERE packId = :packId AND levelIndex = :levelIndex")
    suspend fun getLevel(packId: Int, levelIndex: Int): Level?

    @Query("SELECT * FROM level WHERE id = :id")
    suspend fun getLevelById(id: Int): Level?

    @Query("SELECT COUNT(*) FROM level WHERE packId = :packId AND isCompleted = 1")
    suspend fun getCompletedCount(packId: Int): Int

    @Query("UPDATE level SET isCompleted = 1, completedAt = :timestamp, starsEarned = :stars, timeElapsed = :time, hintsUsed = :hints WHERE id = :id")
    suspend fun markCompleted(id: Int, timestamp: Long, stars: Int, time: Long, hints: Int)

    @Query("SELECT * FROM level WHERE isCompleted = 0 ORDER BY packId, levelIndex LIMIT 1")
    suspend fun getNextUncompleted(): Level?
}

@Dao
interface GameStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: GameState)

    @Update
    suspend fun update(state: GameState)

    @Query("SELECT * FROM game_state WHERE key = :key")
    fun getState(key: String): Flow<GameState?>

    @Query("SELECT * FROM game_state WHERE key = :key")
    suspend fun getStateSync(key: String): GameState?
}