package dev.anilbeesetti.nextplayer

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.anilbeesetti.nextplayer.core.common.extensions.isTelevision
import dev.anilbeesetti.nextplayer.core.common.service.system.SystemService
import dev.anilbeesetti.nextplayer.core.media.services.MediaOperationsService
import dev.anilbeesetti.nextplayer.core.model.ThemeConfig
import dev.anilbeesetti.nextplayer.core.ui.components.LocalNavigationBottomPadding
import dev.anilbeesetti.nextplayer.core.ui.components.LocalTopLevelBottomBarVisibleSetter
import dev.anilbeesetti.nextplayer.core.ui.components.LocalTopLevelFabSetter
import dev.anilbeesetti.nextplayer.core.ui.components.TopLevelFabState
import dev.anilbeesetti.nextplayer.core.ui.components.thenIf
import dev.anilbeesetti.nextplayer.core.ui.components.tvFocusRing
import dev.anilbeesetti.nextplayer.core.ui.theme.NextPlayerTheme
import dev.anilbeesetti.nextplayer.navigation.NextBottomRail
import dev.anilbeesetti.nextplayer.navigation.NextNavigationRail
import dev.anilbeesetti.nextplayer.navigation.TopLevelDestination
import dev.anilbeesetti.nextplayer.navigation.TopLevelNavState
import dev.anilbeesetti.nextplayer.navigation.mediaNavGraph
import dev.anilbeesetti.nextplayer.navigation.moreNavGraph
import dev.anilbeesetti.nextplayer.navigation.navigationTransition
import dev.anilbeesetti.nextplayer.navigation.navigationTransitionModifier
import dev.anilbeesetti.nextplayer.navigation.networkNavGraph
import dev.anilbeesetti.nextplayer.navigation.openPlaylist
import dev.anilbeesetti.nextplayer.navigation.playlistNavGraph
import dev.anilbeesetti.nextplayer.navigation.rememberTopLevelNavState
import dev.anilbeesetti.nextplayer.navigation.settingsNavGraph
import dev.anilbeesetti.nextplayer.settings.navigation.navigateToSettings
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {

    private val mediaOperationsService: MediaOperationsService by inject()

    private val systemService: SystemService by inject()

    private val viewModel: MainViewModel by viewModel()

    private var pendingPlaylistId by mutableStateOf<Long?>(value = null)
    private var darkSystemBars = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingPlaylistId = PlaylistShortcut.playlistId(intent = intent)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            pendingPlaylistId = PlaylistShortcut.playlistId(intent = intent)
        }
        systemService.initialize(activity = this@MainActivity)
        mediaOperationsService.initialize(activity = this@MainActivity)

        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value is MainActivityUiState.Loading
        }

        darkSystemBars = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkSystemBars },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkSystemBars },
            ),
        )

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val shouldUseDarkTheme = shouldUseDarkTheme(state = state)

            LaunchedEffect(key1 = shouldUseDarkTheme) {
                darkSystemBars = shouldUseDarkTheme
                WindowCompat.getInsetsController(
                    window,
                    window.decorView,
                ).apply {
                    isAppearanceLightStatusBars = !shouldUseDarkTheme
                    isAppearanceLightNavigationBars = !shouldUseDarkTheme
                }
            }

            NextPlayerTheme(
                darkTheme = shouldUseDarkTheme,
                highContrastDarkTheme = shouldUseHighContrastDarkTheme(state = state),
                dynamicColor = shouldUseDynamicTheming(state = state),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    val navState = rememberTopLevelNavState()

                    LaunchedEffect(key1 = pendingPlaylistId) {
                        pendingPlaylistId?.let { playlistId ->
                            navState.openPlaylist(playlistId = playlistId)
                            pendingPlaylistId = null
                        }
                    }

                    val mediaStack = navState.backStacks.getValue(TopLevelDestination.MEDIA.route)
                    val playlistStack = navState.backStacks.getValue(
                        TopLevelDestination.PLAYLISTS.route,
                    )
                    val networkStack = navState.backStacks.getValue(
                        TopLevelDestination.NETWORK.route,
                    )
                    val moreStack = navState.backStacks.getValue(TopLevelDestination.MORE.route)

                    val provider = entryProvider {
                        mediaNavGraph(
                            context = this@MainActivity,
                            backStack = mediaStack,
                        )
                        playlistNavGraph(
                            context = this@MainActivity,
                            backStack = playlistStack,
                        )
                        networkNavGraph(
                            context = this@MainActivity,
                            backStack = networkStack,
                        )
                        moreNavGraph(
                            context = this@MainActivity,
                            backStack = moreStack,
                        )
                        settingsNavGraph(backStack = navState.currentStack)
                    }

                    val topLevelFabStates =
                        remember { mutableStateMapOf<String, TopLevelFabState>() }
                    var showTopLevelBottomBar by remember { mutableStateOf(value = true) }
                    NavigationLayout(
                        state = navState,
                        fabStates = topLevelFabStates,
                        showBottomBar = showTopLevelBottomBar,
                    ) { layoutPaddingValues ->
                        val railPadding = layoutPaddingValues.calculateStartPadding(
                            LocalLayoutDirection.current,
                        )
                        val navigationInsetsDecorator = remember(
                            navState,
                            railPadding,
                        ) {
                            NavEntryDecorator<NavKey> { entry ->
                                // Reserve rail space per entry without resizing the animated display.
                                Box(
                                    navState.navigationTransitionModifier(entry).thenIf(
                                        navState.topLevelContentKeys.contains(entry.contentKey),
                                    ) {
                                        padding(start = railPadding)
                                            .consumeWindowInsets(
                                                WindowInsets.displayCutout.only(
                                                    WindowInsetsSides.Start,
                                                ),
                                            )
                                    },
                                ) { entry.Content() }
                            }
                        }
                        CompositionLocalProvider(
                            LocalNavigationBottomPadding provides
                                layoutPaddingValues.calculateBottomPadding(),
                            LocalTopLevelBottomBarVisibleSetter provides
                                { showTopLevelBottomBar = it },
                            LocalTopLevelFabSetter provides { key, state ->
                                if (state == null) {
                                    topLevelFabStates.remove(key = key)
                                } else {
                                    topLevelFabStates[key] = state
                                }
                            },
                        ) {
                            NavDisplay(
                                entries = navState.rememberEntries(
                                    entryProvider = provider,
                                    entryDecorator = navigationInsetsDecorator,
                                ),
                                onBack = { navState.goBack() },
                                transitionSpec = {
                                    navState.navigationTransition(
                                        initialState = initialState,
                                        targetState = targetState,
                                    )
                                },
                                popTransitionSpec = {
                                    navState.navigationTransition(
                                        initialState = initialState,
                                        targetState = targetState,
                                    )
                                },
                                predictivePopTransitionSpec = {
                                    navState.navigationTransition(
                                        initialState = initialState,
                                        targetState = targetState,
                                        isPredictive = true,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationLayout(
    modifier: Modifier = Modifier,
    state: TopLevelNavState,
    fabStates: SnapshotStateMap<String, TopLevelFabState>,
    showBottomBar: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    val isTv = LocalContext.current.isTelevision
    val configuration = LocalConfiguration.current
    val contentFocusRequester = remember { FocusRequester() }
    val fabFocusRequester = remember { FocusRequester() }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useNavRail = configuration.screenWidthDp >= 600 && isLandscape
    val showNavigation =
        state.currentStack.lastOrNull()?.let { state.topLevelContentKeys.contains(element = it) } ==
            true
    val navVisible = showNavigation && showBottomBar
    val railWidth = 96.dp
    val selectedFabState = state.destinations[state.selectedIndex].fabKey?.let(
        block = fabStates::get,
    )
    var displayedFabState by remember { mutableStateOf<TopLevelFabState?>(value = null) }
    LaunchedEffect(key1 = selectedFabState) { selectedFabState?.let { displayedFabState = it } }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .thenIf(condition = isTv) {
                    focusRequester(focusRequester = contentFocusRequester)
                        .focusProperties {
                            onExit = {
                                if (requestedFocusDirection == FocusDirection.Down &&
                                    navVisible &&
                                    displayedFabState != null
                                ) {
                                    fabFocusRequester.requestFocus()
                                }
                            }
                        }
                        .focusGroup()
                },
        ) {
            content(
                PaddingValues(
                    start = if (useNavRail && showBottomBar) railWidth else 0.dp,
                    bottom = if (!useNavRail && showBottomBar) 64.dp else 0.dp,
                ),
            )
        }

        AnimatedVisibility(
            visible = navVisible && useNavRail,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            NextNavigationRail(
                state = state,
                railWidth = railWidth,
                onSettingsClick = { state.currentStack.navigateToSettings() },
            )
        }

        AnimatedVisibility(
            visible = navVisible && !useNavRail,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            NextBottomRail(
                state = state,
            )
        }

        AnimatedVisibility(
            visible = navVisible && displayedFabState != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(
                    end = if (useNavRail) 24.dp else 16.dp,
                    bottom = if (useNavRail) 24.dp else (64.dp + 16.dp),
                ),
        ) {
            displayedFabState?.let { fab ->
                FloatingActionButton(
                    onClick = fab.onClick,
                    modifier = Modifier
                        .testTag(tag = "top_level_fab")
                        .focusRequester(focusRequester = fabFocusRequester)
                        .tvFocusRing(shape = MaterialTheme.shapes.large)
                        .focusProperties {
                            if (isTv) {
                                up = fab.upFocusRequester.takeUnless { it == FocusRequester.Default }
                                    ?: contentFocusRequester
                            }
                        },
                    shape = MaterialTheme.shapes.large,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    AnimatedContent(
                        targetState = fab,
                        transitionSpec = {
                            fadeIn(
                                animationSpec = tween(
                                    durationMillis = 250,
                                    easing = FastOutSlowInEasing,
                                ),
                            ) +
                                scaleIn(
                                    animationSpec = tween(
                                        durationMillis = 250,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    initialScale = 0.25f,
                                ) togetherWith
                                fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 250,
                                        easing = FastOutSlowInEasing,
                                    ),
                                ) +
                                scaleOut(
                                    animationSpec = tween(
                                        durationMillis = 250,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    targetScale = 0.25f,
                                )
                        },
                        label = "fab_icon_swap",
                    ) { targetFab ->
                        Icon(
                            imageVector = targetFab.icon,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Returns `true` if dark theme should be used, as a function of the [state] and the
 * current system context.
 */
@Composable
fun shouldUseDarkTheme(state: MainActivityUiState): Boolean = when (state) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()

    is MainActivityUiState.Success -> when (state.preferences.themeConfig) {
        ThemeConfig.SYSTEM -> isSystemInDarkTheme()
        ThemeConfig.OFF -> false
        ThemeConfig.ON -> true
    }
}

@Composable
fun shouldUseHighContrastDarkTheme(state: MainActivityUiState): Boolean = when (state) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> state.preferences.useHighContrastDarkTheme
}

/**
 * Returns `true` if the dynamic color is disabled, as a function of the [state].
 */
@Composable
fun shouldUseDynamicTheming(state: MainActivityUiState): Boolean = when (state) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> state.preferences.useDynamicColors
}
