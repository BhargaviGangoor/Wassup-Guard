package com.example.wassupguard.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface VirusTotalApi {
    // Note: VirusTotal v3: GET /files/{id} where id is sha256
    @GET("files/{sha256}")
    @Headers("Accept: application/json")
    suspend fun getFileReport(
        @Path("sha256") sha256: String
    ): VirusTotalResponse
}


