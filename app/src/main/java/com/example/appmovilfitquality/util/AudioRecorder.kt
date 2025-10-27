package com.example.appmovilfitquality.util

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): Uri {
        stop()

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.cacheDir
        outputFile = File.createTempFile("msg_", ".m4a", dir)

        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()
        recorder = r.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(outputFile!!.absolutePath)
            prepare()
            start()
        }
        return Uri.fromFile(outputFile)
    }

    // Devuelve el Uri del archivo si quedó OK, o null si no había nada grabando.
    fun stop(): Uri? {
        val r = recorder ?: return null
        return try {
            r.stop()
            r.release()
            recorder = null
            outputFile?.let { Uri.fromFile(it) }
        } catch (_: Exception) {
            try { r.release() } catch (_: Exception) {}
            recorder = null
            null
        } finally {
            recorder = null
        }
    }

    fun isRecording(): Boolean = recorder != null
}