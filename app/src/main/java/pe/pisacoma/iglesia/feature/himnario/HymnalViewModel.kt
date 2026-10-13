package pe.pisacoma.iglesia.feature.himnario

import pe.pisacoma.iglesia.data.content.ContentErrors
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.data.HymnRepository
import pe.pisacoma.iglesia.data.model.HymnVariant
import pe.pisacoma.iglesia.data.prefs.UserState

/** A row of the hymnal index (site himnario/index.tsx Row). */
data class HymnRow(val number: Int, val title: String, val bibleRef: String?)

data class HymnalUiState(
    val variant: HymnVariant = HymnVariant.ES,
    val query: String = "",
    /** Every row of the active hymnal, deduplicated by number (NHA has 587 twice). */
    val rows: List<HymnRow> = emptyList(),
    val featured: List<HymnRow> = emptyList(),
    val recent: List<RecentHymn> = emptyList(),
    val loading: Boolean = true,
) {
    val isSearching: Boolean get() = query.trim().isNotEmpty()

    /** The site's filter: number, title or Bible reference contains the query. */
    val filtered: List<HymnRow>
        get() {
            val q = query.trim().lowercase()
            if (q.isEmpty()) return rows
            val qNum = q.toIntOrNull()
            return rows.filter { r ->
                r.title.lowercase().contains(q) || (qNum != null && r.number == qNum) || (r.bibleRef?.lowercase()?.contains(q) == true)
            }
        }

    /** Ranges of 100 for the «Ir a» chips and the list breaks. */
    val ranges: List<IntRange>
        get() {
            val max = rows.lastOrNull()?.number ?: return emptyList()
            return (1..max step RANGE_SIZE).map { s -> s..minOf(s + RANGE_SIZE - 1, max) }
        }

    val recentForVariant: List<RecentHymn> get() = recent.filter { it.variant == variant.slug }.take(3)

    companion object { const val RANGE_SIZE = 100 }
}

class HymnalViewModel(private val hymns: HymnRepository, private val userState: UserState) : ViewModel() {
    private val _state = MutableStateFlow(HymnalUiState())
    val state: StateFlow<HymnalUiState> = _state

    init { select(HymnVariant.ES) }

    fun select(variant: HymnVariant) {
        _state.update { it.copy(variant = variant, query = "", loading = true) }
        viewModelScope.launch(ContentErrors) {
            val seen = HashSet<Int>()
            val rows = hymns.all(variant).mapNotNull { h -> if (seen.add(h.number)) HymnRow(h.number, h.title, h.bibleReference) else null }
            val byNum = rows.associateBy { it.number }
            val featured = hymns.featured(variant).mapNotNull { byNum[it] }
            _state.update { it.copy(variant = variant, rows = rows, featured = featured, recent = RecentHymns.read(userState), loading = false) }
        }
    }

    fun query(q: String) = _state.update { it.copy(query = q) }

    fun refreshRecent() = _state.update { it.copy(recent = RecentHymns.read(userState)) }
}
