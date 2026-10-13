package pe.pisacoma.iglesia.data.model

import kotlinx.serialization.Serializable

/** One of the four «pasillos» of the EGW catalogue (site egw/index.tsx AISLES). */
@Serializable
data class EgwAisle(val roman: String, val label: String, val lede: String)

/** Catalogue row (site egw/index.tsx BOOKS); `cover` is the file name under assets/covers. */
@Serializable
data class EgwBookMeta(
    val id: String,
    val slug: String,
    val title: String,
    val subtitle: String,
    val color: String,
    val chapters: Int,
    val aisle: String,
    val cover: String,
)

@Serializable
data class EgwChapter(val number: Int, val title: String, val paragraphs: List<String>)

@Serializable
data class EgwBook(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val lang: String = "es",
    val color: String = "",
    val chapters: List<EgwChapter>,
)
