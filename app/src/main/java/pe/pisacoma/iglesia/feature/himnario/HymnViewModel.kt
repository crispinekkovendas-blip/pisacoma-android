package pe.pisacoma.iglesia.feature.himnario

import pe.pisacoma.iglesia.data.content.ContentErrors
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.data.HymnRepository
import pe.pisacoma.iglesia.data.model.Hymn
import pe.pisacoma.iglesia.data.model.HymnVariant
import pe.pisacoma.iglesia.data.model.LyricBlock
import pe.pisacoma.iglesia.data.prefs.UserState

/** One screen of «Proyectar» (site hymn-projector ProjectorSlide). */
data class ProjectorSlide(val label: String, val lines: List<String>) {
    val isChorus: Boolean get() = label.startsWith("Coro")
}

sealed class HymnUi {
    data object Loading : HymnUi()
    data object NotFound : HymnUi()
    data class Ready(val hymn: Hymn, val slides: List<ProjectorSlide>, val hasNext: Boolean) : HymnUi()
}

class HymnViewModel(private val hymns: HymnRepository, private val userState: UserState, variant: String, number: Int) : ViewModel() {
    private val _state = MutableStateFlow<HymnUi>(HymnUi.Loading)
    val state: StateFlow<HymnUi> = _state

    init {
        viewModelScope.launch(ContentErrors) {
            val v = HymnVariant.fromSlug(variant)
            val h = v?.let { hymns.get(it, number) }
            if (h == null) { _state.value = HymnUi.NotFound; return@launch }
            val next = hymns.get(v, number + 1) != null
            _state.value = HymnUi.Ready(h, buildSlides(h.blocks), next)
            RecentHymns.push(userState, variant, number, h.title) // site RecordVisit
        }
    }

    companion object {
        /** Site hymn page rawSlides + splitLong: one screen per stanza, the chorus repeated after each. */
        fun buildSlides(blocks: List<LyricBlock>): List<ProjectorSlide> = splitLong(rawSlides(blocks))

        private fun rawSlides(blocks: List<LyricBlock>): List<ProjectorSlide> {
            val choruses = blocks.filter { it.isChorus }
            val stanzas = blocks.filter { !it.isChorus }
            if (choruses.size == 1 && stanzas.isNotEmpty()) {
                return stanzas.flatMapIndexed { i, b -> listOf(ProjectorSlide("Estrofa ${b.stanzaNumber ?: (i + 1)}", b.lines), ProjectorSlide("Coro", choruses[0].lines)) }
            }
            var n = 0
            return blocks.map { b -> if (b.isChorus) ProjectorSlide("Coro", b.lines) else ProjectorSlide("Estrofa ${b.stanzaNumber ?: ++n}", b.lines) }
        }

        /** Blocks longer than 12 lines don't fit a phone: split them into ≤ 8-line parts. */
        private fun splitLong(slides: List<ProjectorSlide>): List<ProjectorSlide> = slides.flatMap { sl ->
            if (sl.lines.size <= 12) listOf(sl)
            else {
                val parts = (sl.lines.size + 7) / 8
                val size = (sl.lines.size + parts - 1) / parts
                (0 until parts).map { i -> ProjectorSlide("${sl.label} · ${i + 1}/$parts", sl.lines.drop(i * size).take(size)) }
            }
        }
    }
}
