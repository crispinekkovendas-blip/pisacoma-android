package pe.pisacoma.iglesia.data.model

import kotlinx.serialization.Serializable

/** The site's src/data/quizzes/types.ts, exported to quizzes.json by tools/sync-content.mjs. */
@Serializable
data class QuizOption(val letter: String, val text: String)

@Serializable
data class QuizQuestion(
    val n: Int,
    val text: String,
    val ref: String? = null,
    val options: List<QuizOption>,
    val correct: String,
    val explain: String? = null,
)

@Serializable
data class QuizLevel(val minScore: Int, val name: String, val desc: String)

@Serializable
data class Memorize(val text: String, val ref: String)

@Serializable
data class QuizPack(
    val slug: String,
    val lessonNumber: Int,
    val title: String,
    val badge: String,
    val subtitle: String,
    val memorize: Memorize,
    val metaTitle: String = "",
    val metaDescription: String = "",
    val questions: List<QuizQuestion>,
    val levels: List<QuizLevel>? = null,
)
