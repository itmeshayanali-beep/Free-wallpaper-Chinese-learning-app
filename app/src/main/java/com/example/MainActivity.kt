package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MockupsScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen(
    val titleRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val tag: String
) {
    STUDIO(R.string.nav_studio, Icons.Filled.Wallpaper, Icons.Outlined.Wallpaper, "nav_studio"),
    THEMES(R.string.nav_themes, Icons.Filled.Palette, Icons.Outlined.Palette, "nav_themes"),
    LIBRARY(R.string.nav_library, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "nav_library"),
    PRACTICE(R.string.nav_practice, Icons.Filled.School, Icons.Outlined.School, "nav_practice"),
    MOCKUPS(R.string.nav_mockups, Icons.Filled.DesignServices, Icons.Outlined.DesignServices, "nav_mockups")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity

    val currentUser by viewModel.currentUser.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.STUDIO) }

    BackHandler(enabled = currentScreen != AppScreen.STUDIO) {
        currentScreen = AppScreen.STUDIO
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = currentScreen.titleRes),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = cloudSyncStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (currentUser == null) {
                        FilledTonalButton(
                            onClick = {
                                activity?.let { act ->
                                    viewModel.signInWithGoogle(act) { _, _ -> }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("google_sign_in_top_button")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable { showMenu = true }
                                    .testTag("user_avatar_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(currentUser?.email ?: "Signed in") },
                                    onClick = { },
                                    enabled = false
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Sign Out") },
                                    onClick = {
                                        showMenu = false
                                        activity?.let { act -> viewModel.signOut(act) }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AppScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                contentDescription = stringResource(id = screen.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = screen.titleRes),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.STUDIO -> StudioScreen(viewModel = viewModel)
                    AppScreen.THEMES -> ThemesScreen(viewModel = viewModel)
                    AppScreen.LIBRARY -> LibraryScreen(
                        viewModel = viewModel,
                        onNavigateToStudio = { currentScreen = AppScreen.STUDIO }
                    )
                    AppScreen.PRACTICE -> PracticeScreen(viewModel = viewModel)
                    AppScreen.MOCKUPS -> MockupsScreen()
                }
            }
        }
    }
}
