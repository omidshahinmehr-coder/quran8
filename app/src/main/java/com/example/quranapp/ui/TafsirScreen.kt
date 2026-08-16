package com.example.quranapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranapp.ui.theme.translationFontByKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirScreen(
    viewModel: QuranViewModel,
    globalAyahId: Int,
    surahName: String,
    ayahNumber: Int,
    onBack: () -> Unit
) {
    val state by viewModel.tafsir.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(globalAyahId) {
        viewModel.loadTafsir(globalAyahId, surahName, ayahNumber)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفسیر البرهان — $surahName، آیه $ayahNumber") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = if (state.language == "fa") 1 else 0) {
                Tab(
                    selected = state.language == "ar",
                    onClick = { viewModel.setTafsirLanguage("ar") },
                    text = { Text("عربی") }
                )
                Tab(
                    selected = state.language == "fa",
                    onClick = { viewModel.setTafsirLanguage("fa") },
                    text = { Text("فارسی") }
                )
            }

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (state.entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("تفسیری برای این آیه ثبت نشده است.")
                }
                return@Column
            }

            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(state.entries) { entry ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(entry.source, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                entry.textFa,
                                fontFamily = translationFontByKey(settings.translationFontKey),
                                fontSize = settings.translationFontSize.sp,
                                lineHeight = (settings.translationFontSize * 1.6).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
