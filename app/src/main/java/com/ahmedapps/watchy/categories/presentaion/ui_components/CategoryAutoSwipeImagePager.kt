package com.ahmedapps.watchy.categories.presentaion.ui_components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ahmedapps.watchy.main.domain.models.Media
import com.ahmedapps.watchy.ui.theme.Radius


import com.ahmedapps.watchy.ui.theme.font
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryAutoSwipeImagePager(
    title: String,
    route: String,
    mediaList: List<Media>,
    categoriesNavController: NavController
) {

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        mediaList.size
    }

    val scope = rememberCoroutineScope()
    val swipeIntervalMillis = 3000

    HorizontalPager(
        modifier = Modifier
            .fillMaxSize(),
        state = pagerState,
        key = { mediaList[it].mediaId },
        pageSize = PageSize.Fill
    ) { index ->

        CategoryItemImage(
            title = title,
            route = route,
            media = mediaList[index],
            categoriesNavController = categoriesNavController,
        )

        LaunchedEffect(Unit) {
            while (true) {
                delay(swipeIntervalMillis.toLong())
                scope.launch {

                    if (pagerState.canScrollForward) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        pagerState.animateScrollToPage(0)
                    }

                }
            }
        }
    }
}
