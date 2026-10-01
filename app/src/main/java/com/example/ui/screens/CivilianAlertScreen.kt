package com.example.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CivilianAlertLevel
import com.example.ui.EmergencyViewModel
import com.example.ui.components.RealTimeTrafficEtaNotification
import com.example.ui.theme.BeaconBlue
import com.example.ui.theme.DarkHighwayBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.TrafficAmber
import com.example.ui.theme.TrafficGreen
import com.example.ui.theme.TrafficGreenBright

@Composable
fun CivilianAlertScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val civilianAlert by viewModel.civilianAlert.collectAsState()
    val totalPoints by viewModel.totalCivicPoints.collectAsState()
    val isSirenMuted by viewModel.isSirenMuted.collectAsState()
    val targetHospital by viewModel.selectedHospital.collectAsState()
    val ambulanceProgress by viewModel.ambulanceProgress.collectAsState()
    val speedKmh by viewModel.ambulanceSpeed.collectAsState()
    val signals by viewModel.signals.collectAsState()

    val isCritical = civilianAlert.alertLevel == CivilianAlertLevel.IMMEDIATE_ACTION
    val isPreparation = civilianAlert.alertLevel == CivilianAlertLevel.PREPARATION

    val infiniteTransition = rememberInfiniteTransition(label = "civilian_radar")

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    val alertGlowColor by infiniteTransition.animateColor(
        initialValue = TrafficGreen,
        targetValue = if (isCritical) EmergencyRedBright else if (isPreparation) TrafficAmber else TrafficGreen,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_glow"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkHighwayBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Live Traffic & ETA Notification Component
        item {
            RealTimeTrafficEtaNotification(
                targetHospital = targetHospital,
                progress = ambulanceProgress,
                speedKmh = speedKmh,
                nextSignal = signals.firstOrNull { it.distanceFromAmbulanceMeters > 0f }
            )
        }

        // 1. Alert Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (civilianAlert.corridorCleared) TrafficGreen else alertGlowColor,
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (civilianAlert.corridorCleared) Color(0xFF0F291E) else Color(0xFF261214)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (civilianAlert.corridorCleared) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "એલર્ટ",
                        tint = if (civilianAlert.corridorCleared) TrafficGreen else EmergencyRedBright,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (civilianAlert.corridorCleared)
                            "✅ રસ્તો સફળતાપૂર્વક ખાલી કરાયો!"
                        else
                            "🚨 ૧૦૮ એમ્બ્યુલન્સ એપ્રોચ એલર્ટ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = civilianAlert.instructionGujarati,
                        fontSize = 14.sp,
                        color = Color(0xFFE2E8F0),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. Proximity Radar & Lane Clearing Visualizer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val centerX = width / 2
                        val centerY = height / 2

                        // Radar circles
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = 110f,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = 70f,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = 35f,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 1.5f)
                        )

                        // Radar Pulse Wave (Ambulance siren radar)
                        if (!civilianAlert.corridorCleared) {
                            drawCircle(
                                color = alertGlowColor.copy(alpha = (1f - (pulseRadius / 90f)).coerceIn(0f, 0.5f)),
                                radius = pulseRadius,
                                center = Offset(centerX, centerY + 60f),
                                style = Stroke(width = 3f)
                            )
                        }

                        // Commuter Car (In Center, shifting left)
                        val commuterShiftX = if (civilianAlert.corridorCleared) -50f else 0f
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(centerX - 16f + commuterShiftX, centerY - 25f),
                            size = Size(32f, 50f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )

                        // Commuter Car Label
                        // Approaching Ambulance (Coming from Behind)
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(centerX - 16f, centerY + 50f),
                            size = Size(32f, 55f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Ambulance Red Cross
                        drawRect(
                            color = EmergencyRed,
                            topLeft = Offset(centerX - 3f, centerY + 65f),
                            size = Size(6f, 20f)
                        )
                        drawRect(
                            color = EmergencyRed,
                            topLeft = Offset(centerX - 10f, centerY + 72f),
                            size = Size(20f, 6f)
                        )

                        // Directional Guidance Arrow (Shift Left!)
                        if (!civilianAlert.corridorCleared) {
                            drawLine(
                                color = TrafficGreen,
                                start = Offset(centerX - 20f, centerY),
                                end = Offset(centerX - 70f, centerY),
                                strokeWidth = 5f
                            )
                        }
                    }

                    // Radar HUD Overlays
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "પાછળનું અંતર:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${civilianAlert.ambulanceDistanceMeters.toInt()} મીટર",
                            color = if (isCritical) EmergencyRedBright else TrafficAmber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "એમ્બ્યુલન્સ ગતિ:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${civilianAlert.ambulanceSpeedKmh} કિમી/કલાક",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bottom Instruction on Radar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (civilianAlert.corridorCleared)
                                "રસ્તો ક્લિયર છે - આભાર!"
                            else
                                "👈 વાહન ડાબી બાજુ વાળો (Shift Left)",
                            color = if (civilianAlert.corridorCleared) TrafficGreen else TrafficGreenBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Action Button: "I Gave Way"
        item {
            Button(
                onClick = { viewModel.civilianGiveWay() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("give_way_action_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (civilianAlert.corridorCleared) TrafficGreen else EmergencyRed
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (civilianAlert.corridorCleared)
                        "મેં રસ્તો આપ્યો છે (આભાર!)"
                    else
                        "મેં રસ્તો ખાલી કર્યો (I Gave Way)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 4. Good Samaritan Civic Points & Badges Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2A364F), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TrafficAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "બિરુદ",
                                    tint = TrafficAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "સારો નાગરિક બિરુદ (Good Samaritan)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ગોલ્ડન લાઈફ-સેવર રેટિંગ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Text(
                            text = "${totalPoints ?: 150} પોઇન્ટ્સ",
                            color = TrafficGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "૬ વખત",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "રસ્તો આપ્યો",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "૪.૨ મિનિટ",
                                color = TrafficGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "સમય બચાવ્યો",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "૧૦૦%",
                                color = TrafficAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "સુરક્ષા સ્કોર",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Why giving space matters - Gujarati civic guidance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "શા માટે એમ્બ્યુલન્સને રસ્તો આપવો જરૂરી છે?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ગોલ્ડન અવર (Golden Hour) દરમિયાન દરેક સેકન્ડ એક જીવન બચાવી શકે છે. હંમેશા ડાબી બાજુ વાહન ધીમું કરીને મધ્યમ લેન ખાલી રાખો.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
