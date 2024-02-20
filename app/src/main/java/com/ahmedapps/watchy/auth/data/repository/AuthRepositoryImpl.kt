package com.ahmedapps.watchy.auth.data.repository

import android.content.SharedPreferences
import com.ahmedapps.watchy.auth.data.remote.AuthApi
import com.ahmedapps.watchy.auth.domain.model.AuthRequest
import com.ahmedapps.watchy.auth.domain.repository.AuthRepository
import com.ahmedapps.watchy.auth.util.AuthResult
import com.ahmedapps.watchy.favorites.domain.repository.FavoritesRepository
import com.ahmedapps.watchy.main.domain.repository.GenreRepository
import com.ahmedapps.watchy.main.domain.repository.MainRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val mainRepository: MainRepository,
    private val favoritesRepository: FavoritesRepository,
    private val genreRepository: GenreRepository,
    private val prefs: SharedPreferences
) : AuthRepository {

    override suspend fun singUp(
        name: String, email: String, password: String
    ): AuthResult<Unit> {
        return try {

            authApi.signup(
                request = AuthRequest(
                    name = name,
                    email = email,
                    password = password
                )
            )

            singIn(
                email = email,
                password = password
            )

        } catch (e: HttpException) {
            if (e.code() == 401) {
                e.printStackTrace()
                AuthResult.Unauthorized()
            } else {
                e.printStackTrace()
                AuthResult.UnknownError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.UnknownError()
        }
    }

    override suspend fun singIn(
        email: String, password: String
    ): AuthResult<Unit> {
        return try {
            val authResponse = authApi.signin(
                request = AuthRequest(
                    email = email,
                    password = password
                )
            )

            prefs.edit().putString("email", email).apply()
            prefs.edit().putString("name", authResponse.name).apply()

            AuthResult.Authorized()

        } catch (e: HttpException) {
            if (e.code() == 401) {
                e.printStackTrace()
                AuthResult.Unauthorized()
            } else {
                e.printStackTrace()
                AuthResult.UnknownError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.UnknownError()
        }
    }

    override suspend fun authenticate(): AuthResult<Unit> {
        return try {

            val email = prefs.getString("email", null)
                ?: return AuthResult.Unauthorized()

            authApi.authenticate(
                request = AuthRequest(email = email)
            )
            AuthResult.Authorized()

        } catch (e: HttpException) {
            if (e.code() == 401) {
                e.printStackTrace()
                AuthResult.Unauthorized()
            } else {
                e.printStackTrace()
                AuthResult.Authorized()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.Authorized()
        }
    }

    override suspend fun singOut(): AuthResult<Unit> {
        prefs.edit().putString("email", null).apply()
        prefs.edit().putString("name", null).apply()

        mainRepository.clearMediaDb()
        genreRepository.clearGenresDb()
        favoritesRepository.clearFavoritesDb()

        return AuthResult.SingedOut()
    }

}










