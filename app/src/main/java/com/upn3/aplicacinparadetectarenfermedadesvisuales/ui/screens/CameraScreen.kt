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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiseaseCatalog
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppLanguage
import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.LocalAppStrings
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.CameraPreview
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.ViewfinderOverlay
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.AppTopBar
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.MedicalDisclaimerBanner
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.components.ProbabilityMeterRow
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderAlert
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderPositioning
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderReady
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.AnalysisUiState
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.CameraAnalysisViewModel

/**
 * Unica responsabilidad: presentar el flujo de captura/analisis y reaccionar al [AnalysisUiState]
 * del ViewModel. No preprocesa imagenes, no corre inferencia y no aplica reglas clinicas: solo
 * delega en el ViewModel y renderiza lo que este expone.
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
    val uiState by viewModel.uiState.collectAsState()

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
        val successState = uiState as? AnalysisUiState.Success ?: return
        val best = successState.results.firstOrNull() ?: return
        historyRepository.record(best)
        navController.navigate("analysis_result/${best.code}/${best.confidence}/${best.riskLevel.name}")
    }

    val ringColor = when (uiState) {
        is AnalysisUiState.Success -> ViewfinderReady
        is AnalysisUiState.Failed -> ViewfinderAlert
        else -> ViewfinderPositioning
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
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (uploadedImage != null) {
                    Image(
                        bitmap = uploadedImage!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    CameraPreview(onFrameCaptured = { bitmap, rotation -> viewModel.analyze(bitmap, rotation) })
                    ViewfinderOverlay(ringColor = ringColor, modifier = Modifier.fillMaxSize())
                }

                MedicalDisclaimerBanner(
                    text = strings.disclaimerText,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(20.dp)
                ) {
                    when (val state = uiState) {
                        is AnalysisUiState.Idle, is AnalysisUiState.Analyzing -> {
                            Text(
                                text = strings.analyzingMessage,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        is AnalysisUiState.Failed -> {
                            Text(
                                text = "${strings.errorPrefix}: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        is AnalysisUiState.Success -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.results.take(3).forEach { result ->
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
