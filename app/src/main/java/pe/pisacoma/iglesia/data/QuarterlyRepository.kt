package pe.pisacoma.iglesia.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.pisacoma.iglesia.data.model.Quarterly

/** The bundled Sabbath School quarterly (the one the site's src/data/quarterly.ts points to). */
class QuarterlyRepository(private val assets: AssetSource) {
    private var quarterly: Quarterly? = null
    private val mutex = Mutex()

    suspend fun quarterly(): Quarterly = mutex.withLock {
        quarterly ?: AppJson.decodeFromString<Quarterly>(assets.read("data/escuela-sabatica.json")).also { quarterly = it }
    }
}
