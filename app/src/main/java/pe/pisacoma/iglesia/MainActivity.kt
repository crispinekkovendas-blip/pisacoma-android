package pe.pisacoma.iglesia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pe.pisacoma.iglesia.data.prefs.UserState
import pe.pisacoma.iglesia.nav.AppNavHost
import pe.pisacoma.iglesia.nav.Route

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // A deep link https://quiz-qwik.vercel.app/<path> opens the matching screen.
        val deepLink = intent?.data?.path?.let { Route.parse(it) }
        // Until the phone has chosen what to keep, the only screen is /descargas; «Continuar» then
        // goes where the open was headed.
        val firstRun = App.repos.userState.string(UserState.Keys.contentMode) == null
        val start = if (firstRun) Route.Descargas else deepLink ?: Route.Home
        setContent { AppNavHost(start = start, after = if (firstRun) deepLink else null) }
    }
}
