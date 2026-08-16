package com.example.quranapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranapp.ui.theme.QuranFontOptions
import com.example.quranapp.ui.theme.TranslationFontOptions
import com.example.quranapp.ui.theme.quranFontByKey
import com.example.quranapp.ui.theme.translationFontByKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات نمایش") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("متن قرآن", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            FontDropdown(
                options = QuranFontOptions,
                selectedKey = settings.quranFontKey,
                onSelect = { viewModel.updateSettings(settings.copy(quranFontKey = it)) }
            )

            Spacer(Modifier.height(12.dp))
            Text("اندازه قلم: ${settings.quranFontSize.toInt()}")
            Slider(
                value = settings.quranFontSize,
                onValueChange = { viewModel.updateSettings(settings.copy(quranFontSize = it)) },
                valueRange = 16f..40f,
                steps = 11
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontFamily = quranFontByKey(settings.quranFontKey),
                fontSize = settings.quranFontSize.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            Text("متن ترجمه و تفسیر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            FontDropdown(
                options = TranslationFontOptions,
                selectedKey = settings.translationFontKey,
                onSelect = { viewModel.updateSettings(settings.copy(translationFontKey = it)) }
            )

            Spacer(Modifier.height(12.dp))
            Text("اندازه قلم: ${settings.translationFontSize.toInt()}")
            Slider(
                value = settings.translationFontSize,
                onValueChange = { viewModel.updateSettings(settings.copy(translationFontSize = it)) },
                valueRange = 12f..28f,
                steps = 15
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "این متن نمونه‌ای از اندازه و فونت انتخابی برای ترجمه و تفسیر است.",
                fontFamily = translationFontByKey(settings.translationFontKey),
                fontSize = settings.translationFontSize.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontDropdown(
    options: List<com.example.quranapp.ui.theme.FontOption>,
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.key == selectedKey }?.label ?: options.first().label

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("فونت") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, fontFamily = option.family) },
                    onClick = {
                        onSelect(option.key)
                        expanded = false
                    }
                )
            }
        }
    }
}
