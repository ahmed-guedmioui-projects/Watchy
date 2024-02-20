package com.ahmedapps.watchy.details.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedapps.watchy.favorites.domain.repository.FavoritesRepository
import com.ahmedapps.watchy.main.data.remote.api.MediaApi.Companion.API_KEY
import com.ahmedapps.watchy.main.domain.repository.MainRepository
import com.ahmedapps.watchy.details.domain.repository.MediaDetailsRepository
import com.ahmedapps.watchy.details.domain.usecase.MinutesToReadableTime
import com.ahmedapps.watchy.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val mediaDetailsRepository: MediaDetailsRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _detailsScreenState = MutableStateFlow(DetailsScreenState())
    val detailsScreenState = _detailsScreenState.asStateFlow()

    private val _navigateToWatchVideo = Channel<String>()
    val navigateToWatchVideo = _navigateToWatchVideo.receiveAsFlow()

    fun onEvent(event: DetailsUiEvents) {
        when (event) {
            is DetailsUiEvents.SetDataAndLoad -> {
                _detailsScreenState.update {
                    it.copy(
                        moviesGenresList = event.moviesGenresList,
                        tvGenresList = event.tvGenresList,
                    )
                }

                loadMediaItem(
                    isRefresh = false,
                    id = event.id
                )
            }

            is DetailsUiEvents.NavigateToWatchVideo -> {

                val doesVideoExist = detailsScreenState.value.videoId.isNotEmpty()

                viewModelScope.launch {
                    if (doesVideoExist) {
                        _navigateToWatchVideo.send(
                            detailsScreenState.value.videoId
                        )
                    } else {
                        _navigateToWatchVideo.send("")
                    }
                }

            }

            is DetailsUiEvents.Refresh -> {
                _detailsScreenState.update {
                    it.copy(
                        isLoading = true
                    )
                }

                loadMediaItem(isRefresh = true)
            }

            DetailsUiEvents.LikeOrDislike -> {
                likeOrDislike()
            }

            DetailsUiEvents.AddOrRemoveFromWatchlist -> {
                bookmarkOrUnbookmark()
            }

            is DetailsUiEvents.ShowAndHideAlertDialog -> {
                val media = detailsScreenState.value.media

                if (event.alertDialogType == 1 && media?.isLiked == false) {
                    likeOrDislike()
                    return
                }

                if (event.alertDialogType == 2 && media?.isBookmarked == false) {
                    bookmarkOrUnbookmark()
                    return
                }

                _detailsScreenState.update {
                    it.copy(
                        showAlertDialog = !it.showAlertDialog,
                        alertDialogType = event.alertDialogType
                    )
                }

            }
        }
    }

    private fun loadMediaItem(
        id: Int = detailsScreenState.value.media?.mediaId ?: 0,
        isRefresh: Boolean
    ) {
        viewModelScope.launch {
            _detailsScreenState.update {
                it.copy(
                    media = mainRepository.getMediaById(id = id)
                )
            }

            loadDetails(isRefresh) {
                loadVideosList(isRefresh)
            }
        }
    }

    private fun loadDetails(
        isRefresh: Boolean,
        onLoadDetailsFinished: () -> Unit
    ) {

        viewModelScope.launch {

            mediaDetailsRepository
                .getDetails(
                    id = detailsScreenState.value.media?.mediaId ?: 0,
                    type = detailsScreenState.value.media?.mediaType ?: "",
                    isRefresh = isRefresh,
                    apiKey = API_KEY
                )
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { media ->
                                _detailsScreenState.update {
                                    it.copy(
                                        media = it.media?.copy(
                                            runtime = media.runtime,
                                            status = media.status,
                                            tagline = media.tagline,
                                        ),
                                        readableTime = if (media.runtime != 0)
                                            MinutesToReadableTime(media.runtime).invoke()
                                        else ""
                                    )
                                }

                                onLoadDetailsFinished()
                            }
                        }

                        is Resource.Error -> {
                            onLoadDetailsFinished()
                        }

                        is Resource.Loading -> {
                            _detailsScreenState.update {
                                it.copy(
                                    isLoading = result.isLoading
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun loadVideosList(isRefresh: Boolean) {

        viewModelScope.launch {
            mediaDetailsRepository
                .getVideosList(
                    id = detailsScreenState.value.media?.mediaId ?: 0,
                    isRefresh = isRefresh,
                    apiKey = API_KEY
                )
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { videosList ->
                                _detailsScreenState.update {
                                    it.copy(
                                        videosList = videosList,
                                        videoId = videosList.shuffled()[0]
                                    )
                                }
                            }
                        }

                        is Resource.Error -> {}

                        is Resource.Loading -> {
                            _detailsScreenState.update {
                                it.copy(
                                    isLoading = result.isLoading
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun likeOrDislike() {
        _detailsScreenState.update {
            it.copy(
                media = it.media?.copy(
                    isLiked = !it.media.isLiked,
                ),
                alertDialogType = 0,
                showAlertDialog = false
            )
        }

        updateOrDeleteMedia()
    }

    private fun bookmarkOrUnbookmark() {
        _detailsScreenState.update {
            it.copy(
                media = it.media?.copy(
                    isBookmarked = !it.media.isBookmarked
                ),
                alertDialogType = 0,
                showAlertDialog = false
            )
        }

        updateOrDeleteMedia()
    }

    private fun updateOrDeleteMedia() {
        viewModelScope.launch {
            detailsScreenState.value.media?.let { media ->
                if (!media.isLiked && !media.isBookmarked) {
                    favoritesRepository.deleteFavoriteMediaItem(media)
                } else {
                    mainRepository.upsertMediaItem(media)
                    favoritesRepository.upsertFavoriteMediaItem(media)
                }
            }
        }
    }

}






