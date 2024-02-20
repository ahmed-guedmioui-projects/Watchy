package com.ahmedapps.watchy.details.presentation.details

import com.ahmedapps.watchy.main.domain.models.Genre
import com.ahmedapps.watchy.main.domain.models.Media

data class DetailsScreenState(

    val isLoading: Boolean = false,

    val media: Media? = null,

    val videoId: String = "",
    val readableTime: String = "",

    val videosList: List<String> = emptyList(),
    val moviesGenresList: List<Genre> = emptyList(),
    val tvGenresList: List<Genre> = emptyList(),

    val showAlertDialog: Boolean = false,
    val alertDialogType: Int = 0
)