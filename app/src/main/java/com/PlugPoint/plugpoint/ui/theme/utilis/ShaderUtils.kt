package com.PlugPoint.plugpoint.ui.theme.utilis


import android.content.Context
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.PlugPoint.plugpoint.R
import kotlinx.coroutines.android.awaitFrame

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LiquidBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shaderCode = remember { loadShaderFromRaw(context, R.raw.molten_background_shader) }
    val shader = remember { RuntimeShader(shaderCode) }

    var time by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            time += 1f / 60f
            awaitFrame()
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        LaunchedEffect(time, widthPx, heightPx) {
            shader.setFloatUniform("iResolution", widthPx, heightPx)
            shader.setFloatUniform("iTime", time)
        }

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    renderEffect = android.graphics.RenderEffect
                        .createRuntimeShaderEffect(shader, "iTime")
                        .asComposeRenderEffect()
                }
        )
    }
}

private fun loadShaderFromRaw(context: Context, resId: Int): String {
    return context.resources.openRawResource(resId)
        .bufferedReader()
        .use { it.readText() }
}
