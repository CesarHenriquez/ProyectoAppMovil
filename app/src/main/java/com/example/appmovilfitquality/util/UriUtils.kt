package com.example.appmovilfitquality.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore


fun toMediaStoreUri(context: Context, source: Uri): Uri? {
    val authority = source.authority ?: return null


    if (authority == "media") return source
    if (source.toString().startsWith("content://media/")) return source


    if (authority == "com.android.providers.media.documents") {
        val docId = DocumentsContract.getDocumentId(source) ?: return null
        val parts = docId.split(":")
        if (parts.size != 2) return null
        val type = parts[0] // "image" | "video" | "audio"
        val idStr = parts[1]
        val id = idStr.toLongOrNull() ?: return null

        val collection = when (type) {
            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> null
        } ?: return null

        return ContentUris.withAppendedId(collection, id)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        authority == "com.android.providers.media.photopicker"
    ) {
        return null
    }

    return null
}
