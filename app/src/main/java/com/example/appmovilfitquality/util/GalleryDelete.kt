package com.example.appmovilfitquality.util

import android.content.Context
import android.net.Uri


fun deleteFromGallery(context: Context, uriString: String): Boolean {
    return runCatching {
        val rows = context.contentResolver.delete(Uri.parse(uriString), null, null)
        rows > 0
    }.getOrDefault(false)

}