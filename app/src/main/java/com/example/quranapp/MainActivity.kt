package com.example.quranapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quranapp.data.QuranRepository
import com.example.quranapp.data.SettingsRepository
import com.example.quranapp.ui.AboutScreen
import com.example.quranapp.ui.HomeScreen
import com.example.quranapp.ui.JuzPickerScreen
import com.example.quranapp.ui.QuranViewModel
import com.example.quranapp.ui.SearchScreen
import com.example.quranapp.ui.SettingsScreen
import com.example.quranapp.ui.SurahPickerScreen
import com.example.quranapp.ui.TafsirBrowseScreen
import com.example.quranapp.ui.TafsirScreen
import com.example.quranapp.ui.TafsirSurahListScreen
import com.example.quranapp.ui.theme.AppTypography
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(typography = AppTypography) {
                Surface(modifier = Modifier) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        QuranApp()
                    }
                }
            }
        }
    }
}

@Composable
fun QuranApp() {
    val context = LocalContext.current
    val repo = remember { QuranRepository(context.applicationContext) }
    val settingsRepo = remember { SettingsRepository(context.applicationContext) }
    val viewModel: QuranViewModel = viewModel(factory = viewModelFactory(repo, settingsRepo))
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onOpenSurahPicker = { navController.navigate("surahPicker") },
                onOpenJuzPicker = { navController.navigate("juzPicker") },
                onOpenSearch = { navController.navigate("search") },
                onOpenTafsirBrowse = { navController.navigate("tafsirSurahList") },
                onOpenSettings = { navController.navigate("settings") },
                onOpenAbout = { navController.navigate("about") },
                onOpenTafsir = { globalAyahId, surahName, ayahNumber ->
                    val encodedName = URLEncoder.encode(surahName, "UTF-8")
                    navController.navigate("tafsir/$globalAyahId/$encodedName/$ayahNumber")
                }
            )
        }

        composable("surahPicker") {
            SurahPickerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSurahSelected = { surahNumber ->
                    viewModel.requestScrollToSurah(surahNumber)
                    navController.popBackStack()
                }
            )
        }

        composable("juzPicker") {
            JuzPickerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onJuzSelected = { juzNumber ->
                    viewModel.requestScrollToJuz(juzNumber)
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable("tafsirSurahList") {
            TafsirSurahListScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenTafsirForSurah = { surahNumber -> navController.navigate("tafsirBrowse/$surahNumber") }
            )
        }

        composable(
            "tafsirBrowse/{surahNumber}",
            arguments = listOf(navArgument("surahNumber") { type = NavType.IntType })
        ) { backStackEntry ->
            val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
            TafsirBrowseScreen(
                viewModel = viewModel,
                surahNumber = surahNumber,
                onBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenSurah = { surahNumber ->
                    viewModel.requestScrollToSurah(surahNumber)
                    navController.popBackStack()
                }
            )
        }

        composable(
            "tafsir/{globalAyahId}/{surahName}/{ayahNumber}",
            arguments = listOf(
                navArgument("globalAyahId") { type = NavType.IntType },
                navArgument("surahName") { type = NavType.StringType },
                navArgument("ayahNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val globalAyahId = backStackEntry.arguments?.getInt("globalAyahId") ?: 0
            val surahName = URLDecoder.decode(backStackEntry.arguments?.getString("surahName") ?: "", "UTF-8")
            val ayahNumber = backStackEntry.arguments?.getInt("ayahNumber") ?: 0
            TafsirScreen(
                viewModel = viewModel,
                globalAyahId = globalAyahId,
                surahName = surahName,
                ayahNumber = ayahNumber,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun viewModelFactory(repo: QuranRepository, settingsRepo: SettingsRepository) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuranViewModel(repo, settingsRepo) as T
        }
    }
