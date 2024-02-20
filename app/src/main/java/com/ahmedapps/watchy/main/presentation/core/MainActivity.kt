package com.ahmedapps.watchy.main.presentation.core

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ahmedapps.watchy.R
import com.ahmedapps.watchy.auth.presentation.signin.SignInScreen
import com.ahmedapps.watchy.auth.presentation.signup.SignUpScreen
import com.ahmedapps.watchy.auth.util.AuthResult
import com.ahmedapps.watchy.details.presentation.details.CoreDetailScreen
import com.ahmedapps.watchy.favorites.presentation.CoreFavoriteScreen
import com.ahmedapps.watchy.main.presentation.main.MainScreen
import com.ahmedapps.watchy.main.presentation.main.MainUiEvents
import com.ahmedapps.watchy.main.presentation.main.MainViewModel
import com.ahmedapps.watchy.main.presentation.media_list.MediaListScreen
import com.ahmedapps.watchy.profile.presentation.ProfileScreen
import com.ahmedapps.watchy.search.presentation.SearchScreen
import com.ahmedapps.watchy.ui.theme.TheMoviesTheme
import com.ahmedapps.watchy.util.Route
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val coreViewModel by viewModels<CoreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TheMoviesTheme {

                SetBarColor(color = MaterialTheme.colorScheme.background)

                installSplashScreen().apply {
                    setKeepOnScreenCondition {
                        coreViewModel.isLoading.value
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }

            }
        }
    }

    @Composable
    fun CoreScreen(
        onAuthorized: () -> Unit,
        onNotAuthorized: () -> Unit,
    ) {

        LaunchedEffect(coreViewModel.authResultChannel, coreViewModel.isLoading) {
            coreViewModel.authResultChannel.collectLatest { result ->
                when (result) {
                    is AuthResult.Authorized -> {
                        onAuthorized()
                    }

                    is AuthResult.Unauthorized -> {
                        onNotAuthorized()
                    }

                    is AuthResult.UnknownError -> {
                        onNotAuthorized()
                    }

                    is AuthResult.SingedOut -> {
                        onNotAuthorized()
                    }
                }
            }
        }
    }

    @Composable
    fun MainNavigation() {

        val context = LocalContext.current

        val mainViewModel = hiltViewModel<MainViewModel>()
        val mainUiState = mainViewModel.mainUiState.collectAsState().value

        val mainNavController = rememberNavController()

        NavHost(
            navController = mainNavController,
            startDestination = Route.CORE_SCREEN
        ) {

            composable(Route.CORE_SCREEN) {
                CoreScreen(
                    onAuthorized = {
                        mainViewModel.onEvent(MainUiEvents.LoadAll)
                        mainNavController.popBackStack()
                        mainNavController.navigate(Route.MAIN_SCREEN)
                    },

                    onNotAuthorized = {
                        mainNavController.popBackStack()
                        mainNavController.navigate(Route.SIGNIN_SCREEN)
                    }
                )
            }

            composable(Route.SIGNUP_SCREEN) {
                SignUpScreen(
                    onAuthorized = {
                        mainViewModel.onEvent(MainUiEvents.LoadAll)
                        mainNavController.popBackStack()
                        mainNavController.navigate(Route.MAIN_SCREEN)
                    },
                    onSignInClick = {
                        mainNavController.popBackStack()
                        mainNavController.navigate(Route.SIGNIN_SCREEN)
                    }
                )
            }

            composable(Route.SIGNIN_SCREEN) {
                SignInScreen(
                    activity = this@MainActivity,
                    onAuthorized = {
                        mainViewModel.onEvent(MainUiEvents.LoadAll)
                        mainNavController.popBackStack()
                        mainNavController.navigate(Route.MAIN_SCREEN)
                    },
                    onSignUpClick = {
                        mainNavController.popBackStack()
                        mainNavController.navigate(Route.SIGNUP_SCREEN)
                    }
                )
            }

            composable(Route.MAIN_SCREEN) {
                MainScreen(
                    mainNavController = mainNavController,
                    mainUiState = mainUiState,
                    onEvent = mainViewModel::onEvent
                )
            }

            composable(Route.TRENDING_NOW_SCREEN) {
                MediaListScreen(
                    mainNavController = mainNavController,
                    route = Route.TRENDING_NOW_SCREEN,
                    mainUiState = mainUiState,
                    onEvent = mainViewModel::onEvent
                )
            }

            composable(Route.TV_SERIES_SCREEN) {
                MediaListScreen(
                    mainNavController = mainNavController,
                    route = Route.TV_SERIES_SCREEN,
                    mainUiState = mainUiState,
                    onEvent = mainViewModel::onEvent
                )
            }

            composable(Route.MOVIES_SCREEN) {
                MediaListScreen(
                    mainNavController = mainNavController,
                    route = Route.MOVIES_SCREEN,
                    mainUiState = mainUiState,
                    onEvent = mainViewModel::onEvent
                )
            }

            composable(Route.CORE_FAVORITES_SCREEN) {
                CoreFavoriteScreen(
                    mainNavController = mainNavController,
                    mainUiState = mainUiState
                )
            }

            composable(Route.PROFILE_SCREEN) {
                ProfileScreen {
                    Toast.makeText(
                        context, context.getString(R.string.singed_out), Toast.LENGTH_SHORT
                    ).show()
                    mainNavController.popBackStack()
                    mainNavController.popBackStack()
                    mainNavController.navigate(Route.SIGNIN_SCREEN)
                }
            }

            composable(Route.SEARCH_SCREEN) {
                SearchScreen(
                    mainNavController = mainNavController,
                    mainUiState = mainUiState,
                )
            }

            composable(
                "${Route.CORE_DETAILS_SCREEN}?id={id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType }
                )
            ) {

                val id = it.arguments?.getInt("id") ?: 0
                CoreDetailScreen(
                    id = id,
                    mainUiState = mainUiState
                )
            }
        }
    }

    @Composable
    private fun SetBarColor(color: Color) {
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(key1 = color) {
            systemUiController.setSystemBarsColor(color)
        }
    }

}











