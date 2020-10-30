package org.eu.wp_corp.maps.hdtgo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.eu.wp_corp.maps.R
import kotlin.math.roundToInt


class HDTGoActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hdtdo)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        Handler().postDelayed({
            val container = findViewById<FrameLayout>(R.id.hdtgo_container)
            container.removeAllViews()

            val text = TextView(this)
            val duration = 200 + (Math.random() * 300).roundToInt();
            text.text =
                "Unhandled exception: org.eu.wp_corp.maps.hdtgo.GameplayException: Couldn't start introduction tutorial.\n  Caused by: org.eu.wp_corp.maps.http.HttpError: Endpoint `/honneur_de_tibo` returned a non-200 error code (404 - Honneur not found).\n\n510067420 km² indexed in ${duration}ms"
            container.addView(text)
        }, 3000)

        return;

        when (ContextCompat.checkSelfPermission(this, PERMISSION)) {
            PackageManager.PERMISSION_GRANTED -> {
                displayGame()
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(PERMISSION), 0)
                    setContentView(Spinner(this))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.size == 1) {
            displayGame()
        } else {
            val text = TextView(this)
            text.text = "Permission de géolocalisation non accordée... On peut r faire pour toi..."
            setContentView(text)
        }
    }

    private fun displayGame() {
        val wv = WebView(this)
        wv.loadUrl("https://edgar.bzh")
        wv.settings.javaScriptEnabled = true
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                wv.evaluateJavascript("document.body.style='color:white;background:black'") {}
            }
        }
        setContentView(wv)
    }
}
