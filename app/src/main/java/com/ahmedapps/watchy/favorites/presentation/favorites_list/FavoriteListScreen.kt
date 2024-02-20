package com.ahmedapps.watchy.favorites.presentation.favorites_list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.favorites.presentation.FavoritesScreenState
import com.ahmedapps.watchy.favorites.presentation.FavoriteUiEvents
import com.ahmedapps.watchy.ui.theme.BigRadius
import com.ahmedapps.watchy.ui.theme.HugeRadius

import com.ahmedapps.watchy.ui.ui_shared_components.NonFocusedTopBar
import com.ahmedapps.watchy.util.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoriteListScreen(
    route: String,
    mainNavController: NavController,
    favoritesScreenState: FavoritesScreenState,
    onEvent: (FavoriteUiEvents) -> Unit
) {

    val toolbarHeightPx = with(LocalDensity.current) { HugeRadius.dp.roundToPx().toFloat() }
    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = toolbarOffsetHeightPx.floatValue + delta
                toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    val mediaList = when (route) {
        Route.LIKED_SCREEN -> favoritesScreenState.likedList
        else -> favoritesScreenState.watchlist
    }

    val title = when (route) {
        Route.LIKED_SCREEN -> stringResource(id = R.string.favorites)
        else -> stringResource(id = R.string.bookmarks)
    }

    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    fun refresh() = refreshScope.launch {
        refreshing = true
        delay(1500)

        onEvent(FavoriteUiEvents.Refresh)
        refreshing = false
    }

    val refreshState = rememberPullRefreshState(refreshing, ::refresh)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .pullRefresh(refreshState)
    ) {

        val listState = rememberLazyGridState()

        LazyVerticalGrid(
            state = listState,
            contentPadding = PaddingValues(top = HugeRadius.dp),
            columns = GridCells.Adaptive(190.dp),
        ) {

            items(mediaList.size) { i ->
                FavoriteMediaItem(
                    media = mediaList[i],
                    mainNavController = mainNavController,
                    favoritesScreenState = favoritesScreenState
                )
            }
        }

        NonFocusedTopBar(
            title = title,
            toolbarOffsetHeightPx = toolbarOffsetHeightPx.floatValue.roundToInt(),
            mainNavController = mainNavController,
        )

        PullRefreshIndicator(
            refreshing = refreshing,
            state = refreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (BigRadius - 8).dp)
        )

    }
}
