package com.a1exymoroz.mergefruit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : AppCompatActivity() {

    /** Deep link that launched or resumed the activity (e.g. mergefruit://verify?token=...). */
    private val deepLinkUri = MutableStateFlow<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        deepLinkUri.value = intent.takeIf { it.action == Intent.ACTION_VIEW }?.data

        val container = (application as MergeFruitApplication).container
        setContent {
            MergeFruitApp(
                container = container,
                deepLinkUri = deepLinkUri,
                onDeepLinkConsumed = { deepLinkUri.value = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            deepLinkUri.value = intent.data
        }
    }
}
