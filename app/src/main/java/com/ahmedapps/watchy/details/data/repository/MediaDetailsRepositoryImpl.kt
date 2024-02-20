package com.ahmedapps.watchy.details.data.repository

import android.app.Application
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.main.domain.models.Media
import com.ahmedapps.watchy.main.domain.repository.MainRepository
import com.ahmedapps.watchy.details.data.remote.api.DetailsApi
import com.ahmedapps.watchy.details.data.remote.dto.details.DetailsDto
import com.ahmedapps.watchy.details.domain.repository.MediaDetailsRepository
import com.ahmedapps.watchy.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaDetailsRepositoryImpl @Inject constructor(
    private val detailsApi: DetailsApi,
    private val mainRepository: MainRepository,
    private val application: Application,
) : MediaDetailsRepository {

    override suspend fun getDetails(
        type: String,
        isRefresh: Boolean,
        id: Int,
        apiKey: String
    ): Flow<Resource<Media>> {

        return flow {

            emit(Resource.Loading(true))

            val media = mainRepository.getMediaById(id = id)

            val doDetailsExist =
                media.runtime != 0 || media.status.isNotEmpty() || media.tagline.isNotEmpty()

            if (!isRefresh && doDetailsExist) {
                emit(Resource.Success(data = media))
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteDetails = fetchRemoteForDetails(
                type = media.mediaType,
                id = id,
                apiKey = apiKey
            )

            remoteDetails?.let { detailsDto ->
                val mediaWithDetails = media.copy(
                    runtime = detailsDto.runtime ?: 0,
                    status = detailsDto.status ?: "",
                    tagline = detailsDto.tagline ?: ""
                )
                mainRepository.upsertMediaItem(mediaWithDetails)

                emit(Resource.Success(mainRepository.getMediaById(id)))
                emit(Resource.Loading(false))
                return@flow
            }

            emit(Resource.Error(application.getString(R.string.couldnt_load_details)))
            emit(Resource.Loading(false))

        }

    }

    private suspend fun fetchRemoteForDetails(
        type: String,
        id: Int,
        apiKey: String
    ): DetailsDto? {

        val remoteDetails = try {
            detailsApi.getDetails(
                type = type,
                id = id,
                apiKey = apiKey
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        return remoteDetails
    }

    override suspend fun getVideosList(
        isRefresh: Boolean,
        id: Int,
        apiKey: String
    ): Flow<Resource<List<String>>> {
        return flow {

            emit(Resource.Loading(true))

            val media = mainRepository.getMediaById(id = id)

            val doVideosExist = media.videosIds.isNotEmpty()

            if (!isRefresh && doVideosExist) {
                emit(Resource.Success(media.videosIds))
                emit(Resource.Loading(false))
                return@flow
            }

            val remoteVideosIds = fetchRemoteForVideosIds(
                type = media.mediaType,
                id = id,
                apiKey = apiKey
            )

            remoteVideosIds?.let { videoIds ->
                if (videoIds.isNotEmpty()) {
                    mainRepository.upsertMediaItem(
                        media.copy(videosIds = videoIds)
                    )

                    emit(
                        Resource.Success(mainRepository.getMediaById(id).videosIds)
                    )
                } else {
                    emit(Resource.Error(application.getString(R.string.couldnt_get_video_ids)))
                }
                emit(Resource.Loading(false))
                return@flow
            }

            emit(Resource.Error(application.getString(R.string.couldnt_get_video_ids)))
            emit(Resource.Loading(false))

        }
    }

    private suspend fun fetchRemoteForVideosIds(
        type: String,
        id: Int,
        apiKey: String
    ): List<String>? {

        val remoteVideosIds = try {
            detailsApi.getVideosList(
                type = type,
                id = id,
                apiKey = apiKey
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val listToReturn = remoteVideosIds?.results?.filter {
            it.site == "YouTube"
        }

        return listToReturn?.map { it.key }

    }

}










