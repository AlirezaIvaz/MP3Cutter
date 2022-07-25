package ir.ari.mp3cutter.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import ir.ari.mp3cutter.R

fun Int.toString(context: Context): String = context.getString(this)

fun Activity.isPermissionGranted(permission: String): Boolean {
    return if (Build.VERSION.SDK_INT >= 23) ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED else true
}

val Activity.isStoragePermissionGranted: Boolean
    get() = when {
        Build.VERSION.SDK_INT >= 30 -> {
            Environment.isExternalStorageManager()
        }
        Build.VERSION.SDK_INT >= 23 -> {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        else -> true
    }

val Activity.isWriteSettingsPermissionGranted: Boolean
    get() = if (Build.VERSION.SDK_INT >= 23) {
        Settings.System.canWrite(this)
    } else true

val String.typeIcon: Int
get() = when (this) {
    Types.Ringtone -> {
        R.drawable.ic_ringtone
    }
    Types.Alarm -> {
        R.drawable.ic_alarm
    }
    Types.Notification -> {
        R.drawable.ic_notification
    }
    else -> {
        R.drawable.ic_music
    }
}
