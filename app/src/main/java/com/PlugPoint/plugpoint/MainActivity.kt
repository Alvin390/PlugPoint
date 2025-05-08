package com.PlugPoint.plugpoint

import AppNavHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import com.PlugPoint.plugpoint.data.DarkModeViewModel
import com.PlugPoint.plugpoint.networks.ImgurAPI
import com.PlugPoint.plugpoint.ui.theme.PlugPointTheme
import com.PlugPoint.plugpoint.utilis.DarkModeViewModelFactory
import com.PlugPoint.plugpoint.utilis.ImgurViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgur.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val imgurAPI = retrofit.create(ImgurAPI::class.java)

        setContent {
            PlugPointTheme {
                val darkModeViewModel: DarkModeViewModel = viewModel(
                    factory = DarkModeViewModelFactory(applicationContext)
                )
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        darkModeViewModel = darkModeViewModel,
                        imgurAPI = imgurAPI, // Pass imgurAPI here
                        imgurViewModel = viewModel(factory = ImgurViewModelFactory(imgurAPI))
                    )
                }
            }
        }
    }
}
