package com.example.data

import kotlinx.coroutines.flow.Flow

class VerseRepository(private val dao: FavoriteVerseDao) {
    val favorites: Flow<List<FavoriteVerse>> = dao.getAllFavorites()

    suspend fun addFavorite(verse: FavoriteVerse) {
        dao.insertFavorite(verse)
    }

    suspend fun removeFavorite(verse: FavoriteVerse) {
        dao.deleteFavorite(verse)
    }

    suspend fun removeFavoriteByLocation(surahNum: Int, verseNum: Int) {
        dao.deleteFavoriteByLocation(surahNum, verseNum)
    }

    fun isFavorite(surahNum: Int, verseNum: Int): Flow<Boolean> {
        return dao.isFavorite(surahNum, verseNum)
    }
}
