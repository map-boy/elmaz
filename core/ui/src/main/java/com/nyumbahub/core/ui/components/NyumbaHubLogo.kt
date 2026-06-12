package com.nyumbahub.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NyumbaHubLogo(size: Dp = 80.dp) {
    val navy   = Color(0xFF1A3C5E)
    val orange = Color(0xFFE87722)
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(color = navy, radius = w / 2f)
        val roofPath = Path().apply {
            moveTo(w * 0.15f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.85f, h * 0.52f)
            close()
        }
        drawPath(path = roofPath, color = orange, style = Fill)
        drawRect(
            color = Color.White,
            topLeft = Offset(w * 0.28f, h * 0.50f),
            size = Size(w * 0.44f, h * 0.35f)
        )
        drawRect(
            color = navy,
            topLeft = Offset(w * 0.41f, h * 0.62f),
            size = Size(w * 0.18f, h * 0.23f)
        )
    }
}
