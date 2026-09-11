package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.theme.ViewfinderScrim

private const val TICK_COUNT = 8
private const val TICK_ARC_DEGREES = 14f

/**
 * Scrim con una apertura circular (aro de alineacion) sobre el preview en vivo, para guiar el
 * encuadre del ojo. El color del aro refleja [FrameQuality] en tiempo real (no hay deteccion real
 * de parpadeo, solo brillo/nitidez): cian mientras se posiciona/evalua, esmeralda cuando la
 * calidad es buena, ambar cuando esta muy oscuro, con glare o borroso.
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

        // Aro segmentado concentrico (reticula), un poco por fuera del aro principal.
        val tickRadius = ringRadiusPx + 14.dp.toPx()
        val tickStroke = Stroke(width = 2.dp.toPx())
        for (i in 0 until TICK_COUNT) {
            val centerAngle = (360f / TICK_COUNT) * i
            val startAngle = centerAngle - TICK_ARC_DEGREES / 2f
            drawArc(
                color = ringColor,
                startAngle = startAngle,
                sweepAngle = TICK_ARC_DEGREES,
                useCenter = false,
                topLeft = Offset(center.x - tickRadius, center.y - tickRadius),
                size = androidx.compose.ui.geometry.Size(tickRadius * 2, tickRadius * 2),
                style = tickStroke
            )
        }
    }
}
