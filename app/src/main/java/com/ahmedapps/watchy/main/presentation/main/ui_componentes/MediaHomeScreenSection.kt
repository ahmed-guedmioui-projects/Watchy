package com.ahmedapps.watchy.main.presentation.main.ui_componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.main.domain.models.Media
import com.ahmedapps.watchy.main.presentation.main.MainUiState
import com.ahmedapps.watchy.ui.theme.Radius
import com.ahmedapps.watchy.ui.theme.font
import com.ahmedapps.watchy.ui.ui_shared_components.MediaItemImage
import com.ahmedapps.watchy.util.Route

@Composable
fun MediaHomeScreenSection(
    route: String,
    mainNavController: NavController,
    mainUiState: MainUiState,
) {

    val mediaList: List<Media>
    val title: String

    when (route) {
        Route.TRENDING_NOW_SCREEN -> {
            title = stringResource(id = R.string.trending)
            mediaList = mainUiState.trendingAllList.take(10)
        }

        Route.TV_SERIES_SCREEN -> {
            title = stringResource(id = R.string.tv_series)
            mediaList = mainUiState.popularTvSeriesList.take(10)
        }

        else -> {
            title = stringResource(id = R.string.popular_movies)
            mediaList = mainUiState.popularMoviesList.take(10)
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = font,
                fontSize = 20.sp
            )

            Text(
                modifier = Modifier
                    .alpha(0.7f)
                    .clickable { mainNavController.navigate(route) },
                text = stringResource(id = R.string.see_all),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontFamily = font,
                fontSize = 14.sp,
            )
        }

        if (mediaList.isEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .height(200.dp)
                    .width(150.dp)
                    .padding(horizontal = 16.dp)
            )
        } else {
            LazyRow {
                items(mediaList.size) {

                    var paddingEnd = 0.dp
                    if (it == mediaList.size - 1) {
                        paddingEnd = 16.dp
                    }

                    MediaItemImage(
                        media = mediaList[it],
                        mainNavController = mainNavController,
                        modifier = Modifier
                            .height(200.dp)
                            .width(150.dp)
                            .padding(start = 16.dp, end = paddingEnd)
                    )
                }
            }
        }
    }

}
