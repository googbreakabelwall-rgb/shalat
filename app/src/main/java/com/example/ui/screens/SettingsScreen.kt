package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AsrJuristic
import com.example.data.CalculationMethod
import com.example.ui.theme.*
import com.example.ui.viewmodel.PrayerQuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PrayerQuranViewModel,
    modifier: Modifier = Modifier
) {
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val method by viewModel.calcMethod.collectAsState()
    val asrJuristic by viewModel.asrJuristic.collectAsState()

    var latInput by remember { mutableStateOf(lat.toString()) }
    var lngInput by remember { mutableStateOf(lng.toString()) }

    LaunchedEffect(lat) {
        latInput = lat.toString()
    }
    LaunchedEffect(lng) {
        lngInput = lng.toString()
    }

    var isMethodExpanded by remember { mutableStateOf(false) }
    var isAsrExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "ALERTS & CALCULATION CONFIG",
                style = MaterialTheme.typography.labelSmall,
                color = Sage,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // GPS & Grid adjustments
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
                        text = "MANUAL COORDINATES OVERRIDE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = latInput,
                        onValueChange = {
                            latInput = it
                            it.toDoubleOrNull()?.let { d -> viewModel.setLocation(d, lng, "Custom latitude") }
                        },
                        label = { Text("Latitude", color = Sage) },
                        modifier = Modifier.fillMaxWidth().testTag("lat_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Emerald,
                            unfocusedBorderColor = BorderColor
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = lngInput,
                        onValueChange = {
                            lngInput = it
                            it.toDoubleOrNull()?.let { d -> viewModel.setLocation(lat, d, "Custom longitude") }
                        },
                        label = { Text("Longitude", color = Sage) },
                        modifier = Modifier.fillMaxWidth().testTag("lng_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Emerald,
                            unfocusedBorderColor = BorderColor
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "*Mandatory overrides for extreme remote desert regions devoid of live cellular networks (calculates without connection).",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage
                    )
                }
            }
        }

        // Calculation methods
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
                        text = "ASTRONOMICAL CALCULATION RULES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Method selector drop down
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = isMethodExpanded,
                            onExpandedChange = { isMethodExpanded = !isMethodExpanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = when (method) {
                                    CalculationMethod.MWL -> "Muslim World League (Fajr: 18° / Isha: 17°)"
                                    CalculationMethod.ISNA -> "ISNA North America (Fajr: 15° / Isha: 15°)"
                                    CalculationMethod.EGYPT -> "Egyptian General Authority"
                                    CalculationMethod.KARACHI -> "University of Islamic Sciences (Karachi)"
                                    CalculationMethod.TEHRAN -> "Tehran Institute (Geophysics)"
                                },
                                onValueChange = {},
                                label = { Text("Convention Angle Setting", color = Sage) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMethodExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Emerald,
                                    unfocusedBorderColor = BorderColor
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("calc_method_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = isMethodExpanded,
                                onDismissRequest = { isMethodExpanded = false },
                                modifier = Modifier.background(DarkSurfaceSecondary)
                            ) {
                                CalculationMethod.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option.name, color = Color.White) },
                                        onClick = {
                                            viewModel.setCalculationMethod(option)
                                            isMethodExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Asr methods
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = isAsrExpanded,
                            onExpandedChange = { isAsrExpanded = !isAsrExpanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = when (asrJuristic) {
                                    AsrJuristic.STANDARD -> "Standard Shadow Factor 1 (Shafi, Maliki, Hanbali)"
                                    AsrJuristic.HANAFI -> "Hanafi Shadow Factor 2"
                                },
                                onValueChange = {},
                                label = { Text("Asr Juristic Computation Rule", color = Sage) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAsrExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Emerald,
                                    unfocusedBorderColor = BorderColor
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("asr_method_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = isAsrExpanded,
                                onDismissRequest = { isAsrExpanded = false },
                                modifier = Modifier.background(DarkSurfaceSecondary)
                            ) {
                                AsrJuristic.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option.name, color = Color.White) },
                                        onClick = {
                                            viewModel.setAsrJuristic(option)
                                            isAsrExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // About section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceSecondary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info",
                        tint = Gold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Noor Qibla & Quran Companion",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Version 1.0 • Built complete lightweight offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = Sage
                        )
                    }
                }
            }
        }
    }
}
