package com.example.appmovilfitquality.ui.components

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.documentfile.provider.DocumentFile


@Composable
fun DeleteFromGalleryButton(
    imageUriString: String,
    onDeleted: () -> Unit,
    label: String = "Eliminar de galería",
    context: Context? = null
) {
    val ctx = context ?: androidx.compose.ui.platform.LocalContext.current

    Button(
        onClick = {
            val uri = runCatching { Uri.parse(imageUriString) }.getOrNull()
            if (uri == null) {
                Toast.makeText(ctx, "URI inválida", Toast.LENGTH_SHORT).show()
                return@Button
            }

            val deleted = tryDelete(ctx, uri)
            if (deleted) {
                Toast.makeText(ctx, "Imagen eliminada", Toast.LENGTH_SHORT).show()
                onDeleted()
            } else {
                Toast.makeText(ctx, "No se pudo eliminar la imagen", Toast.LENGTH_SHORT).show()
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00B01D),
            contentColor = Color.White
        )
    ) {
        Text(label)
    }
}

private fun tryDelete(context: Context, uri: Uri): Boolean {
    val cr: ContentResolver = context.contentResolver


    runCatching {
        val rows = cr.delete(uri, null, null)
        if (rows > 0) return true
    }


    val df = DocumentFile.fromSingleUri(context, uri)
    if (df != null) {
        runCatching {
            if (df.exists() && df.canWrite()) {
                if (df.delete()) return true
            }
        }
    }


    return false
}