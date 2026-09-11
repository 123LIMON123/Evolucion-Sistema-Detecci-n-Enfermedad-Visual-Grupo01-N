package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderScrim

/**
 * Scrim con una apertura circular (aro de alineacion) sobre el preview en vivo, para guiar el
 * encuadre del ojo. El color del aro es la unica senal dinamica (no hay deteccion real de
 * enfoque/brillo/parpadeo): refleja el estado de analisis para dar feedback inmediato.
 */
@Composable
fun ViewfinderOverlay(ringColor: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        val ringRadiusPx = 120.dp.toPx()
        drawRect(color = ViewfinderScrim)
        drawCircle(
            color = Color.Transparent,
            radius = ringRadiusPx,
            center = center,
            blendMode = BlendMode.Clear
        )
        drawCircle(
            color = ringColor,
            radius = ringRadiusPx,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
