package com.srpski.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.srpski.app.SrpskiApp
import com.srpski.app.ui.achievements.AchievementsScreen
import com.srpski.app.ui.onboarding.InterestsEditorScreen
import com.srpski.app.ui.onboarding.OnboardingScreen
import com.srpski.app.ui.packs.PacksScreen
import com.srpski.app.ui.share.ShareIngestScreen
import com.srpski.app.ui.share.ShareIngestViewModel
import com.srpski.app.ui.theme.SrpskiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var pendingShareText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingShareText = extractShareText(intent)
        val app = application as SrpskiApp
        setContent {
            SrpskiTheme {
                AppNav(app, pendingShareText, onShareConsumed = { pendingShareText = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingShareText = extractShareText(intent)
    }

    private fun extractShareText(intent: Intent?): String? {
        if (intent == null || intent.action != Intent.ACTION_SEND) return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!text.isNullOrBlank()) return text.trim()
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        return uri?.toString()
    }
}

@Composable
fun AppNav(
    app: SrpskiApp,
    initialShareText: String?,
    onShareConsumed: () -> Unit
) {
    val nav = rememberNavController()
    val onboardingDone by app.userPrefs.onboardingComplete.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    LaunchedEffect(onboardingDone) {
        // Route first-launch users to onboarding before anything else runs.
        val done = onboardingDone ?: return@LaunchedEffect
        if (!done && nav.currentDestination?.route != "onboarding") {
            nav.navigate("onboarding") {
                popUpTo("tree") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(initialShareText, onboardingDone) {
        val text = initialShareText ?: return@LaunchedEffect
        if (onboardingDone != true) return@LaunchedEffect
        val encoded = Uri.encode(text)
        nav.navigate("share?text=$encoded")
        onShareConsumed()
    }

    NavHost(navController = nav, startDestination = "tree") {
        composable("tree") {
            SkillTreeScreen(
                app = app,
                onOpenLesson = { lessonId -> nav.navigate("lesson/$lessonId") },
                onOpenInterests = { nav.navigate("interests") },
                onOpenPacks = { nav.navigate("packs") },
                onOpenAchievements = { nav.navigate("achievements") }
            )
        }
        composable("lesson/{lessonId}") { backStack ->
            val lessonId = backStack.arguments?.getString("lessonId") ?: return@composable
            val vm: LessonViewModel = viewModel(
                key = lessonId,
                factory = lessonViewModelFactory(app, lessonId)
            )
            LessonScreen(viewModel = vm, onExit = { nav.popBackStack() })
        }
        composable("share?text={text}") { backStack ->
            val text = backStack.arguments?.getString("text").orEmpty()
            val vm: ShareIngestViewModel = viewModel(
                key = "share-$text",
                factory = shareIngestFactory(app)
            )
            ShareIngestScreen(viewModel = vm, rawText = text, onExit = { nav.popBackStack() })
        }
        composable("onboarding") {
            OnboardingScreen(
                topics = app.topicMap.topics,
                onFinish = { picked ->
                    scope.launch {
                        app.interests.seed(app.topicMap, picked)
                        app.userPrefs.markOnboardingComplete()
                        goToTree(nav)
                    }
                }
            )
        }
        composable("interests") {
            InterestsEditorScreen(app = app, onExit = { nav.popBackStack() })
        }
        composable("packs") {
            PacksScreen(app = app, onExit = { nav.popBackStack() })
        }
        composable("achievements") {
            AchievementsScreen(app = app, onExit = { nav.popBackStack() })
        }
    }
}

private fun goToTree(nav: NavHostController) {
    nav.navigate("tree") {
        popUpTo("tree") { inclusive = true }
        launchSingleTop = true
    }
}

private fun lessonViewModelFactory(app: SrpskiApp, lessonId: String) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LessonViewModel(app, lessonId) as T
    }

private fun shareIngestFactory(app: SrpskiApp) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShareIngestViewModel(app, app.sharedMedia) as T
    }
