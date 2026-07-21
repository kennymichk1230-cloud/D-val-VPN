package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_connection_logs")
data class VpnConnectionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long, // in seconds
    val bytesUploaded: Long,
    val bytesDownloaded: Long,
    val status: String // "Connected", "Failed", etc.
)
