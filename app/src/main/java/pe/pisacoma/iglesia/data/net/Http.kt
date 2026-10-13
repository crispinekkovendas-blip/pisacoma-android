package pe.pisacoma.iglesia.data.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/** One OkHttp client for the live bits (YouTube feed, Wiktionary, the announcements sheet): 8 s timeouts. */
object Http {
    /** Tests flip this to simulate no signal; every call then fails fast with an IOException. */
    @Volatile var forceOffline: Boolean = false

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    class HttpStatus(val code: Int) : IOException("HTTP $code")

    /** GET → body text; throws [HttpStatus] on non-2xx and IOException on network trouble. */
    suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        if (forceOffline) throw IOException("offline (forced by test)")
        val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0 Pisacoma-Android").build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw HttpStatus(res.code)
            res.body?.string() ?: ""
        }
    }
}
