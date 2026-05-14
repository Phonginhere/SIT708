package com.example.p71

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid requires a custom user-agent for tile downloads;
        // default UAs are rate-limited or blocked by OSM tile servers.
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)
    }
}