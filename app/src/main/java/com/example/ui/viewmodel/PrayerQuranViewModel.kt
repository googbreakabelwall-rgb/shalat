package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.*

class PrayerQuranViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = VerseRepository(db.favoriteVerseDao)

    // Location States
    private val _latitude = MutableStateFlow(-6.2088) // default Jakarta
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(106.8456)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _locationLabel = MutableStateFlow("Jakarta, Indonesia")
    val locationLabel: StateFlow<String> = _locationLabel.asStateFlow()

    // Calculation settings
    private val _calcMethod = MutableStateFlow(CalculationMethod.MWL)
    val calcMethod: StateFlow<CalculationMethod> = _calcMethod.asStateFlow()

    private val _asrJuristic = MutableStateFlow(AsrJuristic.STANDARD)
    val asrJuristic: StateFlow<AsrJuristic> = _asrJuristic.asStateFlow()

    // Calculated Prayer Times & Date
    val prayerTimes: StateFlow<PrayerTimes> = combine(
        _latitude, _longitude, _calcMethod, _asrJuristic
    ) { lat, lng, method, juristic ->
        PrayerTimeCalculator.calculateTimes(lat, lng, Date(), method, juristic)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerTimeCalculator.calculateTimes(-6.2088, 106.8456))

    val hijriDate: StateFlow<HijriDate> = _latitude.map {
        PrayerTimeCalculator.gregorianToHijri(Date())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerTimeCalculator.gregorianToHijri(Date()))

    // Compasses & Sensor azimuth (updated from Activity/Sensor)
    private val _compassAzimuth = MutableStateFlow(0f)
    val compassAzimuth: StateFlow<Float> = _compassAzimuth.asStateFlow()

    // Qibla Direction relative to absolute North
    val qiblaBearing: StateFlow<Double> = combine(_latitude, _longitude) { lat, lng ->
        calculateQibla(lat, lng)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 295.0)

    // Database favorite verses
    val favoriteVerses: StateFlow<List<FavoriteVerse>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation tabs
    private val _selectedTab = MutableStateFlow("dashboard")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Quran State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSurahs: StateFlow<List<Surah>> = _searchQuery.map { query ->
        if (query.isBlank()) {
            QuranData.surahList
        } else {
            QuranData.surahList.filter {
                it.englishName.contains(query, ignoreCase = true) ||
                it.englishNameTranslation.contains(query, ignoreCase = true) ||
                it.name.contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuranData.surahList)

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _versesList = MutableStateFlow<List<Verse>>(emptyList())
    val versesList: StateFlow<List<Verse>> = _versesList.asStateFlow()

    // Media Recitation States
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playingVerseIndex = MutableStateFlow<Int?>(null)
    val playingVerseIndex: StateFlow<Int?> = _playingVerseIndex.asStateFlow()

    private val _offlineDownloadedSurahs = MutableStateFlow<Set<Int>>(setOf(1, 112, 113, 114)) // Precached offline
    val offlineDownloadedSurahs: StateFlow<Set<Int>> = _offlineDownloadedSurahs.asStateFlow()

    // Prayer Alert/Reminders log
    private val _loggedPrayers = MutableStateFlow<Map<String, PrayerStatus>>(emptyMap()) // DateKey -> Map<PrayerKey, Status>
    val loggedPrayers: StateFlow<Map<String, PrayerStatus>> = _loggedPrayers.asStateFlow()

    init {
        // Log defaults
        _versesList.value = QuranData.getVersesForSurah(1)
        
        // Initialize simple prayer logging for last 7 days
        val initialLogs = mutableMapOf<String, PrayerStatus>()
        _loggedPrayers.value = initialLogs
    }

    // Tab switcher
    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    // Set location manual or GPS
    fun setLocation(lat: Double, lng: Double, label: String) {
        _latitude.value = lat
        _longitude.value = lng
        _locationLabel.value = label
    }

    // Set calculation properties
    fun setCalculationMethod(method: CalculationMethod) {
        _calcMethod.value = method
    }

    fun setAsrJuristic(juristic: AsrJuristic) {
        _asrJuristic.value = juristic
    }

    // Update sensor azimuth
    fun setCompassAzimuth(azimuth: Float) {
        _compassAzimuth.value = azimuth
    }

    // Favorites Room Actions
    fun toggleFavorite(verse: Verse) {
        viewModelScope.launch {
            val isFav = favoriteVerses.value.any { it.surahNumber == verse.surahNumber && it.verseNumber == verse.verseNumber }
            if (isFav) {
                repository.removeFavoriteByLocation(verse.surahNumber, verse.verseNumber)
            } else {
                val surahName = QuranData.surahList.firstOrNull { it.number == verse.surahNumber }?.englishName ?: "Unknown"
                repository.addFavorite(
                    FavoriteVerse(
                        surahNumber = verse.surahNumber,
                        surahName = surahName,
                        verseNumber = verse.verseNumber,
                        arabicText = verse.arabic,
                        englishText = verse.translation
                    )
                )
            }
        }
    }

    fun removeFavoriteRecord(fav: FavoriteVerse) {
        viewModelScope.launch {
            repository.removeFavorite(fav)
        }
    }

    // Quran Navigator
    fun selectSurah(surah: Surah) {
        stopAudio()
        _selectedSurah.value = surah
        _versesList.value = QuranData.getVersesForSurah(surah.number)
    }

    fun clearSelectedSurah() {
        stopAudio()
        _selectedSurah.value = null
        _versesList.value = emptyList()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Qibla Direction bearing calculation relative to True North
    private fun calculateQibla(userLat: Double, userLng: Double): Double {
        fun d2r(d: Double): Double = d * Math.PI / 180.0
        fun r2d(r: Double): Double = r * 180.0 / Math.PI

        val kaabaLat = d2r(21.4225)
        val kaabaLng = d2r(39.8262)
        val phi = d2r(userLat)
        val lambda = d2r(userLng)

        val deltaLambda = kaabaLng - lambda
        val y = sin(deltaLambda)
        val x = cos(phi) * tan(kaabaLat) - sin(phi) * cos(deltaLambda)
        
        val bearing = r2d(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    // Media Playback Controller
    fun playRecitation(surahNumber: Int, verseNumber: Int, index: Int) {
        try {
            stopAudio()
            _playingVerseIndex.value = index
            val url = QuranData.getAudioUrl(surahNumber, verseNumber)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    playNextVerse()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("PrayerQuranViewModel", "MediaPlayer Error: $what, $extra. Falling back to next verse.")
                    _isPlaying.value = false
                    playNextVerse()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("PrayerQuranViewModel", "Error playing recitation: ${e.message}")
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.start()
            _isPlaying.value = true
        }
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _playingVerseIndex.value = null
    }

    private fun playNextVerse() {
        val currentIdx = _playingVerseIndex.value ?: return
        val list = _versesList.value
        if (currentIdx + 1 < list.size) {
            val nextVerse = list[currentIdx + 1]
            playRecitation(nextVerse.surahNumber, nextVerse.verseNumber, currentIdx + 1)
        } else {
            stopAudio()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }

    fun downloadSurahForOffline(surahNumber: Int) {
        // Simulate downloading to cache for remote areas
        viewModelScope.launch {
            _offlineDownloadedSurahs.value = _offlineDownloadedSurahs.value + surahNumber
        }
    }

    // Habit fast tracking log
    fun logPrayerStatus(dateKey: String, prayerName: String, status: PrayerStatus) {
        val current = _loggedPrayers.value.toMutableMap()
        val key = "${dateKey}_${prayerName}"
        current[key] = status
        _loggedPrayers.value = current
    }

    fun getPrayerStatus(dateKey: String, prayerName: String): PrayerStatus {
        val key = "${dateKey}_${prayerName}"
        return _loggedPrayers.value[key] ?: PrayerStatus.UNTRACKED
    }
}

enum class PrayerStatus {
    COMPLETED,
    LATE,
    MISSED,
    UNTRACKED
}
