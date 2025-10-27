package com.example.appmovilfitquality.util

import android.content.Context
import android.net.Uri


fun canTryDelete(context: Context, uri: Uri): Boolean {
    if (uri.authority == "media" || uri.toString().startsWith("content://media/")) return true
    return toMediaStoreUri(context, uri) != null
}