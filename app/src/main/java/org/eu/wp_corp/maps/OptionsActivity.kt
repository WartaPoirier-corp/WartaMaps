package org.eu.wp_corp.maps

import android.os.Bundle
import android.transition.Slide
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity


class OptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_options)

        findViewById<View>(R.id.status_bar).apply {
            val lp = layoutParams
            lp.height = statusBarHeight()
            layoutParams = lp
        }

        window.enterTransition = Slide().apply {
            duration = 300
        }

        window.exitTransition = Slide().apply {
            duration = 300
        }
    }

    private fun statusBarHeight(): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
}
