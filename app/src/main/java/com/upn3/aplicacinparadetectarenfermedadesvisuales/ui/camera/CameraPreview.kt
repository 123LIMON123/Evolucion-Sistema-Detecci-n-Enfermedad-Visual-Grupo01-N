package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

private const val MIN_MS_BETWEEN_FRAMES = 1000L

/**
 * Unica responsabilidad: mostrar el preview en vivo de CameraX y entregar frames muestreados.
 * No sabe nada de modelos, preprocesamiento ni reglas clinicas: solo produce Bitmaps + rotacion.
 */
@Composable
fun CameraPreview(onFrameCaptured: (Bitmap, Int) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    var lastFrameTimeMs by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    LaunchedEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(cameraExecutor) { imageProxy ->
                            val now = System.currentTimeMillis()
                            if (now - lastFrameTimeMs >= MIN_MS_BETWEEN_FRAMES) {
                                try {
                                    val bitmap = imageProxy.toBitmap()
                                    val rotation = imageProxy.imageInfo.rotationDegrees
                                    onFrameCaptured(bitmap, rotation)
                                    lastFrameTimeMs = now
                                } catch (e: Exception) {
                                    Log.e("CameraPreview", "Error procesando frame: ${e.message}")
                                }
                            }
                            imageProxy.close()
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error al iniciar camara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}
