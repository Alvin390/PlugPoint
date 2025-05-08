package com.PlugPoint.plugpoint.ui.theme.screens.settings_screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.R
import com.PlugPoint.plugpoint.data.DarkModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, darkModeViewModel: DarkModeViewModel) {
    val isDarkModeEnabled by darkModeViewModel.isDarkModeEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(WindowInsets.statusBars.asPaddingValues())
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Biometric Login Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_biometric),
                    contentDescription = "Biometric Login"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Biometric Login", modifier = Modifier.weight(1f))
                Switch(
                    checked = false, // Placeholder, will be implemented later
                    onCheckedChange = {}
                )
            }

            // Dark Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_moon),
                    contentDescription = "Dark Mode"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkModeEnabled,
                    onCheckedChange = { darkModeViewModel.toggleDarkMode(it) }
                )
            }
        }
    }
}

//@Preview
//@Composable
//private fun settings_prev() {
//    SettingsScreen(rememberNavController(), DarkModeViewModel())
//}
