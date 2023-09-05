package com.android.sharefiles

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class MainFileProvider : FileProvider() {
    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"

        fun Context.getUriForFile(file: File): Uri =
            getUriForFile(this, AUTHORITY, file)

        val Context.realImagesPath
            get() = File(externalCacheDir, "real_images_path").apply {
                mkdirs()
            }
    }
}