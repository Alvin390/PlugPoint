package com.PlugPoint.plugpoint

import AppNavHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.DarkModeViewModel
import com.PlugPoint.plugpoint.data.ImgurViewModel
import com.PlugPoint.plugpoint.navigation.ROUTE_LOGIN
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.PlugPoint.plugpoint.networks.ImgurAPI
import com.PlugPoint.plugpoint.ui.theme.PlugPointTheme
import com.PlugPoint.plugpoint.utilis.AuthViewModelFactory
import com.PlugPoint.plugpoint.utilis.DarkModeViewModelFactory
import com.PlugPoint.plugpoint.utilis.ImgurViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgur.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val sharedPreferences = getSharedPreferences("PlugPointPrefs", MODE_PRIVATE)
        val imgurAPI = retrofit.create(ImgurAPI::class.java)
        // Use the custom factory to create the DarkModeViewModel
        val darkModeViewModel = ViewModelProvider(
            this,
            DarkModeViewModelFactory(applicationContext)
        ).get(DarkModeViewModel::class.java)

        val userType = sharedPreferences.getString("userType", null)

        setContent {
            val isDarkModeEnabled by darkModeViewModel.isDarkModeEnabled.collectAsState()

            PlugPointTheme (darkTheme = isDarkModeEnabled) {
                val imgurViewModel: ImgurViewModel = viewModel(factory = ImgurViewModelFactory(imgurAPI))
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(imgurViewModel, applicationContext))

                val isUserLoggedIn = authViewModel.isUserLoggedIn()
                val userId = authViewModel.getLoggedInUserId()

                val startDestination = if (isUserLoggedIn && userId != null && userType != null) {
                    if (userType == "supplier") {
                        "$ROUTE_PROFILE_SUPPLIER/$userId"
                    } else {
                        "$ROUTE_PROFILE_CONSUMER/$userId"
                    }
                } else {
                    ROUTE_LOGIN
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        navController = rememberNavController(),
                        imgurViewModel = imgurViewModel,
                        darkModeViewModel = darkModeViewModel,
                        imgurAPI = imgurAPI,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}