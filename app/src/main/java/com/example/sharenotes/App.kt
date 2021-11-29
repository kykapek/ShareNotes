package com.example.sharenotes

import android.app.Application
import com.example.sharenotes.repository.CrimeRepository

class App: Application() {

    override  fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }

}