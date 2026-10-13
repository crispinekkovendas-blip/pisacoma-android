package pe.pisacoma.iglesia.feature.buscar

import pe.pisacoma.iglesia.data.model.SearchIndex
import pe.pisacoma.iglesia.util.BibleRefs
import java.text.Normalizer

enum class ResultKind { BIBLE, HYMN, EGW, LESSON }

/** One row of results (site utils/search.ts SearchHit); `href` is the site path, parsed by Route.parse. */
data class SearchHit(
    val kind: ResultKind,
    /** Big italic numeral shown on the row (hymn number, chapter, lesson). */
    val num: String,
    val title: String,
    /** Small caption under the title. */
    val caption: String,
    val href: String,
    val score: Int,
)

data class SearchGroups(val bible: List<SearchHit>, val hymns: List<SearchHit>, val egw: List<SearchHit>, val lessons: List<SearchHit>) {
    val total: Int get() = bible.size + hymns.size + egw.size + lessons.size
    companion object { val EMPTY = SearchGroups(emptyList(), emptyList(), emptyList(), emptyList()) }
}

/** Line-by-line port of the site's utils/search.ts over the bundled index. */
object Search {
    private val VARIANT_LABEL = mapOf(
        "es" to "Himnario Adventista · Español",
        "pt" to "Hinário Antigo · Português",
        "pt-novo" to "Novo Hinário · Português",
    )

    private val DIACRITICS = Regex("\\p{Mn}+")
    private val PUNCT = Regex("[«»\"'“”‘’.,;:!¡?¿()\\[\\]]")
    private val SPACES = Regex("\\s+")

    fun normalize(s: String): String =
        DIACRITICS.replace(Normalizer.normalize(s, Normalizer.Form.NFD), "")
            .lowercase()
            .replace(PUNCT, " ")
            .replace(SPACES, " ")
            .trim()

    /** 0 = no match; higher is better. Every query token must appear. */
    fun scoreTitle(qNorm: String, tokens: List<String>, title: String): Int {
        val t = normalize(title)
        if (t.isEmpty()) return 0
        if (t == qNorm) return 100
        if (t.startsWith(qNorm)) return 60
        val words = t.split(" ")
        var score = 0
        for (tok in tokens) {
            score += when {
                words.contains(tok) -> 12
                words.any { it.startsWith(tok) } -> 8
                t.contains(tok) -> 4
                else -> return 0
            }
        }
        return score + maxOf(0, 6 - t.length / 12)
    }

    // Abbreviations that are not a prefix of the book name (after accent folding).
    private val BOOK_ALIASES = mapOf(
        "gn" to "genesis", "lv" to "levitico", "nm" to "numeros", "dt" to "deuteronomio", "rt" to "rut",
        "cnt" to "cantares", "lm" to "lamentaciones", "dn" to "daniel", "jl" to "joel", "mt" to "mateo",
        "mr" to "marcos", "mc" to "marcos", "lc" to "lucas", "jn" to "juan", "hch" to "hechos",
        "flp" to "filipenses", "flm" to "filemon", "ts" to "tesalonicenses", "stg" to "santiago",
        "sant" to "santiago", "sl" to "salmos", "cr" to "cronicas", "co" to "corintios", "s" to "samuel",
        "r" to "reyes", "p" to "pedro", "ti" to "timoteo",
    )

    private fun refFold(s: String) = DIACRITICS.replace(Normalizer.normalize(s, Normalizer.Form.NFD), "").lowercase().replace(SPACES, " ").trim()

    private val PHONE_REF = Regex("^(?:([1-3])\\s*)?([a-zñ]+(?: [a-zñ]+)*?)\\.?\\s*(\\d{1,3})(?:\\s*[:., ]\\s*(\\d{1,3})(?:\\s*[-–]\\s*(\\d{1,3}))?)?$")

    /** A whole-query reference typed the phone way: "juan 3:16", "salmo 23", "1 cor 13 4", "2co 10:4-5". */
    fun parseReference(raw: String, books: List<SearchIndex.BibleRow>): List<SearchHit> {
        val m = PHONE_REF.find(refFold(raw)) ?: return emptyList()
        val prefix = m.groups[1]?.value
        val stemRaw = m.groups[2]!!.value
        val stem = BOOK_ALIASES[stemRaw] ?: stemRaw
        val ch = m.groups[3]!!.value.toInt()
        val v1 = m.groups[4]?.value?.toInt() ?: 0
        val v2 = m.groups[5]?.value?.toInt() ?: 0

        data class P(val book: SearchIndex.BibleRow, val num: String, val base: String)
        val parsed = books.map { b ->
            val f = Regex("^(?:([1-3]) )?(.+)$").find(refFold(b.name))!!
            P(b, f.groups[1]?.value ?: "", f.groups[2]!!.value)
        }
        val matches = { base: String -> base == stem || (stem.length >= 2 && base.startsWith(stem)) }
        var candidates = parsed.filter { it.num == (prefix ?: "") && matches(it.base) }
        if (candidates.isEmpty() && prefix == null) candidates = parsed.filter { it.num.isNotEmpty() && matches(it.base) }
        val exact = candidates.filter { it.base == stem }
        val picked = (if (exact.isNotEmpty()) exact else candidates).take(3).map { it.book }

        val hits = mutableListOf<SearchHit>()
        for (b in picked) {
            if (ch < 1 || ch > b.chapters) continue
            val verses = if (v1 > 0) ":$v1" + (if (v2 > v1) "-$v2" else "") else ""
            hits += SearchHit(
                ResultKind.BIBLE, b.abbr, "${b.name} $ch$verses",
                if (v1 > 0) "Biblia · Reina-Valera 1960 · abrir en el versículo" else "Biblia · Reina-Valera 1960 · abrir el capítulo",
                "/biblia/?libro=${b.num}&cap=$ch" + (if (v1 > 0) "&v=$v1" else ""),
                200,
            )
        }
        return hits
    }

