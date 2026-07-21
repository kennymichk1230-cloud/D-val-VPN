package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnProfileDao {
    @Query("SELECT * FROM vpn_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<VpnProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VpnProfile): Long

    @Delete
    suspend fun deleteProfile(profile: VpnProfile)

    @Query("DELETE FROM vpn_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)

    @Query("SELECT * FROM vpn_connection_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<VpnConnectionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VpnConnectionLog): Long

    @Query("DELETE FROM vpn_connection_logs")
    suspend fun clearLogs()
}
