package com.example.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignalState
import com.example.data.model.TrafficSignalJunction
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.TrafficAmber
import com.example.ui.theme.TrafficGreen
import com.example.ui.theme.TrafficGreenBright

@Composable
fun TrafficSignalCard(
    signal: TrafficSignalJunction,
    onManualForceGreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreempted = signal.preemptionActive || signal.currentState == SignalState.PREEMPTED_GREEN
    val infiniteTransition = rememberInfiniteTransition(label = "signal_glow")

    val greenGlow by infiniteTransition.animateColor(
        initialValue = TrafficGreen,
        targetValue = if (isPreempted) TrafficGreenBright else TrafficGreen,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "green_glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPreempted) TrafficGreen.copy(alpha = 0.6f) else Color(0xFF2A364F),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Traffic,
                        contentDescription = "ટ્રાફિક સિગ્નલ",
                        tint = if (isPreempted) TrafficGreen else TrafficAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = signal.nameGujarati,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = signal.nameEnglish,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                // Traffic Light Visual Indicator
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Red Light
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (signal.currentState == SignalState.RED) EmergencyRedBright
                                else Color(0xFF451A1A)
                            )
                    )
                    // Yellow Light
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (signal.currentState == SignalState.YELLOW) TrafficAmber
                                else Color(0xFF45351A)
                            )
                    )
                    // Green Light
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPreempted || signal.currentState == SignalState.GREEN) greenGlow
                                else Color(0xFF133827)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preemption Status & Distance Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isPreempted) "ગ્રીન વેવ પ્રિ-એમ્પશન સક્રિય" else "સામાન્ય સાયકલ ચાલુ",
                        color = if (isPreempted) TrafficGreen else Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "એમ્બ્યુલન્સ અંતર: ${signal.distanceFromAmbulanceMeters.toInt()} મીટર",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                if (isPreempted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "ક્લિયર",
                            tint = TrafficGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "સિગ્નલ લીલું (${signal.secondsRemaining}s)",
                            color = TrafficGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { onManualForceGreen(signal.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = TrafficGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("force_green_${signal.id}")
                    ) {
                        Text(
                            text = "ગ્રીન કરો",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
