package org.eu.wp_corp.maps

import android.app.Application
import android.content.Context

class WartaMapsApplication : Application() {

    val backend by lazy {
        Backend()
    }

}

val Context.backend get() = (this.applicationContext as WartaMapsApplication).backend
