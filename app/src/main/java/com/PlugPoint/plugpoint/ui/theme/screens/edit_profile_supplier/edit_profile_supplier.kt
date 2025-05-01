package com.PlugPoint.plugpoint.ui.theme.screens.edit_profile_supplier

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
fun EditProfileSupplierScreen(
    firstName: String = "Jane",
    lastName: String = "Doe",
    companyName: String = "PlugPoint Ltd",
    idNumber: String = "12345678",
    county: String = "Nairobi",
    category: String = "Technology",
    email: String = "jane.doe@example.com"
) {
    var updatedFirstName by remember { mutableStateOf(firstName) }
    var updatedLastName by remember { mutableStateOf(lastName) }
    var updatedCompanyName by remember { mutableStateOf(companyName) }
    var updatedIdNumber by remember { mutableStateOf(idNumber) }
    var updatedCounty by remember { mutableStateOf(county) }
    var updatedCategory by remember { mutableStateOf(category) }
    var updatedEmail by remember { mutableStateOf(email) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neworange),
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
                    .background(neworange1, shape = CircleShape),
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
                    focusedIndicatorColor = neworange,
                    unfocusedIndicatorColor = neworange1
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
                    focusedIndicatorColor = neworange,
                    unfocusedIndicatorColor = neworange1
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Company Name
            OutlinedTextField(
                value = updatedCompanyName,
                onValueChange = { updatedCompanyName = it },
                label = { Text("Company Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = neworange,
                    unfocusedIndicatorColor = neworange1
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
                    focusedIndicatorColor = neworange,
                    unfocusedIndicatorColor = neworange1
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // County
            OutlinedTextField(
                value = updatedCounty,
                onValueChange = { updatedCounty = it },
                label = { Text("County") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = neworange,
                    unfocusedIndicatorColor = neworange1
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Category
            OutlinedTextField(
                value = updatedCategory,
                onValueChange = { updatedCategory = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = neworange,
                    unfocusedIndicatorColor = neworange1
                )
            )
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
                    focusedIndicatorColor = neworange,
                    unfocusedIndicatorColor = neworange1
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
                    colors = ButtonDefaults.buttonColors(containerColor = neworange)
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
private fun edit_profile_supplier() {
    EditProfileSupplierScreen()
}