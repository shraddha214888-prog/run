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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.BeaconBlue
import com.example.ui.theme.BeaconBlueBright
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright

@Composable
fun SirenBeaconHeader(
    isEmergencyActive: Boolean,
    isSirenMuted: Boolean,
    onToggleSiren: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon_header")

    val strobeState by infiniteTransition.animateColor(
        initialValue = Color(0xFF334155),
        targetValue = if (isEmergencyActive) EmergencyRedBright else Color(0xFF334155),
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_color"
    )

    val blueStrobeState by infiniteTransition.animateColor(
        initialValue = Color(0xFF1E293B),
        targetValue = if (isEmergencyActive) BeaconBlueBright else Color(0xFF1E293B),
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blue_strobe_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isEmergencyActive) {
                    Brush.horizontalGradient(
                        listOf(
                            EmergencyRed.copy(alpha = 0.25f),
                            Color(0xFF0F172A),
                            BeaconBlue.copy(alpha = 0.25f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                }
            )
            .border(
                1.dp,
                if (isEmergencyActive) strobeState.copy(alpha = 0.7f) else Color(0xFF334155),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Alternating beacon indicators
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isEmergencyActive) strobeState else Color.Gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isEmergencyActive) blueStrobeState else Color.DarkGray)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isEmergencyActive) "🚨 ૧૦૮ ઇમરજન્સી કોરિડોર સક્રિય" else "૧૦૮ સ્ટેન્ડબાય મોડ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = onToggleSiren,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (!isSirenMuted) EmergencyRed else Color(0xFF334155))
                    .testTag("toggle_siren_button")
            ) {
                Icon(
                    imageVector = if (!isSirenMuted) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "સાયરન ચાલુ / બંધ કરો",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
