package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PrayerQuranViewModel
import kotlin.math.abs

@Composable
fun QiblaScreen(
    viewModel: PrayerQuranViewModel,
    modifier: Modifier = Modifier
) {
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val locationLabel by viewModel.locationLabel.collectAsState()

    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    val compassAzimuth by viewModel.compassAzimuth.collectAsState()

    // Slider simulation override block
    var isSimulatingMode by remember { mutableStateOf(false) }
    var simulatedAzimuth by remember { mutableStateOf(0f) }

    val currentAzimuth = if (isSimulatingMode) simulatedAzimuth else compassAzimuth

    // Keep a continuous version of the azimuth to avoid 360-degree wrapping spin
    var continuousAzimuth by remember { mutableStateOf(currentAzimuth.toFloat()) }
    LaunchedEffect(currentAzimuth) {
        val target = currentAzimuth.toFloat()
        val currentMod = (continuousAzimuth % 360f + 360f) % 360f
        var diff = target - currentMod
        if (diff > 180f) {
            diff -= 360f
        } else if (diff < -180f) {
            diff += 360f
        }
        continuousAzimuth += diff
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = continuousAzimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "compass_azimuth"
    )

    val angleDifference = remember(animatedAzimuth, qiblaBearing) {
        val diff = (qiblaBearing - animatedAzimuth + 360.0) % 360.0
        if (diff > 180) diff - 360.0 else diff
    }

    val isAligned = abs(angleDifference) <= 4.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tracker stats card
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
                    text = "QIBLA COMPASS CALCULATOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Sage,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = String.format("%.2f° N", qiblaBearing),
                            style = MaterialTheme.typography.headlineLarge,
                            color = GoldLight,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Kaaba Bearing",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format("%.1f°", currentAzimuth),
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (isAligned) EmeraldLight else Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Phone direction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location pointer",
                        tint = EmeraldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calculating from: $locationLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Animated Alignment status banner
        AnimatedVisibility(
            visible = isAligned,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GreenSuccess.copy(alpha = 0.2f))
                    .border(1.dp, GreenSuccess, RoundedCornerShape(24.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Aligned",
                        tint = GreenSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perfectly Aligned! You are facing Kaaba",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Qibla Compass Dial Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Draw interactive Canvas compass
            Canvas(
                modifier = Modifier
                    .size(260.dp)
                    .testTag("qibla_compass_canvas")
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2

                // 1. Draw outer circle border
                drawCircle(
                    color = BorderColor,
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )                // 2. Draw compass ticks and cardinal points (rotated so -currentAzimuth is at top)
                rotate(degrees = -animatedAzimuth, pivot = center) {
                    // Cardinal markers
                    // North
                    drawContext.canvas.nativeCanvas.drawText(
                        "N",
                        center.x,
                        center.y - radius + 24.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.RED
                            textSize = 20.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    // South
                    drawContext.canvas.nativeCanvas.drawText(
                        "S",
                        center.x,
                        center.y + radius - 12.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 18.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    // East
                    drawContext.canvas.nativeCanvas.drawText(
                        "E",
                        center.x + radius - 18.dp.toPx(),
                        center.y + 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 18.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    // West
                    drawContext.canvas.nativeCanvas.drawText(
                        "W",
                        center.x - radius + 18.dp.toPx(),
                        center.y + 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 18.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
 
                    // Draw circular dial teeth / ticks at 30-degree partitions
                    for (angle in 0 until 360 step 15) {
                        if (angle % 90 != 0) {
                            val tickStart = Offset(
                                center.x + (radius - 12.dp.toPx()) * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                                center.y + (radius - 12.dp.toPx()) * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                            )
                            val tickEnd = Offset(
                                center.x + (radius - 4.dp.toPx()) * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                                center.y + (radius - 4.dp.toPx()) * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                            )
                            drawLine(
                                color = Sage.copy(alpha = 0.5f),
                                start = tickStart,
                                end = tickEnd,
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
 
                // 3. Draw Mecca Needle indicating the computed Bearing relative to absolute North
                // True Needle direction on phone is (qiblaBearing - currentAzimuth)
                val targetQiblaAngle = qiblaBearing.toFloat() - animatedAzimuth
                rotate(degrees = targetQiblaAngle, pivot = center) {
                    // Needle path (Golden arrow point)
                    val needlePathTop = Path().apply {
                        moveTo(center.x, center.y - radius + 32.dp.toPx())
                        lineTo(center.x - 14.dp.toPx(), center.y)
                        lineTo(center.x + 14.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = needlePathTop,
                        brush = Brush.verticalGradient(listOf(GoldLight, Gold))
                    )

                    val needlePathBack = Path().apply {
                        moveTo(center.x, center.y + radius - 32.dp.toPx())
                        lineTo(center.x - 10.dp.toPx(), center.y)
                        lineTo(center.x + 10.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = needlePathBack,
                        color = Sage
                    )
                }

                // 4. Center hub (Kaaba representative icon)
                drawCircle(
                    color = DarkBg,
                    radius = 24.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Gold,
                    radius = 24.dp.toPx(),
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Test simulator panel in remote or hardware absent zones
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceSecondary),
            shape = RoundedCornerShape(24.dp),
            border = CardDefaults.outlinedCardBorder(true).copy(
                brush = Brush.linearGradient(listOf(BorderColor, BorderColor))
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sensor help",
                            tint = Sage,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hardware Sensor Missing?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isSimulatingMode,
                        onCheckedChange = { isSimulatingMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Gold,
                            checkedTrackColor = Emerald
                        )
                    )
                }

                if (isSimulatingMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Simulate phone rotation slider:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sage
                    )
                    Slider(
                        value = simulatedAzimuth,
                        onValueChange = { simulatedAzimuth = it },
                        valueRange = 0f..359f,
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Emerald
                        )
                    )
                    Text(
                        text = "Slide to simulate rotating phone horizontally",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
