package pe.pisacoma.iglesia.feature.escuela

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.data.AppJson
import pe.pisacoma.iglesia.data.QuarterlyRepository
import pe.pisacoma.iglesia.data.QuizRepository
import pe.pisacoma.iglesia.data.model.Quarterly
import pe.pisacoma.iglesia.data.prefs.UserState
import pe.pisacoma.iglesia.util.LessonMarkdown
import pe.pisacoma.iglesia.util.MdBlock
import pe.pisacoma.iglesia.util.Zmanim

val DAY_LABELS = listOf("Sábado", "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
val DAY_SHORT = listOf("Sáb", "Dom", "Lun", "Mar", "Mié", "Jue", "Vie")

/** Site [leccion]/index.tsx ACCENTS: one muted accent per lesson. */
val LESSON_ACCENTS = listOf(
    0xFF5A7C3A, 0xFF6C5424, 0xFF4D6F33, 0xFF7A6633, 0xFF8A5A3A,
    0xFF506E35, 0xFF4A6741, 0xFF7C4F3A, 0xFF5D7333, 0xFF85702D,
    0xFF6F5F2C, 0xFF3F6238, 0xFFA06B3A,
)

/** `escuela:<QID>:bookmarks` entries, as the site writes them. */
@Serializable
data class LessonBookmark(val leccionN: Int, val title: String, val addedAt: Long)

data class QuarterUi(
    val quarterly: Quarterly? = null,
    val currentIdx: Int = 0,
    val ended: Boolean = false,
    val nextStartLabel: String = "",
    /** completed day count per lesson number */
    val progress: Map<Int, Int> = emptyMap(),
)

class QuarterViewModel(private val repo: QuarterlyRepository, private val userState: UserState) : ViewModel() {
    private val _state = MutableStateFlow(QuarterUi())
    val state: StateFlow<QuarterUi> = _state

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val q = repo.quarterly()
        val now = System.currentTimeMillis()
        val status = Zmanim.quarterStatus(Zmanim.QuarterDates(q.startDate, q.endDate), now)
        val progress = q.lessons.associate { l -> l.number to (0 until l.days.size).count { d -> userState.string(UserState.Keys.lessonDone(q.id, l.number, d)) != null } }
        _state.value = QuarterUi(
            quarterly = q,
            currentIdx = Zmanim.currentLessonIndex(q.lessons.map { it.startDate }, now),
            ended = status.state == Zmanim.QuarterState.ENDED,
            nextStartLabel = Zmanim.longDayLabel(status.nextStart),
            progress = progress,
        )
    }
}

data class DayView(val title: String, val blocks: List<MdBlock>)

data class LessonUi(
    val quarterly: Quarterly? = null,
    val number: Int = 0,
    val days: List<DayView> = emptyList(),
    val activeDay: Int = 0,
    val doneDays: Set<Int> = emptySet(),
    val daysWithNotes: Set<Int> = emptySet(),
    val bookmarks: List<LessonBookmark> = emptyList(),
    val progress: Map<Int, Int> = emptyMap(),
    val notes: String = "",
    val savedAt: Long? = null,
    val scale: Float = 1.05f,
    val quizSlug: String? = null,
    val notFound: Boolean = false,
) {
    val lesson get() = quarterly?.lessons?.firstOrNull { it.number == number }
    val totalDays get() = days.size
    val completedCount get() = doneDays.size
    val pct get() = if (totalDays == 0) 0 else Math.round(completedCount * 100f / totalDays)
    val isDone get() = activeDay in doneDays
    val isBookmarked get() = bookmarks.any { it.leccionN == number }
}

