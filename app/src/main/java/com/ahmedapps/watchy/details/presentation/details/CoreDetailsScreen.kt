package com.ahmedapps.watchy.details.presentation.details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.details.presentation.detail_ui_components.MovieImage
import com.ahmedapps.watchy.details.presentation.watch_video.WatchVideoScreen
import com.ahmedapps.watchy.main.data.remote.api.MediaApi
import com.ahmedapps.watchy.main.domain.models.Media
import com.ahmedapps.watchy.main.presentation.main.MainUiState
import com.ahmedapps.watchy.ui.theme.SmallRadius
import com.ahmedapps.watchy.ui.theme.font
import com.ahmedapps.watchy.ui.ui_shared_components.RatingBar
import com.ahmedapps.watchy.util.APIConstants
import com.ahmedapps.watchy.util.Route
import com.ahmedapps.watchy.util.genresProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CoreDetailScreen(
    id: Int,
    mainUiState: MainUiState,
) {

    val detailsViewModel = hiltViewModel<DetailsViewModel>()
    val mediaDetailsScreenState =
        detailsViewModel.detailsScreenState.collectAsState().value

    val detailsNavController = rememberNavController()
    NavHost(
        detailsNavController,
        startDestination = Route.DETAILS_SCREEN
    ) {
        composable(route = Route.DETAILS_SCREEN) {
            DetailScreen(
                detailsScreenState = mediaDetailsScreenState,
                onEvent = detailsViewModel::onEvent
            )
        }

        composable(
            "${Route.WATCH_VIDEO_SCREEN}?videoId={videoId}",
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType }
            )
        ) {

            val videoId = it.arguments?.getString("videoId") ?: ""

            WatchVideoScreen(
                lifecycleOwner = LocalLifecycleOwner.current,
                videoId = videoId
            )
        }
    }

    LaunchedEffect(key1 = true) {
        detailsViewModel.onEvent(
            DetailsUiEvents.SetDataAndLoad(
                moviesGenresList = mainUiState.moviesGenresList,
                tvGenresList = mainUiState.tvGenresList,
                id = id
            )
        )
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        detailsViewModel.navigateToWatchVideo.collect { videoId ->
            if (videoId.isNotEmpty()) {
                detailsNavController
                    .navigate("${Route.WATCH_VIDEO_SCREEN}?videoId=$videoId")
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.no_video_is_available_at_the_moment),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DetailScreen(
    detailsScreenState: DetailsScreenState,
    onEvent: (DetailsUiEvents) -> Unit
) {

    if (detailsScreenState.media != null) {
        val refreshScope = rememberCoroutineScope()
        var refreshing by remember { mutableStateOf(false) }

        fun refresh() = refreshScope.launch {
            refreshing = true
            delay(1500)
            onEvent(DetailsUiEvents.Refresh)
            refreshing = false
        }

        val refreshState = rememberPullRefreshState(refreshing, ::refresh)

        val imageUrl = "${MediaApi.IMAGE_BASE_URL}${detailsScreenState.media.backdropPath}"

        val imagePainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .size(Size.ORIGINAL)
                .build()
        )

        val surface = MaterialTheme.colorScheme.surface
        var averageColor by remember {
            mutableStateOf(surface)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(refreshState)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {

                    VideoSection(
                        media = detailsScreenState.media,
                        imageState = imagePainter.state,
                        onEvent = onEvent
                    ) { color ->
                        averageColor = color
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {

                        PosterSection(media = detailsScreenState.media) {}

                        Spacer(modifier = Modifier.width(12.dp))

                        InfoSection(
                            media = detailsScreenState.media,
                            detailsScreenState = detailsScreenState
                        )

                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                OverviewSection(media = detailsScreenState.media)

                Spacer(modifier = Modifier.height(100.dp))

            }

            FavoritesSection(
                detailsScreenState = detailsScreenState,
                modifier = Modifier.align(Alignment.BottomCenter),
                onEvent = onEvent
            )

            PullRefreshIndicator(
                refreshing, refreshState, Modifier.align(Alignment.TopCenter)
            )
        }
    } else {
        SomethingWentWrong()
    }
}

@Composable
fun VideoSection(
    media: Media,
    imageState: AsyncImagePainter.State,
    onEvent: (DetailsUiEvents) -> Unit,
    onImageLoaded: (color: Color) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable {
                onEvent(DetailsUiEvents.NavigateToWatchVideo)
            },
        shape = RoundedCornerShape(0),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {

            MovieImage(
                imageState = imageState,
                description = media.title,
                noImageId = null,
            ) { color ->
                onImageLoaded(color)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .size(50.dp)
                    .alpha(0.7f)
                    .background(Color.LightGray)
            )
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = stringResource(id = R.string.watch_trailer),
                tint = Color.Black,
                modifier = Modifier.size(35.dp)
            )

        }
    }
}

@Composable
fun PosterSection(
    media: Media,
    onImageLoaded: (color: Color) -> Unit
) {

    val posterUrl = "${MediaApi.IMAGE_BASE_URL}${media.posterPath}"
    val posterPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current).data(posterUrl).size(Size.ORIGINAL)
            .build()
    )
    val posterState = posterPainter.state


    Column {
        Spacer(modifier = Modifier.height(200.dp))

        Card(
            modifier = Modifier
                .width(180.dp)
                .height(250.dp)
                .padding(start = 16.dp),
            shape = RoundedCornerShape(SmallRadius),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                MovieImage(
                    imageState = posterState,
                    description = media.title,
                    noImageId = Icons.Rounded.ImageNotSupported
                ) { color ->
                    onImageLoaded(color)
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    media: Media,
    detailsScreenState: DetailsScreenState,
) {

    var genres by remember {
        mutableStateOf("")
    }
    LaunchedEffect(media) {
        genres = genresProvider(
            genreIds = media.genreIds,
            allGenres = if (media.mediaType == APIConstants.MOVIE) detailsScreenState.moviesGenresList
            else detailsScreenState.tvGenresList
        )
    }

    Column(
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(260.dp))

        Text(
            text = media.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontFamily = font,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (media.voteAverage != 0.0) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingBar(
                    modifier = Modifier,
                    starsModifier = Modifier.size(18.dp),
                    rating = media.voteAverage.div(2)
                )

                Text(
                    modifier = Modifier.padding(
                        horizontal = 4.dp
                    ),
                    text = media.voteAverage.div(2).toString().take(3),
                    fontFamily = font,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        if (media.releaseDate.isNotEmpty()) {
            Text(
                text = media.releaseDate.take(4),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = font,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 6.dp, vertical = 0.5.dp),
            text = if (media.adult) stringResource(R.string._18)
            else stringResource(R.string._12),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = font,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(7.dp))

        if (genres.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = genres,
                fontFamily = font,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(7.dp))
        }

        if (detailsScreenState.readableTime.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = detailsScreenState.readableTime,
                fontFamily = font,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
        }
    }
}


@Composable
fun OverviewSection(
    media: Media
) {
    Column {
        if (media.tagline.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 22.dp),
                text = "\"${media.tagline}\"",
                fontFamily = font,
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (media.overview.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 22.dp),
                text = stringResource(R.string.overview),
                fontFamily = font,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                modifier = Modifier.padding(horizontal = 22.dp),
                text = media.overview,
                fontFamily = font,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun FavoritesSection(
    detailsScreenState: DetailsScreenState,
    modifier: Modifier = Modifier,
    onEvent: (DetailsUiEvents) -> Unit
) {

    if (detailsScreenState.showAlertDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = if (detailsScreenState.alertDialogType == 1) {
                        stringResource(R.string.remove_from_favorites)
                    } else {
                        stringResource(R.string.remove_from_bookmarks)
                    },
                    fontFamily = font,
                    fontSize = 17.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (detailsScreenState.alertDialogType == 1) {
                            onEvent(DetailsUiEvents.LikeOrDislike)
                        } else {
                            onEvent(DetailsUiEvents.AddOrRemoveFromWatchlist)
                        }
                    }
                ) {
                    Text(
                        stringResource(R.string.yes),
                        fontFamily = font
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onEvent(DetailsUiEvents.ShowAndHideAlertDialog())
                    }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        fontFamily = font
                    )
                }
            }
        )
    }

    detailsScreenState.media?.let { media ->

        Row(
            modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    onEvent(DetailsUiEvents.ShowAndHideAlertDialog(1))
                }
            ) {
                if (media.isLiked) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = stringResource(R.string.like)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(R.string.dislike)
                    )
                }

            }

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                modifier = modifier
                    .fillMaxWidth(1f),
                onClick = {
                    onEvent(DetailsUiEvents.ShowAndHideAlertDialog(2))
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (media.isBookmarked) {
                        Icon(
                            imageVector = Icons.Rounded.Bookmark,
                            contentDescription = stringResource(R.string.unbookmark)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkBorder,
                            contentDescription = stringResource(R.string.bookmark)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (media.isBookmarked)
                            stringResource(R.string.unbookmark)
                        else stringResource(R.string.bookmark),
                        fontFamily = font
                    )
                }
            }
        }
    }
}

@Composable
fun SomethingWentWrong() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.something_went_wrong),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontFamily = font,
            fontSize = 19.sp
        )
    }
}


