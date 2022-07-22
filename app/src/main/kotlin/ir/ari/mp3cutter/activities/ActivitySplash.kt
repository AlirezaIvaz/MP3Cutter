package ir.ari.mp3cutter.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class ActivitySplash : AppCompatActivity() {
    override fun onCreate(bundle: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(bundle)
        splashScreen.setKeepOnScreenCondition { true }
        startActivity(
            Intent(this@ActivitySplash, ActivityMain::class.java)
        )
        finish()
    }
}