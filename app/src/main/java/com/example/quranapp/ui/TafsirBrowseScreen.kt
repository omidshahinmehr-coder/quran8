package com.example.quranapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranapp.ui.theme.translationFontByKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirBrowseScreen(
    viewModel: QuranViewModel,
    surahNumber: Int,
    onBack: () -> Unit
) {
    val state by viewModel.tafsirBrowse.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(surahNumber) {
        viewModel.loadTafsirBrowse(surahNumber)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.surahName.ifBlank { "تفسیر" }) },
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
                    onClick = { viewModel.setTafsirBrowseLanguage("ar") },
                    text = { Text("عربی") }
                )
                Tab(
                    selected = state.language == "fa",
                    onClick = { viewModel.setTafsirBrowseLanguage("fa") },
                    text = { Text("فارسی") }
                )
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateTafsirBrowseQuery(it) },
                label = { Text("جستجو در متن تفسیر این سوره") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            val results = state.filteredEntries
            if (results.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (state.searchQuery.isBlank()) "تفسیری برای این سوره ثبت نشده است."
                        else "عبارتی یافت نشد."
                    )
                }
                return@Column
            }

            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(results) { entry ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                if (entry.startAyahId == entry.endAyahId)
                                    "آیه مرتبط: ${entry.startAyahId}"
                                else "آیات مرتبط: ${entry.startAyahId} تا ${entry.endAyahId}",
                                style = MaterialTheme.typography.labelMedium
                            )
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
