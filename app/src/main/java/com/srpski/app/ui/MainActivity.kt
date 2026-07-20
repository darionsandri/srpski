package com.srpski.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.srpski.app.SrpskiApp
import com.srpski.app.ui.theme.SrpskiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SrpskiApp
        setContent {
            SrpskiTheme {
                AppNav(app)
            }
        }
    }
}

@Composable
fun AppNav(app: SrpskiApp) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "tree") {
        composable("tree") {
            SkillTreeScreen(app) { lessonId -> nav.navigate("lesson/$lessonId") }
        }
        composable("lesson/{lessonId}") { backStack ->
            val lessonId = backStack.arguments?.getString("lessonId") ?: return@composable
            val vm: LessonViewModel = viewModel(
                key = lessonId,
                factory = lessonViewModelFactory(app, lessonId)
            )
            LessonScreen(viewModel = vm, onExit = { nav.popBackStack() })
        }
    }
}

private fun lessonViewModelFactory(app: SrpskiApp, lessonId: String) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LessonViewModel(app, lessonId) as T
    }
