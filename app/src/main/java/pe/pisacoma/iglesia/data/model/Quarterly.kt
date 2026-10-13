package pe.pisacoma.iglesia.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The site's escuela-sabatica-<quarter>.json, field names as scraped. */
@Serializable
data class Quarterly(
    val id: String,
    val title: String,
    val description: String = "",
    @SerialName("human_date") val humanDate: String = "",
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("color_primary") val colorPrimary: String = "",
    val author: String = "",
    val introduction: String = "",
    val lessons: List<Lesson>,
)

@Serializable
data class Lesson(
    val number: Int,
    val title: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val days: List<LessonDay>,
)

/** `content` is the day's markdown with a `---` front matter (title, date). */
@Serializable
data class LessonDay(val day: Int, val content: String)
