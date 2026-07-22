package com.findwords.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.findwords.app.model.Level
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<Level>)

    @Query("SELECT * FROM level WHERE packId = :packId ORDER BY levelNumber")
    fun getLevelsByPack(packId: Int): Flow<List<Level>>

    @Query("SELECT * FROM level WHERE packId = :packId AND levelNumber = :levelNumber")
    suspend fun getLevel(packId: Int, levelNumber: Int): Level?

    @Query("SELECT * FROM level WHERE id = :id")
    suspend fun getLevelById(id: Int): Level?

    @Query("SELECT COUNT(*) FROM level WHERE packId = :packId AND isCompleted = 1")
    suspend fun getCompletedCount(packId: Int): Int

    @Query("UPDATE level SET isCompleted = 1, completedAt = :timestamp WHERE id = :id")
    suspend fun markCompleted(id: Int, timestamp: Long)

    @Query("SELECT * FROM level WHERE isCompleted = 0 ORDER BY packId, levelNumber LIMIT 1")
    suspend fun getNextUncompleted(): Level?

    @Query("SELECT MAX(levelNumber) FROM level WHERE packId = :packId")
    suspend fun getMaxLevel(packId: Int): Int?
}