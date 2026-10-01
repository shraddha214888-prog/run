package com.example.data.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Priority levels for emergency ambulance dispatch
 */
enum class EmergencyPriority(
    val titleGujarati: String,
    val titleEnglish: String,
    val sirenIntensity: Int
) {
    CRITICAL("અત્યંત ગંભીર (કોડ રેડ)", "Critical Code Red", 3),
    HIGH("ઉચ્ચ પ્રાથમિકતા (કોડ યલો)", "High Priority", 2),
    STANDARD("સામાન્ય ઈમરજન્સી (કોડ ગ્રીન)", "Standard Emergency", 1)
}

/**
 * Emergency types available for 108 Ambulance dispatch
 */
data class EmergencyCase(
    val id: String,
    val titleGujarati: String,
    val titleEnglish: String,
    val priority: EmergencyPriority,
    val descriptionGujarati: String,
    val recommendedFacility: String
)

/**
 * Hospital facility with live trauma/ICU telemetry
 */
data class Hospital(
    val id: String,
    val nameGujarati: String,
    val nameEnglish: String,
    val addressGujarati: String,
    val latitude: Double,
    val longitude: Double,
    val totalIcuBeds: Int,
    val availableIcuBeds: Int,
    val traumaLevel: String,
    val phone: String,
    val specializedUnits: List<String>,
    val distanceKm: Float = 0f,
    val estimatedMinutes: Int = 0
)

/**
 * Traffic signal status in smart corridor
 */
enum class SignalState {
    RED,
    YELLOW,
    GREEN,
    PREEMPTED_GREEN // Emergency corridor force-cleared
}

/**
 * Smart Traffic Light Junction equipped with IoT Corridor Preemption
 */
data class TrafficSignalJunction(
    val id: String,
    val nameGujarati: String,
    val nameEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val currentState: SignalState,
    val normalCycleSeconds: Int,
    val preemptionActive: Boolean,
    val secondsRemaining: Int,
    val distanceFromAmbulanceMeters: Float = 0f,
    val vehiclesClearedCount: Int = 0
)

/**
 * Step in emergency GPS navigation
 */
data class NavigationStep(
    val stepNumber: Int,
    val instructionGujarati: String,
    val instructionEnglish: String,
    val distanceMeters: Int,
    val iconType: StepIconType
)

enum class StepIconType {
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    CROSS_JUNCTION,
    HOSPITAL_ARRIVAL
}

/**
 * Civilian commuter alert state when ambulance is in proximity
 */
enum class CivilianAlertLevel {
    NORMAL,            // No ambulance nearby (>1000m)
    AWARENESS,         // Ambulance within 1000m - 500m
    PREPARATION,       // Ambulance within 500m - 200m: Prepare to clear lane
    IMMEDIATE_ACTION   // Ambulance < 200m: Immediately shift left and stop
}

data class CivilianAlertStatus(
    val alertLevel: CivilianAlertLevel,
    val ambulanceDistanceMeters: Float,
    val ambulanceSpeedKmh: Int,
    val instructionGujarati: String,
    val instructionEnglish: String,
    val relativeBearingDegrees: Float, // Direction relative to commuter (e.g. 180 = directly behind)
    val corridorCleared: Boolean = false
)

/**
 * Navigation waypoint
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String = ""
)
