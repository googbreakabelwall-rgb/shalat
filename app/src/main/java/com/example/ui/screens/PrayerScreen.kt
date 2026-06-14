package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import kotlin.math.floor
import kotlin.math.round
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PrayerQuranViewModel
import com.example.ui.viewmodel.PrayerStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrayerScreen(
    viewModel: PrayerQuranViewModel,
    onRequestLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val hijriDate by viewModel.hijriDate.collectAsState()
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val locationLabel by viewModel.locationLabel.collectAsState()

    val currentPrayerName = remember(prayerTimes) { getActivePrayer(prayerTimes) }
    val timeToNext = remember(prayerTimes) { getNextPrayerCountdown(prayerTimes) }

    // Ramadan Countdown
    // Target Ramadan 1448 AH or similar depending on tabular date (approx Feb 2027 or Feb 18, 2026)
    val daysToRamadan = remember(hijriDate) {
        calculateDaysToRamadan(hijriDate)
    }

    val activePrayerTime = remember(prayerTimes, currentPrayerName) {
        when (currentPrayerName.lowercase()) {
            "fajr" -> prayerTimes.fajr
            "sunrise" -> prayerTimes.sunrise
            "dhuhr" -> prayerTimes.dhuhr
            "asr" -> prayerTimes.asr
            "maghrib" -> prayerTimes.maghrib
            "isha" -> prayerTimes.isha
            else -> "--:--"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(EmeraldDim)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Upcoming Prayer",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentPrayerName,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Emerald)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = activePrayerTime,
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldDim,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = timeToNext,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = locationLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(100.dp)).background(Color.White))
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(100.dp)).background(Color.White.copy(alpha = 0.3f)))
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(100.dp)).background(Color.White.copy(alpha = 0.3f)))
                        }
                    }
                }
            }
        }

        // Location & GPS Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder(true).copy(
                    brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location icon",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GPS Position",
                                style = MaterialTheme.typography.labelSmall,
                                color = Sage
                            )
                            Text(
                                text = locationLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Button(
                        onClick = onRequestLocation,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDim),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("location_detect_button")
                    ) {
                        Text("Detect", color = EmeraldLight, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Hijri Calendar & Ramadan Tracker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(28.dp),
                border = CardDefaults.outlinedCardBorder(true).copy(
                    brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ISLAMIC CALENDAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val hijriMonthName = hijriDate.monthName
                            Text(
                                text = "${hijriDate.day} $hijriMonthName ${hijriDate.year} AH",
                                style = MaterialTheme.typography.titleLarge,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Month of Islamic Calendar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Moon star icon",
                            tint = Gold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Countdown to Ramadan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (daysToRamadan <= 0) "Ramadan Mubarak is active!" else "$daysToRamadan days left until the Holy Month",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (daysToRamadan <= 0) GreenSuccess else EmeraldDim)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (daysToRamadan <= 0) "Holy Month" else "${daysToRamadan}d",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (daysToRamadan <= 0) Color.White else EmeraldLight,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // Today's Prayer Grid
        item {
            Text(
                text = "TODAY'S PRAYER TIMES",
                style = MaterialTheme.typography.labelSmall,
                color = Sage,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            val prayerTimesList = listOf(
                "Fajr" to prayerTimes.fajr,
                "Sunrise" to prayerTimes.sunrise,
                "Dhuhr" to prayerTimes.dhuhr,
                "Asr" to prayerTimes.asr,
                "Maghrib" to prayerTimes.maghrib,
                "Isha" to prayerTimes.isha
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                prayerTimesList.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (name, time) ->
                            val isActive = name.equals(currentPrayerName, true)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prayer_time_cell_$name"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) EmeraldDim else DarkSurface
                                ),
                                shape = RoundedCornerShape(24.dp),
                                border = if (isActive) {
                                    CardDefaults.outlinedCardBorder(true).copy(
                                        brush = Brush.linearGradient(listOf(Emerald, Emerald))
                                    )
                                } else {
                                    CardDefaults.outlinedCardBorder(true).copy(
                                        brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
                                    )
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isActive) GoldLight else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (isActive) EmeraldLight else Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Tracker Logging
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(28.dp),
                border = CardDefaults.outlinedCardBorder(true).copy(
                    brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DAILY PRAYER TRACKER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val trackedPrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

                    trackedPrayers.forEach { prayer ->
                        val currentStatus = viewModel.getPrayerStatus(todayKey, prayer)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prayer,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PrayerStatusButton(
                                    label = "Done",
                                    isActive = currentStatus == PrayerStatus.COMPLETED,
                                    activeColor = GreenSuccess,
                                    onClick = {
                                        viewModel.logPrayerStatus(todayKey, prayer, PrayerStatus.COMPLETED)
                                    }
                                )
                                PrayerStatusButton(
                                    label = "Late",
                                    isActive = currentStatus == PrayerStatus.LATE,
                                    activeColor = OrangeDim,
                                    onClick = {
                                        viewModel.logPrayerStatus(todayKey, prayer, PrayerStatus.LATE)
                                    }
                                )
                                PrayerStatusButton(
                                    label = "Miss",
                                    isActive = currentStatus == PrayerStatus.MISSED,
                                    activeColor = RedDim,
                                    onClick = {
                                        viewModel.logPrayerStatus(todayKey, prayer, PrayerStatus.MISSED)
                                    }
                                )
                            }
                        }
                        if (prayer != trackedPrayers.last()) {
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerStatusButton(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) activeColor else DarkSurfaceSecondary)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Color.White else Sage,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getActivePrayer(times: PrayerTimes): String {
    val curH = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE) / 60.0
    val raw = times.rawTimes
    val keys = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    return keys.find { (raw[it] ?: 0.0) > curH } ?: "Fajr"
}

private fun getNextPrayerCountdown(times: PrayerTimes): String {
    val curH = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE) / 60.0
    val active = getActivePrayer(times)
    val targetTime = times.rawTimes[active] ?: 0.0
    var diff = targetTime - curH
    if (diff < 0) diff += 24.0
    val hh = floor(diff).toInt()
    val mm = round((diff - hh) * 60.0).toInt()
    return if (hh > 0) "in $hh hours $mm mins" else "in $mm mins"
}

private fun calculateDaysToRamadan(hijriDate: HijriDate): Int {
    // Ramadan is Month 9. Current is Month hijriDate.month
    val targetMonth = 9
    val targetYear = if (hijriDate.month > targetMonth) hijriDate.year + 1 else hijriDate.year
    val monthsDiff = (targetMonth - hijriDate.month + 12) % 12
    
    // Simple fast tabular representation logic (29.5 days per month)
    var totalDays = monthsDiff * 29
    totalDays -= hijriDate.day
    return if (totalDays < 0) 0 else totalDays
}
