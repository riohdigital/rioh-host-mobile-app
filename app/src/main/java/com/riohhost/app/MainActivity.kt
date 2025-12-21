package com.riohhost.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.riohhost.app.ui.navigation.NavGraph
import com.riohhost.app.ui.theme.RiohHostTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RiohHostTheme {
                NavGraph()
            }
        }
    }
}
