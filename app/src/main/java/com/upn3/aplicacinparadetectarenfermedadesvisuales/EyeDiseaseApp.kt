package com.upn3.aplicacinparadetectarenfermedadesvisuales

import android.app.Application
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.AppContainer

/**
 * [Principio S - SRP] Unica responsabilidad: crear y exponer el [AppContainer] (composition root)
 * mientras vive el proceso. No arma UI ni contiene logica de negocio.
 */
class EyeDiseaseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
