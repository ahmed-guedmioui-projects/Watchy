package com.ahmedapps.watchy.auth.domain.repository

import com.ahmedapps.watchy.auth.util.AuthResult

interface AuthRepository {

    suspend fun singUp(
        name: String, email: String, password: String
    ): AuthResult<Unit>

    suspend fun singIn(
        email: String, password: String
    ): AuthResult<Unit>

    suspend fun authenticate(): AuthResult<Unit>

    suspend fun singOut(): AuthResult<Unit>


}