    private val NUM_QUERY = Regex("^(himno|hino|leccion|lec\\.?|#)?\\s*#?\\s*(\\d{1,3})$")

    fun search(index: SearchIndex?, query: String): SearchGroups {
        val raw = query.trim()
        if (index == null || raw.isEmpty()) return SearchGroups.EMPTY
        val qNorm = normalize(raw)
        val tokens = qNorm.split(" ").filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return SearchGroups.EMPTY
        val numMatch = NUM_QUERY.find(refFold(raw))
        val asNumber = numMatch?.groups?.get(2)?.value?.toInt()
        val numWord = numMatch?.groups?.get(1)?.value ?: ""
        val hymnNumbers = !numWord.startsWith("lec")
        val lessonNumbers = !numWord.startsWith("hi")

        // --- Bible
        val bible = parseReference(raw, index.bible).toMutableList()
        val bookByNum = index.bible.associateBy { it.num }
        if (bible.isEmpty()) {
            for (ref in BibleRefs.findRefs(raw)) {
                val first = ref.sections.firstOrNull() ?: continue
                val book = bookByNum[ref.bookNum] ?: continue
                val v = if (first.from > 0) ":${first.from}" + (if (first.to > 0 && first.to != first.from) "-${first.to}" else "") else ""
                bible += SearchHit(
                    ResultKind.BIBLE, book.abbr, "${book.name} ${first.chapter}$v",
                    "Biblia · Reina-Valera 1960 · abrir el capítulo",
                    "/biblia/?libro=${book.num}&cap=${first.chapter}" + (if (first.from > 0) "&v=${first.from}" else ""),
                    200,
                )
                if (bible.size >= 3) break
            }
        }
        if (bible.isEmpty() && asNumber == null) {
            for (b in index.bible) {
                val s = scoreTitle(qNorm, tokens, b.name).let { if (it != 0) it else if (normalize(b.abbr) == qNorm) 50 else 0 }
                if (s == 0) continue
                bible += SearchHit(ResultKind.BIBLE, b.abbr, b.name, "Biblia · ${b.chapters} capítulos", "/biblia/?libro=${b.num}&cap=1", s)
            }
            bible.sortByDescending { it.score }
            while (bible.size > 4) bible.removeAt(bible.lastIndex)
        }

        // --- Hymns
        val hymns = mutableListOf<SearchHit>()
        for (h in index.hymns) {
            var s = 0
            var viaLine = ""
            if (asNumber != null) {
                if (hymnNumbers && h.number == asNumber) s = 100
            } else {
                s = scoreTitle(qNorm, tokens, h.title)
                for (line in h.lines) {
                    val ls = scoreTitle(qNorm, tokens, line) - 2
                    if (ls > s) { s = ls; viaLine = line }
                }
            }
            if (s <= 0) continue
            val label = VARIANT_LABEL[h.variant] ?: h.variant
            hymns += SearchHit(
                ResultKind.HYMN, h.number.toString(), h.title,
                if (viaLine.isNotEmpty()) "«$viaLine…» · $label" else label,
                "/himnario/${h.variant}/${h.number}/",
                s + (if (h.variant == "es") 1 else 0),
            )
        }
        hymns.sortWith(compareByDescending<SearchHit> { it.score }.thenBy { it.title })

        // --- EGW
        val egw = mutableListOf<SearchHit>()
        if (asNumber == null) {
            for (book in index.egw) {
                val bs = scoreTitle(qNorm, tokens, book.t)
                if (bs != 0) egw += SearchHit(ResultKind.EGW, "§", book.t, "Elena G. de White · ${book.c.size} capítulos", "/egw/${book.s}/", bs + 20)
                for ((n, title) in book.c) {
                    val s = scoreTitle(qNorm, tokens, title)
                    if (s == 0) continue
                    egw += SearchHit(ResultKind.EGW, n.toString(), title, "${book.t} · Capítulo $n", "/egw/${book.s}/?ch=$n", s)
                }
            }
            egw.sortWith(compareByDescending<SearchHit> { it.score }.thenBy { it.title })
        }

        // --- Lessons
        val lessons = mutableListOf<SearchHit>()
        for (l in index.lessons) {
            val ls = if (asNumber != null) (if (lessonNumbers && l.n == asNumber) 100 else 0) else scoreTitle(qNorm, tokens, l.t)
            if (ls != 0) lessons += SearchHit(ResultKind.LESSON, l.n.toString(), l.t, "Escuela Sabática · Lección ${l.n} · ${index.quarter.title}", "/escuela-sabatica/${l.n}/", ls + 10)
            if (asNumber != null) continue
            l.d.forEachIndexed { i, dayTitle ->
                if (i == 0 || dayTitle.isEmpty()) return@forEachIndexed
                val s = scoreTitle(qNorm, tokens, dayTitle)
                if (s == 0) return@forEachIndexed
                lessons += SearchHit(ResultKind.LESSON, "${l.n}.$i", dayTitle, "Lección ${l.n} · ${l.t} · Día ${i + 1}", "/escuela-sabatica/${l.n}/?dia=$i", s)
            }
        }
        lessons.sortByDescending { it.score }

        return SearchGroups(bible, hymns, egw, lessons)
    }
}
