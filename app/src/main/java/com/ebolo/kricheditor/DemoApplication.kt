package com.ebolo.kricheditor

import android.app.Application
import io.paperdb.Paper

class DemoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
    }
}