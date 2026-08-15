package com.kbdmouse.app

import android.app.Application
import com.kbdmouse.app.net.ConnectionManager

class KbdApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ConnectionManager.init(this)
    }
}
