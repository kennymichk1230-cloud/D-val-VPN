package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_profiles")
data class VpnProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val configText: String,
    val countryCode: String, // US, GB, JP, DE, SG, etc.
    val isCustom: Boolean = false
)
