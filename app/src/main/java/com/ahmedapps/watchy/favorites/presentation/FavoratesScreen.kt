package com.ahmedapps.watchy.favorites.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.favorites.presentation.favorites_list.FavoriteListScreen
import com.ahmedapps.watchy.main.presentation.main.MainUiState
import com.ahmedapps.watchy.ui.theme.MediumRadius
import com.ahmedapps.watchy.ui.theme.font
import com.ahmedapps.watchy.ui.ui_shared_components.AutoSwipeSection
import com.ahmedapps.watchy.util.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CoreFavoriteScreen(
    mainNavController: NavController,
    mainUiState: MainUiState
) {

    val favoritesViewModel = hiltViewModel<FavoritesViewModel>()
    val favoritesScreenState = favoritesViewModel.favoritesScreenState.collectAsState().value

    LaunchedEffect(key1 = true) {
        favoritesViewModel.onEvent(
            FavoriteUiEvents.SetDataAndLoad(
                moviesGenresList = mainUiState.moviesGenresList,
                tvGenresList = mainUiState.tvGenresList
            )
        )
    }

    val favoritesNavController = rememberNavController()
    NavHost(
        navController = favoritesNavController,
        startDestination = Route.FAVORITES_SCREEN
    ) {

        composable(Route.FAVORITES_SCREEN) {
            FavoritesScreen(
                mainNavController = mainNavController,
                favoritesNavController = favoritesNavController,
                favoritesScreenState = favoritesScreenState,
                onEvent = favoritesViewModel::onEvent
            )
        }

        composable(Route.LIKED_SCREEN) {
            FavoriteListScreen(
                route = Route.LIKED_SCREEN,
                mainNavController = mainNavController,
                favoritesScreenState = favoritesScreenState,
                onEvent = favoritesViewModel::onEvent
            )
        }

        composable(Route.WATCHLIST_SCREEN) {
            FavoriteListScreen(
                route = Route.WATCHLIST_SCREEN,
                mainNavController = mainNavController,
                favoritesScreenState = favoritesScreenState,
                onEvent = favoritesViewModel::onEvent
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    mainNavController: NavController,
    favoritesNavController: NavController,
    favoritesScreenState: FavoritesScreenState,
    onEvent: (FavoriteUiEvents) -> Unit
) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    fun refresh() = refreshScope.launch {
        refreshing = true
        delay(1500)

        onEvent(FavoriteUiEvents.Refresh)
        refreshing = false
    }

    val refreshState = rememberPullRefreshState(refreshing, ::refresh)

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.favorites_and_bookmarks),
                    fontFamily = font,
                    fontSize = 20.sp
                )
            }
        }
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(refreshState)
                .padding(top = it.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                if (favoritesScreenState.likedList.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(id = R.string.favorites),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = font,
                        fontSize = 20.sp
                    )
                    Box(
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth(0.9f)
                            .padding(
                                top = 20.dp,
                                bottom = 12.dp
                            )
                            .clip(RoundedCornerShape(MediumRadius))
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    AutoSwipeSection(
                        title = stringResource(id = R.string.favorites),
                        backdropPath = true,
                        showSeeAll = true,
                        route = Route.LIKED_SCREEN,
                        navController = favoritesNavController,
                        mainNavController = mainNavController,
                        mediaList = favoritesScreenState.likedList
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                if (favoritesScreenState.watchlist.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(R.string.bookmarks),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = font,
                        fontSize = 20.sp
                    )
                    Box(
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth(0.9f)
                            .padding(
                                top = 20.dp,
                                bottom = 12.dp
                            )
                            .clip(RoundedCornerShape(MediumRadius))
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    AutoSwipeSection(
                        title = stringResource(id = R.string.bookmarks),
                        backdropPath = true,
                        showSeeAll = true,
                        route = Route.WATCHLIST_SCREEN,
                        navController = favoritesNavController,
                        mainNavController = mainNavController,
                        mediaList = favoritesScreenState.watchlist
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = refreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
            )
        }
    }
}






