package com.upn3.aplicacinparadetectarenfermedadesvisuales

import android.app.Application
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.AppContainer

class EyeDiseaseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
