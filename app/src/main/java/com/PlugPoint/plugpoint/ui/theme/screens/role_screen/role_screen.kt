package com.PlugPoint.plugpoint.ui.theme.screens.role_screen

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.navigation.ROUTE_REGISTRATION_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_REGISTRATION_SUPPLIER
import com.PlugPoint.plugpoint.ui.theme.blue
import com.PlugPoint.plugpoint.ui.theme.neworange
import com.PlugPoint.plugpoint.ui.theme.neworange1
import kotlin.math.sin

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RoleSelectionScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to PlugPoint",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Consumer Button
            GradientButton(
                text = "I am a Consumer",
                gradient = Brush.horizontalGradient(listOf(blue, blue.copy(alpha = 0.7f))),
                onClick = { navController.navigate(ROUTE_REGISTRATION_CONSUMER) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Supplier Button
            GradientButton(
                text = "I am a Supplier",
                gradient = Brush.horizontalGradient(listOf(neworange1, neworange.copy(alpha = 0.7f))),
                onClick =  { navController.navigate(ROUTE_REGISTRATION_SUPPLIER) }
            )
        }
    }
}

@Composable
fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(blue, neworange),
                    start = Offset(xOffset, 0f),
                    end = Offset(xOffset - 1000f, 2000f)
                )
            )
            .blur(80.dp) // soft glow effect
            .alpha(0.6f)
    )
}

@Composable
fun GradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(10.dp, RoundedCornerShape(16.dp))
            .background(brush = gradient, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun role_selection_preview() {
    RoleSelectionScreen(rememberNavController())
}