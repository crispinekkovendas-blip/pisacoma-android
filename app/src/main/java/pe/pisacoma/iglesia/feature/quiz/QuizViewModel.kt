package pe.pisacoma.iglesia.feature.quiz

import pe.pisacoma.iglesia.data.content.ContentErrors
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.pisacoma.iglesia.data.QuizRepository
import pe.pisacoma.iglesia.data.model.QuizPack
import pe.pisacoma.iglesia.data.prefs.UserState

data class QuizUi(val pack: QuizPack? = null, val state: QuizState = QuizState(), val notFound: Boolean = false) {
    val levels get() = pack?.levels ?: QuizState.DEFAULT_LEVELS
}

/** Loads a pack and keeps its [QuizState] in sync with `quiz.<slug>.v1`, exactly as the site persists it. */
class QuizViewModel(private val quizzes: QuizRepository, private val userState: UserState, private val slug: String) : ViewModel() {
    private val _ui = MutableStateFlow(QuizUi())
    val ui: StateFlow<QuizUi> = _ui

    init {
        viewModelScope.launch(ContentErrors) {
            val pack = quizzes.pack(slug)
            if (pack == null) { _ui.update { it.copy(notFound = true) }; return@launch }
            _ui.value = QuizUi(pack, QuizState.fromJson(userState.string(UserState.Keys.quiz(slug))))
        }
    }

    private fun change(f: (QuizState) -> QuizState, persist: Boolean) {
        _ui.update { it.copy(state = f(it.state)) }
        if (persist) userState.set(UserState.Keys.quiz(slug), _ui.value.state.toJson())
    }

    fun pick(qN: Int, letter: String) = change({ it.requestCommit(qN, letter) }, persist = false)
    fun cancel() = change({ it.cancel() }, persist = false)
    fun confirm() = change({ it.confirm() }, persist = true)
    fun submit() = change({ it.submit() }, persist = true)
    fun reset() = change({ it.reset() }, persist = true)
}

class QuizIndexViewModel(quizzes: QuizRepository) : ViewModel() {
    private val _packs = MutableStateFlow<List<QuizPack>>(emptyList())
    val packs: StateFlow<List<QuizPack>> = _packs
    init { viewModelScope.launch(ContentErrors) { _packs.value = quizzes.packs() } }
}