class LessonViewModel(
    private val repo: QuarterlyRepository,
    private val quizzes: QuizRepository,
    private val userState: UserState,
    private val number: Int,
    private val initialDay: Int?,
) : ViewModel() {
    private val _state = MutableStateFlow(LessonUi(number = number))
    val state: StateFlow<LessonUi> = _state
    private lateinit var qid: String

    init {
        viewModelScope.launch {
            val q = repo.quarterly()
            qid = q.id
            val lesson = q.lessons.firstOrNull { it.number == number }
            if (lesson == null) { _state.update { it.copy(notFound = true) }; return@launch }
            val days = lesson.days.map { d -> val p = LessonMarkdown.parseDay(d.content); DayView(p.title, LessonMarkdown.parse(p.body)) }
            val last = initialDay ?: userState.string(UserState.Keys.lessonLastDay(qid, number))?.toIntOrNull()
            val active = last?.takeIf { it in days.indices } ?: 0
            _state.update {
                it.copy(
                    quarterly = q, days = days, activeDay = active,
                    doneDays = days.indices.filter { d -> userState.string(UserState.Keys.lessonDone(qid, number, d)) != null }.toSet(),
                    daysWithNotes = days.indices.filter { d -> !userState.string(UserState.Keys.lessonNotes(qid, number, d)).isNullOrEmpty() }.toSet(),
                    bookmarks = readBookmarks(),
                    progress = q.lessons.associate { l -> l.number to (0 until l.days.size).count { d -> userState.string(UserState.Keys.lessonDone(qid, l.number, d)) != null } },
                    notes = userState.string(UserState.Keys.lessonNotes(qid, number, active)) ?: "",
                    quizSlug = quizzes.forLesson(number)?.slug,
                )
            }
            userState.set(UserState.Keys.lessonLastDay(qid, number), active.toString())
        }
    }

    private fun readBookmarks(): List<LessonBookmark> = runCatching {
        userState.string(UserState.Keys.lessonBookmarks(qid))?.let { AppJson.decodeFromString<List<LessonBookmark>>(it) } ?: emptyList()
    }.getOrDefault(emptyList())

    fun selectDay(d: Int) {
        if (d !in _state.value.days.indices) return
        _state.update { it.copy(activeDay = d, notes = userState.string(UserState.Keys.lessonNotes(qid, number, d)) ?: "", savedAt = null) }
        userState.set(UserState.Keys.lessonLastDay(qid, number), d.toString())
    }

    fun toggleDone() {
        val d = _state.value.activeDay
        val key = UserState.Keys.lessonDone(qid, number, d)
        if (d in _state.value.doneDays) {
            userState.set(key, null)
            _state.update { it.copy(doneDays = it.doneDays - d) }
        } else {
            userState.set(key, System.currentTimeMillis().toString())
            _state.update { it.copy(doneDays = it.doneDays + d) }
        }
        _state.update { it.copy(progress = it.progress + (number to it.doneDays.size)) }
    }

    fun toggleBookmark() {
        val s = _state.value
        val next = if (s.isBookmarked) s.bookmarks.filter { it.leccionN != number }
        else listOf(LessonBookmark(number, s.lesson?.title ?: "", System.currentTimeMillis())) + s.bookmarks
        userState.set(UserState.Keys.lessonBookmarks(qid), AppJson.encodeToString(next))
        _state.update { it.copy(bookmarks = next) }
    }

    fun removeBookmark(leccionN: Int) {
        val next = _state.value.bookmarks.filter { it.leccionN != leccionN }
        userState.set(UserState.Keys.lessonBookmarks(qid), AppJson.encodeToString(next))
        _state.update { it.copy(bookmarks = next) }
    }

    fun notes(value: String) {
        val d = _state.value.activeDay
        val key = UserState.Keys.lessonNotes(qid, number, d)
        if (value.isBlank()) { userState.set(key, null); _state.update { it.copy(notes = value, daysWithNotes = it.daysWithNotes - d, savedAt = null) } }
        else { userState.set(key, value); _state.update { it.copy(notes = value, daysWithNotes = it.daysWithNotes + d, savedAt = System.currentTimeMillis()) } }
    }

    fun clearNotes() = notes("")
    fun smaller() = _state.update { it.copy(scale = pe.pisacoma.iglesia.ui.components.TextScale.smaller(it.scale)) }
    fun larger() = _state.update { it.copy(scale = pe.pisacoma.iglesia.ui.components.TextScale.larger(it.scale)) }
}

/** Site relativeTime: "ahora mismo", "hace 5s", "hace 3 min", "hace 2 h", "hace 4 d". */
fun relativeTimeEs(ms: Long, now: Long = System.currentTimeMillis()): String {
    val s = ((now - ms) / 1000).coerceAtLeast(0)
    if (s < 5) return "ahora mismo"
    if (s < 60) return "hace ${s}s"
    val m = s / 60
    if (m < 60) return "hace $m min"
    val h = m / 60
    if (h < 24) return "hace $h h"
    return "hace ${h / 24} d"
}
