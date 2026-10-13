package pe.pisacoma.iglesia.data

import pe.pisacoma.iglesia.data.prefs.KeyValueStore

/** In-memory KeyValueStore for JVM tests. */
class MemoryKeyValueStore : KeyValueStore {
    private val map = mutableMapOf<String, String>()
    private val listeners = mutableListOf<(String) -> Unit>()
    override fun get(key: String): String? = map[key]
    override fun put(key: String, value: String?) {
        if (value == null) map.remove(key) else map[key] = value
        listeners.toList().forEach { it(key) }
    }
    override fun all(): Map<String, String> = map.toMap()
    override fun addListener(listener: (String) -> Unit) { listeners += listener }
    override fun removeListener(listener: (String) -> Unit) { listeners -= listener }
}
