package ir.ari.mp3cutter.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import ir.ari.mp3cutter.R
import ir.ari.mp3cutter.databinding.ActivityMainBinding
import ir.ari.mp3cutter.utils.isStoragePermissionGranted
import ir.ari.mp3cutter.utils.toString

class ActivityMain : AppCompatActivity() {
    private val activityMain = this@ActivityMain
    private lateinit var binding: ActivityMainBinding

    private val requestStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= 30) {
                if (Environment.isExternalStorageManager()) {
                    restartActivity()
                } else {
                    Snackbar.make(
                        binding.root,
                        String.format(
                            R.string.warning_permission_denied.toString(activityMain),
                            R.string.permission_storage.toString(activityMain)
                        ),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.action_grant) {
                            requestStoragePermission()
                        }
                        .show()
                }
            }
        }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${packageName}")
                requestStoragePermissionResult.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                requestStoragePermissionResult.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this@ActivityMain,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> if (grantResults.isNotEmpty()) {
                if (isStoragePermissionGranted) {
                    restartActivity()
                } else {
                    Snackbar.make(
                        binding.root,
                        String.format(
                            R.string.warning_permission_denied.toString(activityMain),
                            R.string.permission_storage.toString(activityMain)
                        ),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.action_grant) {
                            requestStoragePermission()
                        }
                        .show()
                }
            }
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if (getSharedPreferences("remember", Activity.MODE_PRIVATE).getBoolean(
                "check_permission",
                true
            )
        ) {
            if (isStoragePermissionGranted) {
                val status = Environment.getExternalStorageState()
                when {
                    status == Environment.MEDIA_MOUNTED_READ_ONLY -> {
                        MaterialAlertDialogBuilder(activityMain)
                            .setTitle(R.string.error_bad_exception)
                            .setMessage(R.string.error_sdcard_readonly)
                            .setPositiveButton(R.string.action_ok) { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                        return
                    }
                    status == Environment.MEDIA_SHARED -> {
                        MaterialAlertDialogBuilder(activityMain)
                            .setTitle(R.string.error_bad_exception)
                            .setMessage(R.string.error_sdcard_shared)
                            .setPositiveButton(R.string.action_ok) { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                        return
                    }
                    status != Environment.MEDIA_MOUNTED -> {
                        MaterialAlertDialogBuilder(activityMain)
                            .setTitle(R.string.error_bad_exception)
                            .setMessage(R.string.error_no_sdcard)
                            .setPositiveButton(R.string.action_ok) { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                        return
                    }
                }
                startActivity()
            } else {
                val linearLayout = LinearLayout(activityMain)
                val checkbox = CheckBox(activityMain)
                checkbox.setText(R.string.action_dismiss_forever)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.marginStart = 50
                checkbox.setOnCheckedChangeListener { _, b ->
                    getSharedPreferences("remember", Activity.MODE_PRIVATE).edit()
                        .putBoolean("check_permission", !b).apply()
                }
                linearLayout.addView(checkbox, params)
                MaterialAlertDialogBuilder(activityMain)
                    .setCancelable(false)
                    .setTitle(R.string.attention)
                    .setMessage(R.string.storage_permission_request)
                    .setView(linearLayout)
                    .setPositiveButton(R.string.action_grant) { _, _ ->
                        requestStoragePermission()
                    }
                    .setNegativeButton(R.string.action_dismiss) { _, _ ->
                        startActivity()
                    }
                    .show()
            }
        } else {
            startActivity()
        }
    }

    private fun startActivity() {
        // TODO:
    }

    private fun restartActivity() {
        // TODO:
    }

}