package com.PlugPoint.plugpoint.utilis

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.PlugPoint.plugpoint.data.ImgurViewModel
import kotlinx.coroutines.launch

/**
 * Centralized image picker and uploader utility for Compose screens.
 * Handles runtime permissions, image picking, and image upload (via ImgurViewModel).
 */
@Composable
fun rememberImagePickerAndUploader(
    imgurViewModel: ImgurViewModel,
    onImageUploaded: (String) -> Unit,
    onError: (String) -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            coroutineScope.launch {
                try {
                    val url = imgurViewModel.uploadImageAndGetUrl(uri, context, ImgurViewModel.AUTHORIZATION)
                    onImageUploaded(url)
                } catch (e: Exception) {
                    onError(e.message ?: "Image upload failed")
                }
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            onError("Permission denied to access images.")
        }
    }

    // Function to launch image picker with permission check
    return remember {
        {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            when {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                    pickImageLauncher.launch("image/*")
                }
                else -> {
                    permissionLauncher.launch(permission)
                }
            }
        }
    }
}
