package com.ahmedapps.watchy.auth.data.remote

import com.ahmedapps.watchy.auth.domain.model.AuthRequest
import com.ahmedapps.watchy.auth.domain.model.AuthResponse
import com.ahmedapps.watchy.util.BackendConstants
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST(BackendConstants.SIGN_UP)
    suspend fun signup(
        @Body request: AuthRequest
    )

    @POST(BackendConstants.SIGN_IN)
    suspend fun signin(
        @Body request: AuthRequest
    ): AuthResponse

    @GET(BackendConstants.AUTHENTICATE)
    suspend fun authenticate(
        @Body request: AuthRequest
    )

}














