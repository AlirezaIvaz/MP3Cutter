package ir.ari.mp3cutter.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

fun Int.toString(context: Context): String = context.getString(this)

val Activity.isStoragePermissionGranted: Boolean
    get() = if (Build.VERSION.SDK_INT >= 30) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
