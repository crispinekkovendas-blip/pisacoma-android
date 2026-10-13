package pe.pisacoma.iglesia.audio

import pe.pisacoma.iglesia.data.model.Hymn
import java.net.URLEncoder

/** Hymn audio streams through the site's /api/audio proxy, built exactly as the hymn page does. */
object AudioUrls {
    const val SITE = "https://quiz-qwik.vercel.app"
    const val PROXY = "$SITE/api/audio?url="

    /** `encodeURIComponent` semantics: the Java encoder differs on space, `*`, `~` and parentheses. */
    fun proxied(mp3: String): String = PROXY + URLEncoder.encode(mp3, "UTF-8")
        .replace("+", "%20").replace("%21", "!").replace("%27", "'").replace("%28", "(").replace("%29", ")").replace("%7E", "~").replace("*", "%2A")

    fun mediaId(hymn: Hymn, instrumental: Boolean) = "${hymn.variant.slug}-${hymn.number}" + if (instrumental) "-instr" else ""

    fun title(hymn: Hymn, instrumental: Boolean) = "${hymn.number} · ${hymn.title}" + if (instrumental) " (instrumental)" else ""

    fun streamUrl(hymn: Hymn, instrumental: Boolean): String? = (if (instrumental) hymn.mp3UrlInstr else hymn.mp3Url)?.let { proxied(it) }
}
