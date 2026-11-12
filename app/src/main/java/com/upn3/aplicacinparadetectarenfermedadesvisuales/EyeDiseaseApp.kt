package com.upn3.aplicacinparadetectarenfermedadesvisuales

import android.app.Application
import com.upn3.aplicacinparadetectarenfermedadesvisuales.di.AppContainer
import com.upn3.aplicacinparadetectarenfermedadesvisuales.reminders.ReminderScheduler

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
        // El canal debe existir antes de poder mostrar cualquier notificacion; crearlo de nuevo
        // en cada arranque no hace nada si ya existe (API de Android, no cuesta nada llamarlo).
        ReminderScheduler.createNotificationChannelIfNeeded(this)
    }
}
