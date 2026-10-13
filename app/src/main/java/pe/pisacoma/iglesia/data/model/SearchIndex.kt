package pe.pisacoma.iglesia.data.model

/** Typed view of the site's search index (utils/search.ts SearchIndex). */
data class SearchIndex(
    val v: Int,
    val quarter: Quarter,
    /** variant, number, title, first line of stanza 1 / of the chorus when they differ from the title */
    val hymns: List<HymnRow>,
    val egw: List<EgwRow>,
    val lessons: List<LessonRow>,
    /** book number, name, chapter count, abbreviation */
    val bible: List<BibleRow>,
) {
    data class Quarter(val id: String, val title: String)
    data class HymnRow(val variant: String, val number: Int, val title: String, val lines: List<String>)
    data class EgwRow(val s: String, val t: String, val c: List<Pair<Int, String>>)
    data class LessonRow(val n: Int, val t: String, val d: List<String>)
    data class BibleRow(val num: Int, val name: String, val chapters: Int, val abbr: String)
}
