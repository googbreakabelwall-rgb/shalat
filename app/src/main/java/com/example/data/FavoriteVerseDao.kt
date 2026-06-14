package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteVerseDao {
    @Query("SELECT * FROM favorite_verses ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteVerse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(verse: FavoriteVerse)

    @Delete
    suspend fun deleteFavorite(verse: FavoriteVerse)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_verses WHERE surahNumber = :surahNum AND verseNumber = :verseNum)")
    fun isFavorite(surahNum: Int, verseNum: Int): Flow<Boolean>

    @Query("DELETE FROM favorite_verses WHERE surahNumber = :surahNum AND verseNumber = :verseNum")
    suspend fun deleteFavoriteByLocation(surahNum: Int, verseNum: Int)
}
