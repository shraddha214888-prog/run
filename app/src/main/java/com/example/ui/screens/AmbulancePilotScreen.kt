package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StepIconType
import com.example.ui.EmergencyViewModel
import com.example.ui.components.InteractiveCorridorMap
import com.example.ui.components.RealTimeTrafficEtaNotification
import com.example.ui.components.SirenBeaconHeader
import com.example.ui.components.TrafficSignalCard
import com.example.ui.theme.DarkHighwayBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.TrafficAmber
import com.example.ui.theme.TrafficGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbulancePilotScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val isMissionActive by viewModel.isMissionActive.collectAsState()
    val ambulanceProgress by viewModel.ambulanceProgress.collectAsState()
    val speedKmh by viewModel.ambulanceSpeed.collectAsState()
    val isSirenMuted by viewModel.isSirenMuted.collectAsState()
    val selectedCase by viewModel.selectedCase.collectAsState()
    val selectedHospital by viewModel.selectedHospital.collectAsState()
    val signals by viewModel.signals.collectAsState()
    val isGpsActive by viewModel.gpsManager.isGpsActive.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkHighwayBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Siren Beacon Header
        item {
            SirenBeaconHeader(
                isEmergencyActive = isMissionActive,
                isSirenMuted = isSirenMuted,
                onToggleSiren = { viewModel.toggleSiren() }
            )
        }

        // 2. Interactive Map of Corridor & Signals
        item {
            InteractiveCorridorMap(
                ambulanceProgress = ambulanceProgress,
                targetHospital = selectedHospital,
                signals = signals,
                speedKmh = speedKmh,
                isSirenActive = !isSirenMuted
            )
        }

        // 3. Real-Time Traffic Status & ETA Notification Component (Gujarati)
        item {
            RealTimeTrafficEtaNotification(
                targetHospital = selectedHospital,
                progress = ambulanceProgress,
                speedKmh = speedKmh,
                nextSignal = signals.firstOrNull { it.distanceFromAmbulanceMeters > 0f }
            )
        }

        // 3. Telemetry HUD Cards: Speed, Hospital ETA, Distance, GPS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Speed Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "ગતિ",
                            tint = TrafficGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$speedKmh",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "કિમી/કલાક",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // ETA Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "અંદાજિત સમય",
                            tint = EmergencyRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedHospital.estimatedMinutes} મિનિટ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "બાકી સમય (ETA)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Distance Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "અંતર",
                            tint = TrafficAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedHospital.distanceKm}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "કિમી અંતર",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // 4. Target Hospital Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmergencyRedBright.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmergencyRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = "હોસ્પિટલ",
                                    tint = EmergencyRedBright,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "લક્ષ્ય હોસ્પિટલ (ઈમરજન્સી પ્રવેશ)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = selectedHospital.nameGujarati,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ICU બેડ ઉપલબ્ધ: ${selectedHospital.availableIcuBeds} / ${selectedHospital.totalIcuBeds}",
                            color = TrafficGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedHospital.traumaLevel,
                            color = TrafficAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 5. Emergency Case Selection Row
        item {
            Column {
                Text(
                    text = "કટોકટીનો પ્રકાર પસંદ કરો (Emergency Case):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.emergencyCases) { c ->
                        val isSelected = c.id == selectedCase.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCase(c) },
                            label = {
                                Text(
                                    text = c.titleGujarati,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmergencyRed,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceCard,
                                labelColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("case_chip_${c.id}")
                        )
                    }
                }
            }
        }

        // 6. Navigation Turn Directions
        item {
            Column {
                Text(
                    text = "જીપીએસ નેવિગેશન નિર્દેશો (Turn-by-Turn GPS):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                viewModel.navigationSteps.forEach { step ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (step.iconType) {
                                        StepIconType.STRAIGHT -> Icons.Default.Navigation
                                        StepIconType.TURN_LEFT -> Icons.Default.Navigation
                                        StepIconType.CROSS_JUNCTION -> Icons.Default.Traffic
                                        StepIconType.HOSPITAL_ARRIVAL -> Icons.Default.LocalHospital
                                        else -> Icons.Default.Navigation
                                    },
                                    contentDescription = null,
                                    tint = TrafficGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.instructionGujarati,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = step.instructionEnglish,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "${step.distanceMeters}m",
                                color = TrafficAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 7. Approaching Smart Traffic Signals
        item {
            Column {
                Text(
                    text = "કોરિડોર સ્માર્ટ સિગ્નલ કંટ્રોલ (Green Wave Status):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                signals.take(2).forEach { signal ->
                    TrafficSignalCard(
                        signal = signal,
                        onManualForceGreen = { viewModel.forceGreenSignal(it) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // 8. Mission Completion Action
        item {
            Button(
                onClick = { viewModel.completeMission() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("complete_mission_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TrafficGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "દર્દી હોસ્પિટલ પહોંચ્યા - મિશન પૂર્ણ કરો",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
