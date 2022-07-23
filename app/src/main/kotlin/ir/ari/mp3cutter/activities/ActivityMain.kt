package ir.ari.mp3cutter.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import ir.ari.mp3cutter.R
import ir.ari.mp3cutter.databinding.ActivityMainBinding
import ir.ari.mp3cutter.file.SoundFile
import ir.ari.mp3cutter.models.Sound
import ir.ari.mp3cutter.utils.*

class ActivityMain : AppCompatActivity() {
    private val activityMain = this@ActivityMain
    private lateinit var binding: ActivityMainBinding
    private var filter = ""
    private var showAll = true
    private val sounds: ArrayList<Sound> = arrayListOf()

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
        if (isStoragePermissionGranted) {
            var selection = ""
            val selectionArgsList: ArrayList<String> = arrayListOf()
            if (showAll) {
                selection = "(_DATA LIKE ?)"
                selectionArgsList.add("%")
            } else {
                selection = "("
                SoundFile.getSupportedExtensions().forEach {
                    selectionArgsList.add("%.$it")
                    if (selection.length > 1) {
                        selection += " OR "
                    }
                    selection += "(_DATA LIKE ?)"
                }
                selection += ")"
                selection = "($selection) AND (_DATA NOT LIKE ?)"
                selectionArgsList.add("%espeak-data/scratch%")
            }
            if (filter.isNotEmpty()) {
                filter = "%$filter%";
                selection =
                    "($selection AND ((TITLE LIKE ?) OR (ARTIST LIKE ?) OR (ALBUM LIKE ?)))";
                selectionArgsList.add(filter)
                selectionArgsList.add(filter)
                selectionArgsList.add(filter)
            }
            val selectionArgs: Array<String> =
                selectionArgsList.toArray(arrayOfNulls(selectionArgsList.size))
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.IS_RINGTONE,
                MediaStore.Audio.Media.IS_ALARM,
                MediaStore.Audio.Media.IS_NOTIFICATION,
            )
            sounds.clear()
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            ).use { cursor ->
                while (cursor!!.moveToNext()) {
                    val songId =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    var songArtist =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    if (songArtist.isEmpty() || songArtist.contains("<unknown>")) {
                        songArtist = getString(R.string.unknown_artist)
                    }
                    val songAlbum =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val songTrack =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val songPath =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val songType = when {
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)) != 0 -> {
                            Types.Music
                        }
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)) != 0 -> {
                            Types.Ringtone
                        }
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)) != 0 -> {
                            Types.Alarm
                        }
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)) != 0 -> {
                            Types.Notification
                        }
                        else -> {
                            Types.Unknown
                        }
                    }
                    sounds.add(
                        Sound(
                            songId,
                            songType,
                            songPath,
                            songArtist,
                            songAlbum,
                            songTrack
                        )
                    )
                }
            }

            val soundAdapter =
                RecyclerAdapter(R.layout.layout_item, sounds.size) { holder, position ->
                    val sound = sounds[position]
                    (holder.view(R.id.icon) as ImageView).setImageResource(sound.type.typeIcon)
                    (holder.view(R.id.artist) as TextView).text = sound.artist
                    (holder.view(R.id.album) as TextView).text = sound.album
                    (holder.view(R.id.title) as TextView).text = sound.title
                    (holder.view(R.id.item_layout) as ConstraintLayout).setOnClickListener {
                        // TODO: On item clicked
                    }
                    (holder.view(R.id.edit) as ImageView).setOnClickListener {
                        // TODO: On edit button clicked
                    }
                }
            binding.recyclerView.layoutManager = LinearLayoutManager(this@ActivityMain)
            binding.recyclerView.adapter = soundAdapter
        } else {
            Snackbar.make(
                binding.root,
                String.format(
                    getString(R.string.warning_permission_denied),
                    getString(R.string.permission_storage)
                ),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.action_grant) {
                    requestStoragePermission()
                }
                .show()
        }
    }

    private fun restartActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity()
        }, 1000)
    }

}