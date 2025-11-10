package com.example.wassupguard.network

import com.squareup.moshi.Json

data class VirusTotalResponse(
    val data: Data?
)

data class Data(
    val id: String?,
    val type: String?,
    val attributes: Attributes?
)

data class Attributes(
    @Json(name = "last_analysis_stats") val lastAnalysisStats: LastAnalysisStats?,
    @Json(name = "md5") val md5: String?,
    @Json(name = "sha1") val sha1: String?,
    @Json(name = "sha256") val sha256: String?
)

data class LastAnalysisStats(
    val harmless: Int?,
    val malicious: Int?,
    val suspicious: Int?,
    val undetected: Int?,
    val timeout: Int?
)


