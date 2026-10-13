package pe.pisacoma.iglesia.util

import java.text.Normalizer

/** A chapter:from-to span inside a reference (site bibleStore.ts RefSection). */
data class RefSection(val chapter: Int, val from: Int, val to: Int)

/** A reference found in free text (site bibleStore.ts ParsedRef). `start`/`end` are char offsets. */
data class ParsedRef(
    val bookNum: Int,
    val bookName: String,
    val sections: List<RefSection>,
    val display: String,
    val start: Int,
    val end: Int,
)

/**
 * Port of the site's src/utils/bibleStore.ts findRefs: the same Spanish book map (full names,
 * accents, abbreviations with and without a dot, numbered books), the same regex
 * ("Juan 15:9", "1 Cor. 10:12 al 15", "Hechos 17: 16-34") and the same `; 5:4-8` continuations.
 */
object BibleRefs {
    private val BOOK_MAP: Map<String, Int> = mapOf(
        "génesis" to 1, "genesis" to 1, "gén" to 1, "gen" to 1, "gén." to 1, "gen." to 1, "gn" to 1,
        "éxodo" to 2, "exodo" to 2, "éxo" to 2, "exo" to 2, "éxo." to 2, "exo." to 2, "ex" to 2,
        "levítico" to 3, "levitico" to 3, "lev" to 3, "lev." to 3, "lv" to 3,
        "números" to 4, "numeros" to 4, "núm" to 4, "num" to 4, "núm." to 4, "num." to 4, "nm" to 4,
        "deuteronomio" to 5, "deut" to 5, "deut." to 5, "dt" to 5,
        "josué" to 6, "josue" to 6, "jos" to 6, "jos." to 6,
        "jueces" to 7, "juec" to 7, "juec." to 7, "jue" to 7,
        "rut" to 8, "rt" to 8,
        "1 samuel" to 9, "1 sam" to 9, "1 sam." to 9, "1s" to 9,
        "2 samuel" to 10, "2 sam" to 10, "2 sam." to 10, "2s" to 10,
        "1 reyes" to 11, "1 rey" to 11, "1 rey." to 11, "1r" to 11,
        "2 reyes" to 12, "2 rey" to 12, "2 rey." to 12, "2r" to 12,
        "1 crónicas" to 13, "1 cronicas" to 13, "1 crón" to 13, "1 cron" to 13, "1 crón." to 13, "1 cron." to 13, "1cr" to 13,
        "2 crónicas" to 14, "2 cronicas" to 14, "2 crón" to 14, "2 cron" to 14, "2 crón." to 14, "2 cron." to 14, "2cr" to 14,
        "esdras" to 15, "esd" to 15, "esd." to 15,
        "nehemías" to 16, "nehemias" to 16, "neh" to 16, "neh." to 16,
        "ester" to 17, "est" to 17,
        "job" to 18,
        "salmos" to 19, "salmo" to 19, "sal" to 19, "sal." to 19,
        "proverbios" to 20, "prov" to 20, "prov." to 20, "pr" to 20,
        "eclesiastés" to 21, "eclesiastes" to 21, "ecl" to 21, "ecl." to 21, "ec" to 21,
        "cantares" to 22, "cant" to 22, "cant." to 22, "cnt" to 22,
        "isaías" to 23, "isaias" to 23, "isa" to 23, "isa." to 23, "is" to 23,
        "jeremías" to 24, "jeremias" to 24, "jer" to 24, "jer." to 24,
        "lamentaciones" to 25, "lam" to 25, "lam." to 25, "lm" to 25,
        "ezequiel" to 26, "eze" to 26, "eze." to 26, "ez" to 26,
        "daniel" to 27, "dan" to 27, "dan." to 27, "dn" to 27,
        "oseas" to 28, "ose" to 28, "ose." to 28, "os" to 28,
        "joel" to 29, "jl" to 29,
        "amós" to 30, "amos" to 30, "am" to 30,
        "abdías" to 31, "abdias" to 31, "abd" to 31,
        "jonás" to 32, "jonas" to 32, "jon" to 32, "jon." to 32,
        "miqueas" to 33, "miq" to 33, "miq." to 33, "mi" to 33,
        "nahúm" to 34, "nahum" to 34, "nah" to 34, "nah." to 34,
        "habacuc" to 35, "hab" to 35, "hab." to 35,
        "sofonías" to 36, "sofonias" to 36, "sof" to 36, "sof." to 36,
        "hageo" to 37, "hag" to 37, "hag." to 37,
        "zacarías" to 38, "zacarias" to 38, "zac" to 38, "zac." to 38,
        "malaquías" to 39, "malaquias" to 39, "mal" to 39, "mal." to 39,
        "mateo" to 40, "mat" to 40, "mat." to 40, "mt" to 40,
        "marcos" to 41, "mar" to 41, "mar." to 41, "mc" to 41, "mr" to 41,
        "lucas" to 42, "luc" to 42, "luc." to 42, "lc" to 42,
        "juan" to 43, "jn" to 43,
        "hechos" to 44, "hech" to 44, "hech." to 44, "hch" to 44,
        "romanos" to 45, "rom" to 45, "rom." to 45, "ro" to 45,
        "1 corintios" to 46, "1 cor" to 46, "1 cor." to 46, "1co" to 46,
        "2 corintios" to 47, "2 cor" to 47, "2 cor." to 47, "2co" to 47,
        "gálatas" to 48, "galatas" to 48, "gál" to 48, "gal" to 48, "gál." to 48, "gal." to 48, "gá" to 48,
        "efesios" to 49, "efe" to 49, "efe." to 49, "ef" to 49,
        "filipenses" to 50, "fil" to 50, "fil." to 50,
        "colosenses" to 51, "col" to 51, "col." to 51,
        "1 tesalonicenses" to 52, "1 tes" to 52, "1 tes." to 52, "1ts" to 52,
        "2 tesalonicenses" to 53, "2 tes" to 53, "2 tes." to 53, "2ts" to 53,
        "1 timoteo" to 54, "1 tim" to 54, "1 tim." to 54, "1ti" to 54,
        "2 timoteo" to 55, "2 tim" to 55, "2 tim." to 55, "2ti" to 55,
        "tito" to 56, "tit" to 56, "tit." to 56,
        "filemón" to 57, "filemon" to 57, "flm" to 57,
        "hebreos" to 58, "heb" to 58, "heb." to 58, "hb" to 58, "he" to 58,
        "santiago" to 59, "sant" to 59, "sant." to 59, "stg" to 59,
        "1 pedro" to 60, "1 ped" to 60, "1 ped." to 60, "1 pe" to 60, "1p" to 60,
        "2 pedro" to 61, "2 ped" to 61, "2 ped." to 61, "2 pe" to 61, "2p" to 61,
        "1 juan" to 62, "1 jn" to 62,
        "2 juan" to 63, "2 jn" to 63,
        "3 juan" to 64, "3 jn" to 64,
        "judas" to 65, "jud" to 65, "jud." to 65,
        "apocalipsis" to 66, "apoc" to 66, "apoc." to 66, "ap" to 66,
    )

