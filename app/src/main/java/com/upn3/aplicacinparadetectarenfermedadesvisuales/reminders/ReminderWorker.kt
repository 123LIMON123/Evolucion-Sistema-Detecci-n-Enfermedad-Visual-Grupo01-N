package com.upn3.aplicacinparadetectarenfermedadesvisuales.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.upn3.aplicacinparadetectarenfermedadesvisuales.MainActivity
import com.upn3.aplicacinparadetectarenfermedadesvisuales.R
import java.util.Locale
import java.util.concurrent.TimeUnit

const val REMINDER_CHANNEL_ID = "eye_checkup_reminders"
private const val REMINDER_WORK_NAME = "periodic_eye_checkup_reminder"
private const val REMINDER_NOTIFICATION_ID = 1001
private val REMINDER_INTERVAL_DAYS = 30L

/**
 * [Principio S - SRP] Unica responsabilidad: crear y mostrar la notificacion del recordatorio
 * periodico de chequeo ocular. No sabe nada de la preferencia del usuario (eso lo decide
 * `ReminderScheduler` antes de programarlo); solo sabe como construir y mostrar la notificacion.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // El permiso se pidio al activar el switch, pero pudo haber sido revocado despues
            // desde Ajustes del sistema; sin el, mostrar la notificacion lanzaria una excepcion.
            return
        }

        val isSpanish = Locale.getDefault().language == "es"
        val title = if (isSpanish) "Hora de tu chequeo ocular" else "Time for your eye checkup"
        val text = if (isSpanish) {
            "Te recomendamos hacerte un nuevo analisis con OcuCheck AI."
        } else {
            "We recommend running a new analysis with OcuCheck AI."
        }

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            android.content.Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(REMINDER_NOTIFICATION_ID, notification)
    }
}

/**
 * [Principio S - SRP] Unica responsabilidad: crear el canal de notificacion (una vez, al
 * arrancar la app) y programar/cancelar el trabajo periodico segun la preferencia del usuario.
 */
object ReminderScheduler {

    fun createNotificationChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val isSpanish = Locale.getDefault().language == "es"
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            if (isSpanish) "Recordatorios de chequeo ocular" else "Eye checkup reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = if (isSpanish) {
                "Avisos periodicos para recordarte hacerte un nuevo analisis."
            } else {
                "Periodic reminders to run a new eye analysis."
            }
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(REMINDER_INTERVAL_DAYS, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }
}
