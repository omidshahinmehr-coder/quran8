package com.example.quranapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.AppSettings
import com.example.quranapp.data.AyahEntity
import com.example.quranapp.data.JuzInfo
import com.example.quranapp.data.QuranRepository
import com.example.quranapp.data.ReadingItem
import com.example.quranapp.data.SearchResult
import com.example.quranapp.data.SettingsRepository
import com.example.quranapp.data.SurahInfo
import com.example.quranapp.data.TafsirEntity
import com.example.quranapp.data.TranslationEntity
import com.example.quranapp.data.TRANSLATOR_EN
import com.example.quranapp.data.TRANSLATOR_FA
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val SURAHS_WITHOUT_SEPARATE_BISMILLAH_VM = setOf(1, 9)

data class SurahListUiState(
    val surahs: List<SurahInfo> = emptyList(),
    val loading: Boolean = true
)

data class FullQuranUiState(
    val items: List<ReadingItem> = emptyList(),
    val translations: Map<Int, TranslationEntity> = emptyMap(),
    val ayahIdsWithTafsir: Set<Int> = emptySet(),
    val ayahItemIndex: Map<Int, Int> = emptyMap(), // globalAyahId -> index in items
    val surahItemIndex: Map<Int, Int> = emptyMap(), // surahNumber -> index of its header
    val juzAyahIndex: Map<Int, Int> = emptyMap(), // juzNumber -> index of its first ayah
    val showTranslation: Boolean = true,
    val loading: Boolean = true
)

data class JuzListUiState(
    val juzList: List<JuzInfo> = emptyList(),
    val loading: Boolean = true
)

data class JuzUiState(
    val juzNumber: Int = 0,
    val ayat: List<AyahEntity> = emptyList(),
    val translations: Map<Int, TranslationEntity> = emptyMap(),
    val ayahIdsWithTafsir: Set<Int> = emptySet(),
    val showTranslation: Boolean = true,
    val loading: Boolean = true
)

data class SurahUiState(
    val surahNumber: Int = 0,
    val surahName: String = "",
    val ayat: List<AyahEntity> = emptyList(),
    val translations: Map<Int, TranslationEntity> = emptyMap(),
    val ayahIdsWithTafsir: Set<Int> = emptySet(),
    val showTranslation: Boolean = true,
    val loading: Boolean = true
)

data class TafsirUiState(
    val surahName: String = "",
    val ayahNumber: Int = 0,
    val entriesAr: List<TafsirEntity> = emptyList(),
    val entriesFa: List<TafsirEntity> = emptyList(),
    val language: String = "ar",
    val loading: Boolean = true
) {
    val entries: List<TafsirEntity>
        get() = if (language == "fa") entriesFa else entriesAr
}

data class TafsirBrowseUiState(
    val surahNumber: Int = 0,
    val surahName: String = "",
    val entriesAr: List<TafsirEntity> = emptyList(),
    val entriesFa: List<TafsirEntity> = emptyList(),
    val language: String = "ar",
    val searchQuery: String = "",
    val loading: Boolean = true
) {
    val filteredEntries: List<TafsirEntity>
        get() {
            val entries = if (language == "fa") entriesFa else entriesAr
            return if (searchQuery.isBlank()) entries
            else entries.filter { it.textFa.contains(searchQuery, ignoreCase = true) }
        }
}

data class SearchUiState(
    val query: String = "",
    val includeQuran: Boolean = true,
    val includeTranslation: Boolean = true,
    val includeTafsir: Boolean = true,
    val results: List<SearchResult> = emptyList(),
    val loading: Boolean = false
)

