package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiseaseCatalog
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.CameraPreview
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.FrameQuality
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.ViewfinderOverlay
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.ClinicalStatusChip
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.MedicalDisclaimerBanner
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.ProbabilityMeterRow
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.label
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.toClinicalTier
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderAlert
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderPositioning
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderReady
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.AnalysisUiState
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.CameraAnalysisViewModel

/**
 * Unica responsabilidad: presentar el flujo de captura/analisis y reaccionar al [AnalysisUiState]
 * y al [FrameQuality] del ViewModel. No preprocesa imagenes, no corre inferencia y no aplica
 * reglas clinicas: solo delega en el ViewModel y renderiza lo que este expone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraAnalysisViewModel,
    historyRepository: AnalysisHistoryRepository,
    localizationRepository: LocalizationRepository
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    val frameQuality by viewModel.frameQuality.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var uploadedImage by remember { mutableStateOf<Bitmap?>(null) }
    var showDisclaimer by remember { mutableStateOf(true) }
    var previousQuality by remember { mutableStateOf(FrameQuality.POSITIONING) }

    // Se retiene el ultimo resultado exitoso para que el panel no colapse a un simple texto de
    // "analizando" y luego vuelva a expandirse en cada ciclo (cada ~1s mientras el encuadre es
    // bueno): el tamano del panel se mantiene fijo y solo se actualizan los numeros.
    var lastSuccess by remember { mutableStateOf<AnalysisUiState.Success?>(null) }
    LaunchedEffect(uiState) {
        val successState = uiState as? AnalysisUiState.Success
        if (successState != null) {
            lastSuccess = successState
        }
    }

    LaunchedEffect(frameQuality) {
        if (frameQuality == FrameQuality.GOOD && previousQuality != FrameQuality.GOOD) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        previousQuality = frameQuality
    }

    val gallerySelector = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                if (bitmap != null) {
                    uploadedImage = bitmap
                    viewModel.analyze(bitmap)
                }
            } catch (e: Exception) {
                Log.e("CameraScreen", "Error al cargar la foto: ${e.message}")
            }
        }
    }

    fun goToResultDetail() {
        val best = lastSuccess?.results?.firstOrNull() ?: return
        historyRepository.record(best)
        navController.navigate("analysis_result/${best.code}/${best.confidence}/${best.riskLevel.name}")
    }

    val ringColor = when (frameQuality) {
        FrameQuality.GOOD -> ViewfinderReady
        FrameQuality.TOO_DARK, FrameQuality.TOO_BRIGHT, FrameQuality.BLURRY -> ViewfinderAlert
        FrameQuality.POSITIONING -> ViewfinderPositioning
    }
    val qualityHint = when (frameQuality) {
        FrameQuality.POSITIONING -> strings.qualityPositioning
        FrameQuality.TOO_DARK -> strings.qualityTooDark
        FrameQuality.TOO_BRIGHT -> strings.qualityTooBright
        FrameQuality.BLURRY -> strings.qualityBlurry
        FrameQuality.GOOD -> strings.qualityGood
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = strings.cameraTitle,
                language = language,
                onToggleLanguage = { localizationRepository.toggle() },
                backContentDescription = strings.back,
                onBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        if (hasCameraPermission) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (uploadedImage != null) {
                        Image(
                            bitmap = uploadedImage!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        CameraPreview(onFrameCaptured = { bitmap, rotation -> viewModel.onFrameCaptured(bitmap, rotation) })
                        ViewfinderOverlay(ringColor = ringColor, modifier = Modifier.fillMaxSize())

                        Text(
                            text = qualityHint,
                            color = androidx.compose.ui.graphics.Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                                .background(ringColor.copy(alpha = 0.9f), RoundedCornerShape(50))
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }

                    if (showDisclaimer) {
                        MedicalDisclaimerBanner(
                            text = strings.disclaimerText,
                            dismissLabel = strings.disclaimerDismiss,
                            onDismiss = { showDisclaimer = false },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 190.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        .padding(20.dp)
                ) {
                    val successToShow = lastSuccess
                    val errorMessage = (uiState as? AnalysisUiState.Failed)?.message

                    when {
                        successToShow != null -> {
                            val topResult = successToShow.results.firstOrNull()
                            if (topResult != null) {
                                val topDiseaseName = DiseaseCatalog.byCode(topResult.code)?.localized(language)?.name
                                    ?: topResult.code
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = topDiseaseName, style = MaterialTheme.typography.titleMedium)
                                        if (uiState is AnalysisUiState.Analyzing) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    ClinicalStatusChip(
                                        tier = topResult.riskLevel.toClinicalTier(),
                                        label = topResult.riskLevel.label(strings)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                successToShow.results.take(3).forEach { result ->
                                    val diseaseName = DiseaseCatalog.byCode(result.code)?.localized(language)?.name
                                        ?: result.code
                                    ProbabilityMeterRow(name = diseaseName, confidence = result.confidence)
                                }
                            }

                            Button(
                                onClick = { goToResultDetail() },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(strings.viewDetail)
                            }
                        }
                        errorMessage != null -> {
                            Text(
                                text = "${strings.errorPrefix}: $errorMessage",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        else -> {
                            Text(
                                text = strings.analyzingMessage,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Button(
                        onClick = { gallerySelector.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(if (uploadedImage == null) strings.uploadPhoto else strings.uploadAnotherPhoto)
                    }

                    if (uploadedImage != null) {
                        OutlinedButton(
                            onClick = {
                                uploadedImage = null
                                lastSuccess = null
                                viewModel.resetToIdle()
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(strings.backToLiveCamera)
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.cameraPermissionRequired)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { gallerySelector.launch("image/*") }, shape = MaterialTheme.shapes.medium) {
                        Text(strings.uploadPhotoInstead)
                    }

                    if (uploadedImage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            bitmap = uploadedImage!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.height(200.dp),
                            contentScale = ContentScale.Fit
                        )
                        val successState = uiState as? AnalysisUiState.Success
                        successState?.results?.take(3)?.forEach { result ->
                            val diseaseName = DiseaseCatalog.byCode(result.code)?.localized(language)?.name
                                ?: result.code
                            Text(text = "$diseaseName: ${(result.confidence * 100).toInt()}%")
                        }
                        if (successState != null && successState.results.isNotEmpty()) {
                            Button(onClick = { goToResultDetail() }, shape = MaterialTheme.shapes.medium) {
                                Text(strings.viewDetail)
                            }
                        }
                    }
                }
            }
        }
    }
}
