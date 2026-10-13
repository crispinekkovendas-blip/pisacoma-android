package pe.pisacoma.iglesia.feature.himnario

import kotlinx.serialization.Serializable
import pe.pisacoma.iglesia.data.AppJson
import pe.pisacoma.iglesia.data.prefs.UserState

/** The site's utils/recent-hymns.ts: last six hymns opened, newest first, under `himnario.recent`. */
@Serializable
data class RecentHymn(val variant: String, val number: Int, val title: String, val ts: Long)

object RecentHymns {
    const val MAX = 6

    fun read(state: UserState): List<RecentHymn> = runCatching {
        state.string(UserState.Keys.hymnRecent)?.let { AppJson.decodeFromString<List<RecentHymn>>(it) } ?: emptyList()
    }.getOrDefault(emptyList())

    fun push(state: UserState, variant: String, number: Int, title: String, now: Long = System.currentTimeMillis()) {
        val list = read(state).filter { !(it.variant == variant && it.number == number) }.toMutableList()
        list.add(0, RecentHymn(variant, number, title, now))
        state.set(UserState.Keys.hymnRecent, AppJson.encodeToString(list.take(MAX)))
    }
}
