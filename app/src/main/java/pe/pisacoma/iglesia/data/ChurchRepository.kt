package pe.pisacoma.iglesia.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.pisacoma.iglesia.data.model.Church

/** Facts about the church (site src/data/church.ts), used by the home and Visítanos. */
class ChurchRepository(private val assets: AssetSource) {
    private var church: Church? = null
    private val mutex = Mutex()

    suspend fun church(): Church = mutex.withLock {
        church ?: AppJson.decodeFromString<Church>(assets.read("data/church.json")).also { church = it }
    }
}
