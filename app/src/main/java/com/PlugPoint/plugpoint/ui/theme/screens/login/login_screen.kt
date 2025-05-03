package com.PlugPoint.plugpoint.ui.theme.screens.login

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.PlugPoint.plugpoint.ui.theme.blue
import com.PlugPoint.plugpoint.ui.theme.lightSlateGray
import com.PlugPoint.plugpoint.ui.theme.neworange
import com.PlugPoint.plugpoint.ui.theme.neworange1

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AnimatedSubtleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login to PlugPoint",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StyledTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.MailOutline,
                    keyboardType = KeyboardType.Email
                )

                StyledTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        color = blue,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    GradientButton(
                        text = "Login",
                        gradient = Brush.horizontalGradient(listOf(neworange, blue)),
                        onClick = {
                            isLoading = true
                            viewModel.loginUser(
                                email = email,
                                password = password,
                                onNavigateToProfile = { profileRoute ->
                                    isLoading = false
                                    snackbarMessage = "Login successful"
                                    navController.navigate(profileRoute)
                                },
                                onLoginError = { error ->
                                    isLoading = false
                                    snackbarMessage = error
                                }
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Register Here",
                        color = blue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate("roles") }
                    )
                }
            }
        }

        snackbarMessage?.let { message ->
            Snackbar(
                action = {
                    TextButton(onClick = { snackbarMessage = null }) {
                        Text("Dismiss", color = Color.White)
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = message, color = Color.White)
            }
        }
    }
}

@Composable
fun AnimatedSubtleBackground() {
    val transition = rememberInfiniteTransition()
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(20000, easing = LinearEasing)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blue.copy(alpha = 0.4f),
                        neworange.copy(alpha = 0.4f),
                        Color.Black
                    ),
                    center = Offset(offset % 1000, offset % 2000),
                    radius = 800f
                )
            )
            .blur(100.dp)
            .alpha(0.5f)
    )
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label, color = lightSlateGray)
        },
        singleLine = true,
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = blue)
        },
        trailingIcon = if (isPassword) {
            {
                val iconTint = if (isPasswordVisible) blue else Color.LightGray
                Text(
                    text = if (isPasswordVisible) "Hide" else "Show",
                    color = iconTint,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isPasswordVisible = !isPasswordVisible
                        }
                )
            }
        } else null,
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = blue,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = blue,
            cursorColor = blue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
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
            .height(56.dp)
            .shadow(6.dp, shape = RoundedCornerShape(14.dp))
            .background(brush = gradient, shape = RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(rememberNavController(), viewModel = AuthViewModel())
}