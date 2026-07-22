package com.findwords.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.findwords.app.model.GameState
import kotlinx.coroutines.flow.Flow

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