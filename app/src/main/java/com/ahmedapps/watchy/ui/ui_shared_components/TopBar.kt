package com.ahmedapps.watchy.ui.ui_shared_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.search.presentation.SearchScreenState
import com.ahmedapps.watchy.ui.theme.BigRadius
import com.ahmedapps.watchy.ui.theme.HugeRadius
import com.ahmedapps.watchy.ui.theme.font

@Composable
fun NonFocusedTopBar(
    isMainScreen: Boolean = false,
    title: String = "",
    name: String = "",
    toolbarOffsetHeightPx: Int,
    mainNavController: NavController
) {

    Box(
        modifier = Modifier
            .background(Color.Transparent)
            .height(
                if (title.isNotEmpty()) HugeRadius.dp
                else BigRadius.dp
            )
            .offset { IntOffset(x = 0, y = toolbarOffsetHeightPx) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (title.isNotEmpty()) MaterialTheme.colorScheme.background
                    else Color.Transparent
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            NonFocusedSearchBar(
                name = name,
                isMainScreen = isMainScreen,
                mainNavController = mainNavController
            )

            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                    fontSize = 19.sp,
                    maxLines = 1
                )
            }
        }

    }
}

@Composable
fun FocusedTopBar(
    toolbarOffsetHeightPx: Int,
    searchScreenState: SearchScreenState,
    onSearch: (String) -> Unit = {}
) {

    Box(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .height(BigRadius.dp)
            .offset { IntOffset(x = 0, y = toolbarOffsetHeightPx) }
    ) {
        SearchBar(
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(50.dp),
            placeholderText = stringResource(R.string.search_for_a_movie_or_tv_series),
            searchScreenState = searchScreenState
        ) {
            onSearch(it)
        }
    }
}
