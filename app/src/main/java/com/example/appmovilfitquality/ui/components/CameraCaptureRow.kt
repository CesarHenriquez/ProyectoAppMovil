package com.example.appmovilfitquality.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.appmovilfitquality.util.CameraUtils

@Composable
fun CameraCaptureRow(
    onImageReady: (uriString: String) -> Unit
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    //  Tomar foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) pendingCameraUri?.toString()?.let(onImageReady)
    }

    //  Permiso de cámara
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = CameraUtils.createImageUri(context)
            pendingCameraUri = uri
            if (uri != null) takePictureLauncher.launch(uri)
        }

    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            val hasCam = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCam) {
                val uri = CameraUtils.createImageUri(context)
                pendingCameraUri = uri
                if (uri != null) takePictureLauncher.launch(uri)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) { Text("Tomar foto") }
    }
}