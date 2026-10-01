package com.example.ui.components

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Hospital
import com.example.data.model.SignalState
import com.example.data.model.TrafficSignalJunction
import com.example.ui.theme.BeaconBlue
import com.example.ui.theme.DarkHighwayBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBright
import com.example.ui.theme.TrafficAmber
import com.example.ui.theme.TrafficGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveCorridorMap(
    ambulanceProgress: Float, // 0.0 to 1.0 along the corridor
    targetHospital: Hospital?,
    signals: List<TrafficSignalJunction>,
    speedKmh: Int,
    isSirenActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_animations")

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    val beaconFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_flash"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkHighwayBackground)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw Grid Lines for Urban Map look
            drawUrbanGrid(width, height)

            // 2. Main Highway Corridor (Curved Road)
            val roadPoints = listOf(
                Offset(width * 0.12f, height * 0.85f),
                Offset(width * 0.32f, height * 0.65f),
                Offset(width * 0.52f, height * 0.60f),
                Offset(width * 0.72f, height * 0.38f),
                Offset(width * 0.88f, height * 0.18f)
            )

            // Draw Road Base
            val roadPath = Path().apply {
                moveTo(roadPoints[0].x, roadPoints[0].y)
                cubicTo(
                    roadPoints[1].x, roadPoints[1].y,
                    roadPoints[2].x, roadPoints[2].y,
                    roadPoints[3].x, roadPoints[3].y
                )
                lineTo(roadPoints[4].x, roadPoints[4].y)
            }

            // Road Asphalt
            drawPath(
                path = roadPath,
                color = Color(0xFF1E2638),
                style = Stroke(width = 56f)
            )

            // Green Wave Corridor Overlay (Glow)
            drawPath(
                path = roadPath,
                color = TrafficGreen.copy(alpha = 0.28f),
                style = Stroke(width = 62f)
            )

            // Road Edge Lines
            drawPath(
                path = roadPath,
                color = Color(0xFF334155),
                style = Stroke(width = 58f)
            )
            drawPath(
                path = roadPath,
                color = Color(0xFF1E2638),
                style = Stroke(width = 52f)
            )

            // Center Road Dashes (Traffic Lane Divider)
            drawPath(
                path = roadPath,
                color = Color(0xFFE2E8F0).copy(alpha = 0.65f),
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f), 0f)
                )
            )

            // 3. Draw Side Civilian Vehicles giving space to ambulance (pulled left)
            drawCommuterVehiclesParting(width, height, ambulanceProgress)

            // 4. Draw Traffic Signal Junctions along the corridor
            val signalOffsets = listOf(
                Offset(width * 0.28f, height * 0.68f),
                Offset(width * 0.50f, height * 0.60f),
                Offset(width * 0.70f, height * 0.40f)
            )

            signalOffsets.forEachIndexed { index, offset ->
                val signal = signals.getOrNull(index)
                drawTrafficSignalNode(offset, signal)
            }

            // 5. Draw Destination Hospital Marker
            val hospitalOffset = roadPoints.last()
            drawHospitalDestination(hospitalOffset, targetHospital)

            // 6. Draw Ambulance Position along the path
            val ambPos = calculatePositionOnCorridor(roadPoints, ambulanceProgress)
            drawAmbulanceVehicle(
                center = ambPos,
                pulseRadius = pulseRadius,
                isSirenActive = isSirenActive,
                beaconFlash = beaconFlash
            )
        }

        // Overlay Badge: Speed & Live Corridor Status
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC0F172A))
                .border(1.dp, TrafficGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "⚡ ગ્રીન કોરિડોર: $speedKmh કિમી/કલાક",
                color = TrafficGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Overlay Badge: Target Hospital
        targetHospital?.let { hosp ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC0F172A))
                    .border(1.dp, EmergencyRedBright.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🏥 ${hosp.nameGujarati.take(18)}...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun DrawScope.drawUrbanGrid(width: Float, height: Float) {
    val step = 40f
    var x = 0f
    while (x <= width) {
        drawLine(
            color = Color(0xFF131D31),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += step
    }
    var y = 0f
    while (y <= height) {
        drawLine(
            color = Color(0xFF131D31),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawCommuterVehiclesParting(width: Float, height: Float, ambProgress: Float) {
    // Commuter civilian cars pulled over to the left side of the road
    val cars = listOf(
        Offset(width * 0.22f, height * 0.77f),
        Offset(width * 0.42f, height * 0.69f),
        Offset(width * 0.63f, height * 0.49f)
    )

    cars.forEachIndexed { i, carPos ->
        // Draw car chassis
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(carPos.x - 8f, carPos.y - 14f),
            size = Size(16f, 28f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Hazard flasher (yellow)
        drawCircle(
            color = TrafficAmber,
            radius = 3f,
            center = Offset(carPos.x - 6f, carPos.y - 12f)
        )
        drawCircle(
            color = TrafficAmber,
            radius = 3f,
            center = Offset(carPos.x + 6f, carPos.y - 12f)
        )
    }
}

private fun DrawScope.drawTrafficSignalNode(offset: Offset, signal: TrafficSignalJunction?) {
    // Signal junction tower
    val isGreenWave = signal?.currentState == SignalState.PREEMPTED_GREEN || signal?.currentState == SignalState.GREEN
    val signalColor = when (signal?.currentState) {
        SignalState.PREEMPTED_GREEN, SignalState.GREEN -> TrafficGreen
        SignalState.YELLOW -> TrafficAmber
        SignalState.RED, null -> EmergencyRed
    }

    // Outer glow for signal
    drawCircle(
        color = signalColor.copy(alpha = 0.35f),
        radius = 20f,
        center = offset
    )
    // Signal body
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(offset.x - 10f, offset.y - 18f),
        size = Size(20f, 36f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Lamp
    drawCircle(
        color = signalColor,
        radius = 7f,
        center = Offset(offset.x, offset.y + if (isGreenWave) 8f else -8f)
    )
}

private fun DrawScope.drawHospitalDestination(offset: Offset, hospital: Hospital?) {
    // Hospital circular base with red cross
    drawCircle(
        color = EmergencyRed.copy(alpha = 0.3f),
        radius = 28f,
        center = offset
    )
    drawCircle(
        color = Color.White,
        radius = 18f,
        center = offset
    )
    // Red Cross symbol
    drawRect(
        color = EmergencyRed,
        topLeft = Offset(offset.x - 3f, offset.y - 12f),
        size = Size(6f, 24f)
    )
    drawRect(
        color = EmergencyRed,
        topLeft = Offset(offset.x - 12f, offset.y - 3f),
        size = Size(24f, 6f)
    )
}

private fun DrawScope.drawAmbulanceVehicle(
    center: Offset,
    pulseRadius: Float,
    isSirenActive: Boolean,
    beaconFlash: Float
) {
    // Radar Pulse Wave (clearing traffic radius)
    if (isSirenActive) {
        drawCircle(
            color = EmergencyRedBright.copy(alpha = (1f - (pulseRadius / 65f)).coerceIn(0f, 0.6f)),
            radius = pulseRadius,
            center = center,
            style = Stroke(width = 3f)
        )
        drawCircle(
            color = BeaconBlue.copy(alpha = (1f - (pulseRadius / 65f)).coerceIn(0f, 0.4f)),
            radius = pulseRadius * 0.7f,
            center = center,
            style = Stroke(width = 2.5f)
        )
    }

    // Ambulance Van Base
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(center.x - 14f, center.y - 24f),
        size = Size(28f, 48f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Windshield
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(center.x - 11f, center.y - 21f),
        size = Size(22f, 10f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Red Cross on top
    drawRect(
        color = EmergencyRed,
        topLeft = Offset(center.x - 2f, center.y - 2f),
        size = Size(4f, 14f)
    )
    drawRect(
        color = EmergencyRed,
        topLeft = Offset(center.x - 7f, center.y + 3f),
        size = Size(14f, 4f)
    )

    // Siren Beacon Flasher (Alternating Red & Blue)
    val leftSirenColor = if (beaconFlash > 0.5f) EmergencyRedBright else BeaconBlue
    val rightSirenColor = if (beaconFlash > 0.5f) BeaconBlue else EmergencyRedBright

    drawCircle(
        color = leftSirenColor,
        radius = 5f,
        center = Offset(center.x - 7f, center.y - 8f)
    )
    drawCircle(
        color = rightSirenColor,
        radius = 5f,
        center = Offset(center.x + 7f, center.y - 8f)
    )
}

private fun calculatePositionOnCorridor(points: List<Offset>, progress: Float): Offset {
    if (points.isEmpty()) return Offset.Zero
    val clampedProgress = progress.coerceIn(0.02f, 0.96f)
    val totalSegments = points.size - 1
    val scaledProgress = clampedProgress * totalSegments
    val segmentIndex = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
    val localProgress = scaledProgress - segmentIndex

    val p1 = points[segmentIndex]
    val p2 = points[segmentIndex + 1]

    return Offset(
        x = p1.x + (p2.x - p1.x) * localProgress,
        y = p1.y + (p2.y - p1.y) * localProgress
    )
}
