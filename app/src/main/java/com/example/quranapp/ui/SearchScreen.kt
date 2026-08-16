package com.example.quranapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    onOpenSurah: (Int) -> Unit
) {
    val state by viewModel.search.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جستجو") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = {
                    viewModel.updateQuery(it)
                    viewModel.runSearch()
                },
                label = { Text("جستجو در قرآن، ترجمه و تفسیر البرهان") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row {
                FilterChip(
                    selected = state.includeQuran,
                    onClick = { viewModel.toggleFilter("quran"); viewModel.runSearch() },
                    label = { Text("متن قرآن") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = state.includeTranslation,
                    onClick = { viewModel.toggleFilter("translation"); viewModel.runSearch() },
                    label = { Text("ترجمه انصاریان") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = state.includeTafsir,
                    onClick = { viewModel.toggleFilter("tafsir"); viewModel.runSearch() },
                    label = { Text("تفسیر البرهان") }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            LazyColumn {
                items(state.results) { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onOpenSurah(result.surahNumber) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "${result.surahNameFa} - آیه ${result.ayahNumber}  [" +
                                    kindLabel(result.kind) + "]",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(result.snippet, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun kindLabel(kind: String) = when (kind) {
    "quran" -> "متن قرآن"
    "translation" -> "ترجمه"
    "tafsir" -> "تفسیر"
    else -> kind
}
