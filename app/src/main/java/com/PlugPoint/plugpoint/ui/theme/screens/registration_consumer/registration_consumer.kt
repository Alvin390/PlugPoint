package com.PlugPoint.plugpoint.ui.theme.screens.registration_consumer

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.PlugPoint.plugpoint.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrationConsumerScreen(navController: NavController) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Initialize the image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var county by remember { mutableStateOf("") }
    var countySearchQuery by remember { mutableStateOf("") }
    var expandedCountyDropdown by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val categories = listOf("Construction", "Agriculture", "Technology", "Cosmetics", "Other")
    val kenyanCounties = listOf(
        "Mombasa", "Kwale", "Kilifi", "Tana River", "Lamu", "Taita-Taveta",
        "Garissa", "Wajir", "Mandera", "Marsabit", "Isiolo", "Meru", "Tharaka-Nithi",
        "Embu", "Kitui", "Machakos", "Makueni", "Nyandarua", "Nyeri", "Kirinyaga",
        "Murang'a", "Kiambu", "Turkana", "West Pokot", "Samburu", "Trans-Nzoia",
        "Uasin Gishu", "Elgeyo-Marakwet", "Nandi", "Baringo", "Laikipia", "Nakuru",
        "Narok", "Kajiado", "Kericho", "Bomet", "Kakamega", "Vihiga", "Bungoma",
        "Busia", "Siaya", "Kisumu", "Homa Bay", "Migori", "Kisii", "Nyamira",
        "Nairobi"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consumer Registration", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = blue),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    // Clickable Image Placeholder
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(blue1, shape = CircleShape)
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri == null) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Upload Image",
                                tint = Color.White,
                                modifier = Modifier.size(100.dp)
                            )
                            Text(
                                text = "Tap to upload",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape), // Ensures the image is clipped to a circle
                                contentScale = ContentScale.Crop // Fills the circle while maintaining aspect ratio
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
                item {
                    // Title
                    Text(
                        text = "Create Your Consumer Account",
                        color = blue1,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                item {
                    // First Name
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = blue,
                            unfocusedIndicatorColor = blue1
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    // Last Name
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = blue,
                            unfocusedIndicatorColor = blue1
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    // ID Number
                    OutlinedTextField(
                        value = idNumber,
                        onValueChange = { idNumber = it },
                        label = { Text("ID Number/Passport No") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = blue,
                            unfocusedIndicatorColor = blue1
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    // County Dropdown
                    OutlinedTextField(
                        value = county,
                        onValueChange = { county = it },
                        label = { Text("County") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedCountyDropdown = !expandedCountyDropdown }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Icon"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    // Category Dropdown
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedCategoryDropdown = !expandedCategoryDropdown }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Icon"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    // Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = blue,
                            unfocusedIndicatorColor = blue1
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = blue,
                            unfocusedIndicatorColor = blue1
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Toggle Confirm Password Visibility"
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = blue,
                            unfocusedIndicatorColor = blue1
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    // Register Button
                    Button(
                        onClick = { /* Handle registration logic */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = blue)
                    ) {
                        Text(text = "Register", color = Color.White, fontSize = 16.sp)
                    }
                }
            }

            // DropdownMenu for County
            DropdownMenu(
                expanded = expandedCountyDropdown,
                onDismissRequest = { expandedCountyDropdown = false }
            ) {
                Column {
                    OutlinedTextField(
                        value = countySearchQuery,
                        onValueChange = { countySearchQuery = it },
                        label = { Text("Search County") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        singleLine = true
                    )
                    kenyanCounties.filter {
                        it.contains(countySearchQuery, ignoreCase = true)
                    }.forEach { filteredCounty ->
                        DropdownMenuItem(
                            text = { Text(text = filteredCounty) },
                            onClick = {
                                county = filteredCounty
                                expandedCountyDropdown = false
                                countySearchQuery = "" // Reset search query
                            }
                        )
                    }
                }
            }

            // DropdownMenu for Category
            DropdownMenu(
                expanded = expandedCategoryDropdown,
                onDismissRequest = { expandedCategoryDropdown = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            category = item
                            expandedCategoryDropdown = false
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ConsumerRegisterPreview() {
    RegistrationConsumerScreen(rememberNavController())
}