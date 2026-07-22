package com.findwords.app

import android.app.Application
import com.findwords.app.data.GameRepository

class App : Application() {

    lateinit var repository: GameRepository

    override fun onCreate() {
        super.onCreate()
        repository = GameRepository(this)
    }
}