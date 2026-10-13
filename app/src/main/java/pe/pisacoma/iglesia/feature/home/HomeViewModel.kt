package pe.pisacoma.iglesia.feature.home

import pe.pisacoma.iglesia.data.content.ContentErrors
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.data.AppJson
import pe.pisacoma.iglesia.data.AssetSource
import pe.pisacoma.iglesia.data.ChurchRepository
import pe.pisacoma.iglesia.data.QuarterlyRepository
import pe.pisacoma.iglesia.data.model.ScheduleItem
import pe.pisacoma.iglesia.data.net.Anuncio
import pe.pisacoma.iglesia.data.net.AnuncioView
import pe.pisacoma.iglesia.data.net.Anuncios
import pe.pisacoma.iglesia.data.prefs.UserState
import pe.pisacoma.iglesia.util.Zmanim

/** Photo credits shown in the night scene (site src/data/home-photos.ts). */
@Serializable
data class HomePhoto(val section: String, val title: String, val author: String, val license: String, val licenseUrl: String, val source: String)

@Serializable
private data class AnunciosFile(val csvUrl: String = "", val items: List<Anuncio> = emptyList())

data class HomeUi(
    val quarterTitle: String = "",
    val weeksTotal: Int = 13,
    val escWeek: Int = 1,
    val escEnded: Boolean = false,
    val sabbath: Zmanim.SabbathTimes? = null,
    val schedule: List<ScheduleItem> = emptyList(),
    val anuncios: List<AnuncioView> = emptyList(),
    val photos: List<HomePhoto> = emptyList(),
    val paused: Boolean = false,
)

/** The live bits the site renders over the motion: lesson of the week, Sabbath sunset, schedule, announcements, the pause flag. */
class HomeViewModel(
    private val assets: AssetSource,
    private val quarterly: QuarterlyRepository,
    private val church: ChurchRepository,
    private val userState: UserState,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUi(paused = userState.bool(UserState.Keys.homePaused)))
    val state: StateFlow<HomeUi> = _state

    init {
        viewModelScope.launch(ContentErrors) {
            val now = System.currentTimeMillis()
            val q = quarterly.quarterly()
            val c = church.church()
            val photos = runCatching { AppJson.decodeFromString<List<HomePhoto>>(assets.read("data/home-photos.json")) }.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    quarterTitle = q.title,
                    weeksTotal = q.lessons.size,
                    escWeek = Zmanim.currentLessonIndex(q.lessons.map { l -> l.startDate }, now) + 1,
                    escEnded = Zmanim.quarterStatus(Zmanim.QuarterDates(q.startDate, q.endDate), now).state == Zmanim.QuarterState.ENDED,
                    sabbath = Zmanim.sabbathTimes(now),
                    schedule = c.schedule,
                    photos = photos,
                )
            }
            val file = runCatching { AppJson.decodeFromString<AnunciosFile>(assets.read("data/anuncios.json")) }.getOrDefault(AnunciosFile())
            val views = Anuncios.load(file.items, file.csvUrl, now)
            _state.update { it.copy(anuncios = views) }
        }
    }

    fun togglePause() {
        val next = !_state.value.paused
        userState.set(UserState.Keys.homePaused, next)
        _state.update { it.copy(paused = next) }
    }
}
