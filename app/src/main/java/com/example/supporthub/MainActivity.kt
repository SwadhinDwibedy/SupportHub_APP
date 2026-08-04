package com.example.supporthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.supporthub.core.navigation.AppNavGraph
import com.example.supporthub.ui.theme.SupportHubTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SupportHubTheme {
                AppNavGraph()
            }
        }
    }
}