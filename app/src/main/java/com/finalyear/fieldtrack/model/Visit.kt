package com.finalyear.fieldtrack.model

/**
 * Represents one field visit record.
 * synced = 0 -> not yet pushed to the remote web API, 1 -> synced
 */
data class Visit(
    val id: Long = 0,
    val siteName: String,
    val notes: String,
    val latitude: Double,
    val longitude: Double,
    val photoPath: String?,
    val timestamp: Long,
    val synced: Int = 0
)
