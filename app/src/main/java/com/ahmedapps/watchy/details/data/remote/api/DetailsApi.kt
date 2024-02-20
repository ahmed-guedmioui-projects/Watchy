package com.ahmedapps.watchy.details.data.remote.api

import com.ahmedapps.watchy.details.data.remote.dto.details.DetailsDto
import com.ahmedapps.watchy.details.data.remote.dto.videos.VideosList
import com.ahmedapps.watchy.main.data.remote.dto.MediaListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DetailsApi {

    @GET("{type}/{id}")
    suspend fun getDetails(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): DetailsDto?

    @GET("{type}/{id}/videos")
    suspend fun getVideosList(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): VideosList?

}