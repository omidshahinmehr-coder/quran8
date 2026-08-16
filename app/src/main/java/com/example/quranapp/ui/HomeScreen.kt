package com.example.quranapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranapp.data.ReadingItem
import com.example.quranapp.ui.theme.quranFontByKey
import com.example.quranapp.ui.theme.translationFontByKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    onOpenSurahPicker: () -> Unit,
    onOpenJuzPicker: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenTafsirBrowse: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTafsir: (globalAyahId: Int, surahName: String, ayahNumber: Int) -> Unit
) {
    val state by viewModel.fullQuran.collectAsState()
    val scrollTarget by viewModel.scrollTarget.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showHint by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadFullQuran()
    }

    // بازگشت از صفحه تفسیر: اسکرول به همان آیه‌ای که تفسیرش باز شده بود
    LaunchedEffect(state.items.size) {
        if (state.items.isNotEmpty()) {
            viewModel.consumePendingReturnAyah()?.let { ayahId ->
                viewModel.itemIndexForAyah(ayahId)?.let { index ->
                    listState.scrollToItem(index)
                }
            }
        }
    }

    // انتخاب سوره/جزء از منو، حتی وقتی صفحه اصلی از قبل باز است
    LaunchedEffect(scrollTarget) {
        scrollTarget?.let { index ->
            listState.scrollToItem(index)
            viewModel.consumeScrollTarget()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "قرآن کریم",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("فهرست سوره‌ها") },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenSurahPicker() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("فهرست اجزاء") },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenJuzPicker() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "زبان ترجمه",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    FilterChip(
                        selected = settings.translationLanguage == "fa",
                        onClick = { viewModel.updateSettings(settings.copy(translationLanguage = "fa")) },
                        label = { Text("فارسی") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = settings.translationLanguage == "en",
                        onClick = { viewModel.updateSettings(settings.copy(translationLanguage = "en")) },
                        label = { Text("English") }
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("تفسیر البرهان") },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenTafsirBrowse() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("تنظیمات نمایش") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenSettings() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("درباره برنامه") },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenAbout() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("قرآن کریم") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "منو")
                        }
                    },
                    actions = {
                        Text(
                            "ترجمه",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = state.showTranslation,
                            onCheckedChange = { viewModel.toggleFullQuranTranslationVisible() }
                        )
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Default.Search, contentDescription = "جستجو")
                        }
                    }
                )
            }
        ) { padding ->
            if (state.loading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
            ) {
                if (showHint) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "برای مشاهده تفسیر البرهان، روی آیه مورد نظر نگه دارید (لمس طولانی). برای رفتن به سوره یا جزء دیگر از منو استفاده کنید.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showHint = false }) {
                                    Text("×", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }

                items(state.items) { item ->
                    when (item) {
                        is ReadingItem.SurahHeader -> {
                            Text(
                                item.surahNameFa,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 4.dp)
                            )
                        }
                        is ReadingItem.Bismillah -> {
                            Text(
                                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontFamily = quranFontByKey(settings.quranFontKey),
                                fontSize = settings.quranFontSize.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )
                        }
                        is ReadingItem.Ayah -> {
                            val ayah = item.ayah
                            val hasTafsir = ayah.globalAyahId in state.ayahIdsWithTafsir
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            if (hasTafsir) {
                                                viewModel.rememberReturnAyah(ayah.globalAyahId)
                                                onOpenTafsir(ayah.globalAyahId, ayah.surahNameFa, ayah.ayahNumber)
                                            }
                                        }
                                    )
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        "${ayah.textArabic}  (${ayah.ayahNumber})",
                                        fontFamily = quranFontByKey(settings.quranFontKey),
                                        fontSize = settings.quranFontSize.sp,
                                        lineHeight = (settings.quranFontSize * 1.9).sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (state.showTranslation) {
                                        state.translations[ayah.globalAyahId]?.let { tr ->
                                            Spacer(Modifier.height(8.dp))
                                            val isEnglish = settings.translationLanguage == "en"
                                            Text(
                                                tr.textFa,
                                                fontFamily = if (isEnglish) null else translationFontByKey(settings.translationFontKey),
                                                fontSize = settings.translationFontSize.sp,
                                                lineHeight = (settings.translationFontSize * 1.6).sp,
                                                textAlign = if (isEnglish) TextAlign.Left else TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
