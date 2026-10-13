package pe.pisacoma.iglesia.data.model

/** The three hymnals, by the site's URL slug (`/himnario/<slug>/<n>`). */
enum class HymnVariant(val slug: String, val file: String, val label: String, val tabLabel: String, val tabSub: String) {
    ES("es", "himnario-es", "Himnario Adventista · Español", "Español", "Adventista · 613 himnos"),
    PT("pt", "himnario-pt", "Hinário Adventista · Português (Antigo)", "PT Antigo", "Hinário Antigo · 610"),
    PT_NOVO("pt-novo", "himnario-pt-novo", "Novo Hinário Adventista · Português", "PT Novo", "Novo Hinário · 600");

    companion object {
        fun fromSlug(slug: String): HymnVariant? = entries.firstOrNull { it.slug == slug }
    }
}

/** One printed block of a hymn: a numbered stanza or the chorus. */
data class LyricBlock(val type: String, val lines: List<String>, val stanzaNumber: Int? = null) {
    val isChorus: Boolean get() = type == "chorus"
}

data class Hymn(
    val variant: HymnVariant,
    val number: Int,
    val title: String,
    val bibleReference: String?,
    val mp3Url: String?,
    val mp3UrlInstr: String?,
    val ytId: String?,
    val blocks: List<LyricBlock>,
)