class QuranViewModel(
    private val repo: QuranRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(settingsRepo.load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _tafsirBrowse = MutableStateFlow(TafsirBrowseUiState())
    val tafsirBrowse: StateFlow<TafsirBrowseUiState> = _tafsirBrowse.asStateFlow()

    private val _fullQuran = MutableStateFlow(FullQuranUiState())
    val fullQuran: StateFlow<FullQuranUiState> = _fullQuran.asStateFlow()

    private val _scrollTarget = MutableStateFlow<Int?>(null)
    val scrollTarget: StateFlow<Int?> = _scrollTarget.asStateFlow()

    private var pendingReturnAyahId: Int? = null

    private val _surahList = MutableStateFlow(SurahListUiState())
    val surahList: StateFlow<SurahListUiState> = _surahList.asStateFlow()

    private val _juzList = MutableStateFlow(JuzListUiState())
    val juzList: StateFlow<JuzListUiState> = _juzList.asStateFlow()

    private val _juz = MutableStateFlow(JuzUiState())
    val juz: StateFlow<JuzUiState> = _juz.asStateFlow()

    private val _surah = MutableStateFlow(SurahUiState())
    val surah: StateFlow<SurahUiState> = _surah.asStateFlow()

    private val _tafsir = MutableStateFlow(TafsirUiState())
    val tafsir: StateFlow<TafsirUiState> = _tafsir.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    fun updateSettings(newSettings: AppSettings) {
        val langChanged = newSettings.translationLanguage != _settings.value.translationLanguage
        _settings.value = newSettings
        settingsRepo.save(newSettings)
        if (langChanged) refreshTranslations()
    }

    fun loadTafsirBrowse(surahNumber: Int) = viewModelScope.launch {
        val keepLang = _tafsirBrowse.value.language
        _tafsirBrowse.value = TafsirBrowseUiState(surahNumber = surahNumber, language = keepLang, loading = true)
        val entriesAr = repo.getTafsirForSurah(surahNumber, "ar")
        val entriesFa = repo.getTafsirForSurah(surahNumber, "fa")
        val surahName = repo.getSurah(surahNumber).firstOrNull()?.surahNameFa ?: ""
        _tafsirBrowse.value = TafsirBrowseUiState(
            surahNumber = surahNumber,
            surahName = surahName,
            entriesAr = entriesAr,
            entriesFa = entriesFa,
            language = keepLang,
            loading = false
        )
    }

    fun setTafsirBrowseLanguage(language: String) {
        _tafsirBrowse.value = _tafsirBrowse.value.copy(language = language)
    }

    fun updateTafsirBrowseQuery(q: String) {
        _tafsirBrowse.value = _tafsirBrowse.value.copy(searchQuery = q)
    }

    fun loadFullQuran() = viewModelScope.launch {
        if (_fullQuran.value.items.isNotEmpty()) return@launch
        _fullQuran.value = FullQuranUiState(loading = true)

        val allAyat = repo.getAllAyat()
        val translations = repo.getAllTranslations(currentTranslator())
        val tafsirIds = repo.getAllTafsirAyahIds()
        val juzList = repo.getJuzList()

        val items = mutableListOf<ReadingItem>()
        val ayahItemIndex = HashMap<Int, Int>()
        val surahItemIndex = HashMap<Int, Int>()
        var lastSurah = -1

        for (ayah in allAyat) {
            if (ayah.surahNumber != lastSurah) {
                surahItemIndex[ayah.surahNumber] = items.size
                items += ReadingItem.SurahHeader(ayah.surahNumber, ayah.surahNameFa)
                if (ayah.surahNumber !in SURAHS_WITHOUT_SEPARATE_BISMILLAH_VM) {
                    items += ReadingItem.Bismillah(ayah.surahNumber)
                }
                lastSurah = ayah.surahNumber
            }
            ayahItemIndex[ayah.globalAyahId] = items.size
            items += ReadingItem.Ayah(ayah)
        }

        val juzAyahIndex = juzList.associate { it.juzNumber to (ayahItemIndex[it.startAyahId] ?: 0) }

        _fullQuran.value = FullQuranUiState(
            items = items,
            translations = translations,
            ayahIdsWithTafsir = tafsirIds,
            ayahItemIndex = ayahItemIndex,
            surahItemIndex = surahItemIndex,
            juzAyahIndex = juzAyahIndex,
            showTranslation = _fullQuran.value.showTranslation,
            loading = false
        )
    }

    private fun currentTranslator(): String =
        if (_settings.value.translationLanguage == "en") TRANSLATOR_EN else TRANSLATOR_FA

    /** وقتی زبان ترجمه عوض می‌شود، فقط نقشه‌ی ترجمه را دوباره می‌خواند (بدون بارگذاری مجدد کل قرآن) */
    fun refreshTranslations() = viewModelScope.launch {
        if (_fullQuran.value.items.isEmpty()) return@launch
        val translations = repo.getAllTranslations(currentTranslator())
        _fullQuran.value = _fullQuran.value.copy(translations = translations)
    }

    fun toggleFullQuranTranslationVisible() {
        _fullQuran.value = _fullQuran.value.copy(showTranslation = !_fullQuran.value.showTranslation)
    }

    fun requestScrollToSurah(surahNumber: Int) {
        _fullQuran.value.surahItemIndex[surahNumber]?.let { _scrollTarget.value = it }
    }

    fun requestScrollToJuz(juzNumber: Int) {
        _fullQuran.value.juzAyahIndex[juzNumber]?.let { _scrollTarget.value = it }
    }

    fun consumeScrollTarget() {
        _scrollTarget.value = null
    }

    /** قبل از رفتن به صفحه تفسیر، آیه جاری را ذخیره می‌کند تا هنگام بازگشت به همان‌جا اسکرول شود */
    fun rememberReturnAyah(globalAyahId: Int) {
        pendingReturnAyahId = globalAyahId
    }

    /** هنگام ورود مجدد به صفحه اصلی (بازگشت از تفسیر) صدا زده می‌شود */
    fun consumePendingReturnAyah(): Int? {
        val id = pendingReturnAyahId
        pendingReturnAyahId = null
        return id
    }

    fun itemIndexForAyah(globalAyahId: Int): Int? = _fullQuran.value.ayahItemIndex[globalAyahId]

    fun loadSurahList() = viewModelScope.launch {
        _surahList.value = SurahListUiState(loading = true)
        val list = repo.getSurahList()
        _surahList.value = SurahListUiState(list, loading = false)
    }

    fun loadJuzList() = viewModelScope.launch {
        _juzList.value = JuzListUiState(loading = true)
        val list = repo.getJuzList()
        _juzList.value = JuzListUiState(list, loading = false)
    }

    fun loadJuz(juzNumber: Int) = viewModelScope.launch {
        val keepTranslationPref = _juz.value.showTranslation
        _juz.value = JuzUiState(juzNumber = juzNumber, showTranslation = keepTranslationPref, loading = true)
        val juzInfo = repo.getJuzList().firstOrNull { it.juzNumber == juzNumber }
        if (juzInfo == null) {
            _juz.value = JuzUiState(juzNumber = juzNumber, showTranslation = keepTranslationPref, loading = false)
            return@launch
        }
        val ayat = repo.getAyahRange(juzInfo.startAyahId, juzInfo.endAyahId)
        val translations = repo.getTranslations(juzInfo.startAyahId, juzInfo.endAyahId).associateBy { it.globalAyahId }
        val tafsirIds = repo.getAyahIdsWithTafsirInRange(juzInfo.startAyahId, juzInfo.endAyahId)
        _juz.value = JuzUiState(
            juzNumber = juzNumber,
            ayat = ayat,
            translations = translations,
            ayahIdsWithTafsir = tafsirIds,
            showTranslation = keepTranslationPref,
            loading = false
        )
    }

    fun toggleJuzTranslationVisible() {
        _juz.value = _juz.value.copy(showTranslation = !_juz.value.showTranslation)
    }

    fun loadSurah(surahNumber: Int) = viewModelScope.launch {
        val keepTranslationPref = _surah.value.showTranslation
        _surah.value = SurahUiState(surahNumber = surahNumber, showTranslation = keepTranslationPref, loading = true)
        val ayat = repo.getSurah(surahNumber)
        val translations = if (ayat.isNotEmpty())
            repo.getTranslations(ayat.first().globalAyahId, ayat.last().globalAyahId)
                .associateBy { it.globalAyahId }
        else emptyMap()
        val tafsirIds = repo.getAyahIdsWithTafsir(surahNumber)
        _surah.value = SurahUiState(
            surahNumber = surahNumber,
            surahName = ayat.firstOrNull()?.surahNameFa ?: "",
            ayat = ayat,
            translations = translations,
            ayahIdsWithTafsir = tafsirIds,
            showTranslation = keepTranslationPref,
            loading = false
        )
    }

    fun setTafsirLanguage(language: String) {
        _tafsir.value = _tafsir.value.copy(language = language)
    }

    fun toggleTranslationVisible() {
        _surah.value = _surah.value.copy(showTranslation = !_surah.value.showTranslation)
    }

    fun loadTafsir(globalAyahId: Int, surahName: String, ayahNumber: Int) = viewModelScope.launch {
        val keepLang = _tafsir.value.language
        _tafsir.value = TafsirUiState(surahName = surahName, ayahNumber = ayahNumber, language = keepLang, loading = true)
        val entriesAr = repo.getTafsirForAyah(globalAyahId, "ar")
        val entriesFa = repo.getTafsirForAyah(globalAyahId, "fa")
        _tafsir.value = TafsirUiState(surahName, ayahNumber, entriesAr, entriesFa, keepLang, loading = false)
    }

    fun updateQuery(q: String) {
        _search.value = _search.value.copy(query = q)
    }

    fun toggleFilter(kind: String) {
        val s = _search.value
        _search.value = when (kind) {
            "quran" -> s.copy(includeQuran = !s.includeQuran)
            "translation" -> s.copy(includeTranslation = !s.includeTranslation)
            "tafsir" -> s.copy(includeTafsir = !s.includeTafsir)
            else -> s
        }
    }

    fun runSearch() = viewModelScope.launch {
        val s = _search.value
        if (s.query.isBlank()) {
            _search.value = s.copy(results = emptyList(), loading = false)
            return@launch
        }
        _search.value = s.copy(loading = true)
        val results = repo.search(s.query, s.includeQuran, s.includeTranslation, s.includeTafsir)
        // اگر کاربر در حین جستجو متن را عوض کرده، این نتیجه‌ی قدیمی را نادیده بگیر
        if (_search.value.query == s.query) {
            _search.value = _search.value.copy(results = results, loading = false)
        }
    }
}
