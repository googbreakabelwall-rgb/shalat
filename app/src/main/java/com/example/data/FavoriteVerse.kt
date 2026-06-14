package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_verses")
data class FavoriteVerse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val arabicText: String,
    val englishText: String,
    val timestamp: Long = System.currentTimeMillis()
)
