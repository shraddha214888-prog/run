package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM emergency_missions ORDER BY timestamp DESC")
    fun getAllMissions(): Flow<List<EmergencyMission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: EmergencyMission): Long

    @Query("SELECT COUNT(*) FROM emergency_missions")
    fun getMissionCount(): Flow<Int>

    @Query("SELECT SUM(trafficSignalsPreempted) FROM emergency_missions")
    fun getTotalSignalsPreempted(): Flow<Int?>

    // Civic Give-Way Records
    @Query("SELECT * FROM civic_clearance_records ORDER BY timestamp DESC")
    fun getAllCivicRecords(): Flow<List<CivicClearanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCivicRecord(record: CivicClearanceRecord): Long

    @Query("SELECT SUM(pointsAwarded) FROM civic_clearance_records")
    fun getTotalCivicPoints(): Flow<Int?>
}
