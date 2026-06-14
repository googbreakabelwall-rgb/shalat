package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PrayerQuranViewModel

@Composable
fun QuranScreen(
    viewModel: PrayerQuranViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val rawFavorites by viewModel.favoriteVerses.collectAsState()

    var showFavoritesTab by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (selectedSurah == null) {
            // Header Tabs: Surahs vs Favorites
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(DarkSurface, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    label = "All Surahs",
                    isActive = !showFavoritesTab,
                    onClick = { showFavoritesTab = false },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    label = "Saved Favorites (${rawFavorites.size})",
                    isActive = showFavoritesTab,
                    onClick = { showFavoritesTab = true },
                    modifier = Modifier.weight(1f)
                )
            }

            if (showFavoritesTab) {
                FavoritesListSection(viewModel = viewModel)
            } else {
                SurahIndexSection(viewModel = viewModel)
            }
        } else {
            SurahReaderSection(viewModel = viewModel)
        }
    }
}

@Composable
fun TabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Emerald else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) Color.White else TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahIndexSection(viewModel: PrayerQuranViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredSurahs by viewModel.filteredSurahs.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("surah_search_input"),
            placeholder = { Text("Search Surah e.g. Al-Fatihah", color = Sage) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldLight) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Emerald,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredSurahs) { surah ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectSurah(surah) }
                        .testTag("surah_item_${surah.number}"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder(true).copy(
                        brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Circular Number indicator
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(DarkSurfaceSecondary, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = surah.englishName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${surah.revelationType} • ${surah.numberOfVerses} Verses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Arabic text display
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = surah.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = surah.englishNameTranslation,
                                style = MaterialTheme.typography.labelSmall,
                                color = Sage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesListSection(viewModel: PrayerQuranViewModel) {
    val favorites by viewModel.favoriteVerses.collectAsState()

    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "No favorites",
                    tint = Sage,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No saved verses yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap the heart icon when reading a Surah to instantly save it offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favorites) { favorite ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder(true).copy(
                        brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Saved icon",
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${favorite.surahName} [${favorite.surahNumber}:${favorite.verseNumber}]",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.removeFavoriteRecord(favorite) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete from favorites",
                                    tint = RedDim
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = favorite.arabicText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = favorite.englishText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurahReaderSection(viewModel: PrayerQuranViewModel) {
    val surah by viewModel.selectedSurah.collectAsState()
    val verses by viewModel.versesList.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playingIndex by viewModel.playingVerseIndex.collectAsState()
    val favorites by viewModel.favoriteVerses.collectAsState()
    val downloaded by viewModel.offlineDownloadedSurahs.collectAsState()

    val context = LocalContext.current

    val currentSurah = surah ?: return

    val isDownloaded = downloaded.contains(currentSurah.number)

    Column(modifier = Modifier.fillMaxSize()) {
        // Back toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.clearSelectedSurah() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = currentSurah.englishName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "${currentSurah.revelationType} • ${currentSurah.numberOfVerses} Verses",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage
                    )
                }
            }

            // Offline access download toggle
            IconButton(
                onClick = { viewModel.downloadSurahForOffline(currentSurah.number) }
            ) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = "Download for offline access",
                    tint = if (isDownloaded) GreenSuccess else TextSecondary
                )
            }
        }

        // Bismillah Header for all surahs except Tawbah (number 9)
        if (currentSurah.number != 9) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // verses rendering
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(verses) { index, verse ->
                val isCurrentPlaying = playingIndex == index
                val isFavorite = favorites.any { it.surahNumber == verse.surahNumber && it.verseNumber == verse.verseNumber }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("verse_item_$index"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentPlaying) EmeraldDim else DarkSurface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = if (isCurrentPlaying) {
                        CardDefaults.outlinedCardBorder(true).copy(
                            brush = Brush.linearGradient(listOf(Emerald, Emerald))
                        )
                    } else {
                        CardDefaults.outlinedCardBorder(true).copy(
                            brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
                        )
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(BorderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = verse.verseNumber.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row {
                                // Favorite Button
                                IconButton(onClick = { viewModel.toggleFavorite(verse) }) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Save favorite",
                                        tint = if (isFavorite) RedDim else TextSecondary
                                    )
                                }

                                // Play Recitation Button
                                IconButton(onClick = {
                                    if (isCurrentPlaying && isPlaying) {
                                        viewModel.togglePlayPause()
                                    } else {
                                        viewModel.playRecitation(verse.surahNumber, verse.verseNumber, index)
                                    }
                                }) {
                                    Text(
                                        text = if (isCurrentPlaying && isPlaying) "‖ " else "▶ ",
                                        color = if (isCurrentPlaying) GoldLight else EmeraldLight,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        // Arabic text
                        Text(
                            text = verse.arabic,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            textAlign = TextAlign.Right,
                            lineHeight = 44.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        // Transliteration
                        Text(
                            text = verse.transliteration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldLight,
                            fontWeight = FontWeight.Light
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        // Translation text
                        Text(
                            text = verse.translation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Floating media controller bar if active
        if (playingIndex != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = EmeraldDim,
                tonalElevation = 8.dp,
                border = CardDefaults.outlinedCardBorder(true).copy(
                    brush = Brush.linearGradient(listOf(EmeraldLight, Gold))
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Music indicator",
                            tint = Gold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val playingVerse = verses.getOrNull(playingIndex!!)
                            Text(
                                text = "Reciting ${currentSurah.englishName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Verse ${playingVerse?.verseNumber ?: ""} • Alafasy Reciter",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { viewModel.togglePlayPause() }) {
                            Text(
                                text = if (isPlaying) "‖ " else "▶ ",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.stopAudio() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop recitation stream",
                                tint = RedDim
                            )
                        }
                    }
                }
            }
        }
    }
}
