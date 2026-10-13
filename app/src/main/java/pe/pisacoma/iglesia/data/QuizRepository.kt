package pe.pisacoma.iglesia.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.pisacoma.iglesia.data.model.QuizPack

/** The quiz packs, newest first (the order of the site's QUIZZES array). */
class QuizRepository(private val assets: AssetSource) {
    private var packs: List<QuizPack>? = null
    private val mutex = Mutex()

    suspend fun packs(): List<QuizPack> = mutex.withLock {
        packs ?: AppJson.decodeFromString<List<QuizPack>>(assets.read("data/quizzes.json")).also { packs = it }
    }

    suspend fun pack(slug: String): QuizPack? = packs().firstOrNull { it.slug == slug }

    suspend fun forLesson(lessonNumber: Int): QuizPack? = packs().firstOrNull { it.lessonNumber == lessonNumber }
}