    private val BOOK_NAMES: Map<Int, String> = mapOf(
        1 to "Génesis", 2 to "Éxodo", 3 to "Levítico", 4 to "Números", 5 to "Deuteronomio",
        6 to "Josué", 7 to "Jueces", 8 to "Rut", 9 to "1 Samuel", 10 to "2 Samuel",
        11 to "1 Reyes", 12 to "2 Reyes", 13 to "1 Crónicas", 14 to "2 Crónicas",
        15 to "Esdras", 16 to "Nehemías", 17 to "Ester", 18 to "Job", 19 to "Salmos",
        20 to "Proverbios", 21 to "Eclesiastés", 22 to "Cantares", 23 to "Isaías",
        24 to "Jeremías", 25 to "Lamentaciones", 26 to "Ezequiel", 27 to "Daniel",
        28 to "Oseas", 29 to "Joel", 30 to "Amós", 31 to "Abdías", 32 to "Jonás",
        33 to "Miqueas", 34 to "Nahúm", 35 to "Habacuc", 36 to "Sofonías", 37 to "Hageo",
        38 to "Zacarías", 39 to "Malaquías", 40 to "Mateo", 41 to "Marcos", 42 to "Lucas",
        43 to "Juan", 44 to "Hechos", 45 to "Romanos", 46 to "1 Corintios", 47 to "2 Corintios",
        48 to "Gálatas", 49 to "Efesios", 50 to "Filipenses", 51 to "Colosenses",
        52 to "1 Tesalonicenses", 53 to "2 Tesalonicenses", 54 to "1 Timoteo", 55 to "2 Timoteo",
        56 to "Tito", 57 to "Filemón", 58 to "Hebreos", 59 to "Santiago",
        60 to "1 Pedro", 61 to "2 Pedro", 62 to "1 Juan", 63 to "2 Juan", 64 to "3 Juan",
        65 to "Judas", 66 to "Apocalipsis",
    )

    private val DIACRITICS = Regex("\\p{Mn}+")
    private fun nfc(s: String) = DIACRITICS.replace(Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD), "").trim()

    fun bookName(num: Int): String = BOOK_NAMES[num] ?: ""

    fun resolveBook(raw: String): Int? {
        val lo = raw.lowercase().trim()
        BOOK_MAP[lo]?.let { return it }
        val stripped = nfc(lo)
        for ((k, v) in BOOK_MAP) if (nfc(k) == stripped) return v
        return null
    }

    // (optional number prefix) BookName chapter:verse (optional "al/-/y endVerse")
    private val REF_RE = Regex("(?:(\\d)\\s+)?([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\.?)\\s+(\\d+)\\s*:\\s*(\\d+)(?:\\s*(?:al|-|–|y|,)\\s*(\\d+))?")
    private val CONT_RE = Regex("^\\s*;\\s*(\\d+)\\s*:\\s*(\\d+)(?:\\s*(?:al|-|–|y|,)\\s*(\\d+))?")

    fun findRefs(text: String): List<ParsedRef> {
        val refs = mutableListOf<ParsedRef>()
        var m = REF_RE.find(text)
        while (m != null) {
            val numPrefix = m.groups[1]?.value
            val bookPart = m.groups[2]!!.value
            val rawBook = if (numPrefix != null) "$numPrefix $bookPart" else bookPart
            val bookNum = resolveBook(rawBook)
            var end = m.range.last + 1
            if (bookNum != null) {
                val from = m.groups[4]!!.value.toInt()
                val sections = mutableListOf(RefSection(m.groups[3]!!.value.toInt(), from, m.groups[5]?.value?.toInt() ?: from))
                var display = m.value.trim()
                var rest = text.substring(end)
                var cont = CONT_RE.find(rest)
                while (cont != null) {
                    val cFrom = cont.groups[2]!!.value.toInt()
                    sections += RefSection(cont.groups[1]!!.value.toInt(), cFrom, cont.groups[3]?.value?.toInt() ?: cFrom)
                    display = (display + cont.value).trim()
                    end += cont.value.length
                    rest = text.substring(end)
                    cont = CONT_RE.find(rest)
                }
                refs += ParsedRef(bookNum, BOOK_NAMES[bookNum] ?: rawBook, sections, display, m.range.first, end)
            }
            m = REF_RE.find(text, end)
        }
        return refs
    }
}
