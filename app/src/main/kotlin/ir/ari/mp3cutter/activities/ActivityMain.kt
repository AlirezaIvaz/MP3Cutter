package ir.ari.mp3cutter.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.ari.mp3cutter.databinding.ActivityMainBinding

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
    }
}