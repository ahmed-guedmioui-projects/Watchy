package com.ahmedapps.watchy.main.presentation.main

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.main.presentation.main.ui_componentes.MediaHomeScreenSection
import com.ahmedapps.watchy.ui.theme.BigRadius
import com.ahmedapps.watchy.ui.theme.MediumRadius
import com.ahmedapps.watchy.ui.theme.font
import com.ahmedapps.watchy.ui.ui_shared_components.AutoSwipeSection
import com.ahmedapps.watchy.ui.ui_shared_components.NonFocusedTopBar
import com.ahmedapps.watchy.util.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainNavController: NavController,
    mainUiState: MainUiState,
    onEvent: (MainUiEvents) -> Unit
) {

    Scaffold(
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = {
                        mainNavController.navigate(Route.CORE_FAVORITES_SCREEN)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmarks,
                        contentDescription = stringResource(R.string.categories)
                    )
                }
            }
        }
    ) { paddingValues ->
        val padding = paddingValues

        val toolbarHeightPx = with(LocalDensity.current) { BigRadius.dp.roundToPx().toFloat() }
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

        val refreshScope = rememberCoroutineScope()
        var refreshing by remember { mutableStateOf(false) }

        fun refresh() = refreshScope.launch {
            refreshing = true
            delay(1500)

            onEvent(MainUiEvents.Refresh(route = Route.MAIN_SCREEN))

            refreshing = false
        }

        val refreshState = rememberPullRefreshState(refreshing, ::refresh)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .pullRefresh(refreshState),
            contentAlignment = Alignment.BottomCenter
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = BigRadius.dp),
            ) {

                MediaHomeScreenSection(
                    route = Route.TRENDING_NOW_SCREEN,
                    mainNavController = mainNavController,
                    mainUiState = mainUiState
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (mainUiState.specialList.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(id = R.string.special),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = font,
                        fontSize = 20.sp
                    )
                    Box(
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth(0.9f)
                            .padding(top = 20.dp, bottom = 12.dp)
                            .clip(RoundedCornerShape(MediumRadius))
                            .align(CenterHorizontally)
                    )
                } else {
                    AutoSwipeSection(
                        title = stringResource(id = R.string.special),
                        backdropPath = true,
                        mainNavController = mainNavController,
                        mediaList = mainUiState.specialList
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                MediaHomeScreenSection(
                    route = Route.TV_SERIES_SCREEN,
                    mainNavController = mainNavController,
                    mainUiState = mainUiState
                )

                Spacer(modifier = Modifier.height(16.dp))

                MediaHomeScreenSection(
                    route = Route.MOVIES_SCREEN,
                    mainNavController = mainNavController,
                    mainUiState = mainUiState
                )

                Spacer(modifier = Modifier.height(80.dp))
            }

            PullRefreshIndicator(
                refreshing,
                refreshState,
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = (BigRadius - 8).dp)
            )
        }

        NonFocusedTopBar(
            isMainScreen = true,
            name = mainUiState.name.take(1),
            toolbarOffsetHeightPx = toolbarOffsetHeightPx.floatValue.roundToInt(),
            mainNavController = mainNavController,
        )

    }
}












