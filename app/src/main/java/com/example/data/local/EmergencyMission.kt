package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_missions")
data class EmergencyMission(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val missionCode: String,
    val timestamp: Long,
    val emergencyType: String,
    val targetHospital: String,
    val durationSeconds: Int,
    val distanceCoveredKm: Float,
    val trafficSignalsPreempted: Int,
    val averageSpeedKmh: Int,
    val status: String // "પૂર્ણ થયેલ" (Completed), "સક્રિય" (Active)
)

@Entity(tableName = "civic_clearance_records")
data class CivicClearanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val ambulanceCode: String,
    val locationNameGujarati: String,
    val pointsAwarded: Int,
    val reactionTimeSeconds: Int
)
