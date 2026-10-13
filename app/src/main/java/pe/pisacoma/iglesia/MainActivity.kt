package pe.pisacoma.iglesia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pe.pisacoma.iglesia.nav.AppNavHost
import pe.pisacoma.iglesia.nav.Route

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // A deep link https://quiz-qwik.vercel.app/<path> opens the matching screen.
        val start = intent?.data?.path?.let { Route.parse(it) } ?: Route.Home
        setContent { AppNavHost(start = start) }
    }
}
