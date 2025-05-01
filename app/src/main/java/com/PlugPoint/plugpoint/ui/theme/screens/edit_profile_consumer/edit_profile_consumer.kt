package com.PlugPoint.plugpoint.ui.theme.screens.edit_profile_consumer



import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.PlugPoint.plugpoint.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfileConsumerScreen(
    firstName: String = "John",
    lastName: String = "Doe",
    idNumber: String = "12345678",
    county: String = "Nairobi",
    category: String = "Technology",
    email: String = "john.doe@example.com"
) {
    var updatedFirstName by remember { mutableStateOf(firstName) }
    var updatedLastName by remember { mutableStateOf(lastName) }
    var updatedIdNumber by remember { mutableStateOf(idNumber) }
    var updatedCounty by remember { mutableStateOf(county) }
    var countySearchQuery by remember { mutableStateOf("") }
    var expandedCountyDropdown by remember { mutableStateOf(false) }
    var updatedCategory by remember { mutableStateOf(category) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var updatedEmail by remember { mutableStateOf(email) }

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
                title = { Text("Edit Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = blue),
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(newwhite),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(blue1, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    tint = Color.White,
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // First Name
            OutlinedTextField(
                value = updatedFirstName,
                onValueChange = { updatedFirstName = it },
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

            // Last Name
            OutlinedTextField(
                value = updatedLastName,
                onValueChange = { updatedLastName = it },
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

            // ID Number
            OutlinedTextField(
                value = updatedIdNumber,
                onValueChange = { updatedIdNumber = it },
                label = { Text("ID Number/Passport No") },
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

            // County Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = updatedCounty,
                    onValueChange = { updatedCounty = it },
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
                        LazyColumn {
                            items(kenyanCounties.filter { it.contains(countySearchQuery, ignoreCase = true) }) { filteredCounty ->
                                DropdownMenuItem(
                                    text = { Text(text = filteredCounty) },
                                    onClick = {
                                        updatedCounty = filteredCounty
                                        expandedCountyDropdown = false
                                        countySearchQuery = "" // Reset search query
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Category Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = updatedCategory,
                    onValueChange = { updatedCategory = it },
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
                DropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false }
                ) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                updatedCategory = item
                                expandedCategoryDropdown = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Email Address
            OutlinedTextField(
                value = updatedEmail,
                onValueChange = { updatedEmail = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = blue,
                    unfocusedIndicatorColor = blue1
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* Handle save logic */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text(text = "Save", color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Handle cancel logic */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text(text = "Cancel", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview
@Composable
private fun edit_profile_consumer_preview() {
    EditProfileConsumerScreen()
}