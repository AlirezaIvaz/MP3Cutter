package ir.ari.mp3cutter.activities

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import ir.ari.mp3cutter.R
import ir.ari.mp3cutter.databinding.ActivityMainBinding
import ir.ari.mp3cutter.file.SoundFile
import ir.ari.mp3cutter.models.Item
import ir.ari.mp3cutter.models.Sound
import ir.ari.mp3cutter.utils.*

class ActivityMain : AppCompatActivity() {
    private val activityMain = this@ActivityMain
    private lateinit var binding: ActivityMainBinding
    private var filter = ""
    private val sounds: ArrayList<Sound> = arrayListOf()
    private lateinit var sound: Sound

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

    private val chooseContactResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                try {
                    val contactData: Uri? = it.data?.data
                    val contactId = contactData?.lastPathSegment
                    val projection = arrayOf(
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER
                    )
                    val localCursor: Cursor? =
                        contentResolver.query(contactData!!, projection, null, null, null)
                    localCursor?.moveToFirst()
                    val contactID: String? =
                        localCursor?.getString(localCursor.getColumnIndexOrThrow("_id"))
                    val contactDisplayName: String? =
                        localCursor?.getString(localCursor.getColumnIndexOrThrow("display_name"))
                    val localUri =
                        Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactID)
                    localCursor?.close()
                    val localContentValues = ContentValues()
                    localContentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                    localContentValues.put(
                        ContactsContract.Data.CUSTOM_RINGTONE,
                        sound.path
                    )
                    contentResolver.update(localUri, localContentValues, null, null)
                    Snackbar.make(
                        binding.root,
                        String.format(
                            "Ringtone assigned to \"%1\$s\" successfully!",
                            contactDisplayName
                        ),
                        Snackbar.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {
                    Snackbar.make(
                        binding.root,
                        R.string.error_unknown,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }

    private val requestWriteSettingsPermissionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isWriteSettingsPermissionGranted) {
                try {
                    if (sound.type == Types.Ringtone || sound.type == Types.Notification) {
                        val type =
                            if (sound.type == Types.Notification) RingtoneManager.TYPE_NOTIFICATION
                            else RingtoneManager.TYPE_RINGTONE
                        val typeName =
                            if (sound.type == Types.Notification) R.string.type_notification
                            else R.string.type_ringtone
                        RingtoneManager.setActualDefaultRingtoneUri(
                            activityMain, type,
                            Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${sound.id}")
                        )
                        Snackbar.make(
                            binding.root, String.format(
                                R.string.set_default_success.toString(activityMain),
                                sound.title,
                                typeName.toString(activityMain)
                            ), Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        Snackbar.make(
                            binding.root,
                            R.string.error_action_denied,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(
                        binding.root,
                        R.string.error_unknown,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    binding.root,
                    String.format(
                        R.string.warning_permission_denied.toString(activityMain),
                        R.string.permission_settings.toString(activityMain)
                    ),
                    Snackbar.LENGTH_INDEFINITE
                ).show()
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
                Actions.RequestStoragePermission
            )
        }
    }

    private fun requestWriteSettingsPermission() {
        requestWriteSettingsPermissionResult.launch(
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:$packageName"))
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Actions.RequestStoragePermission -> if (grantResults.isNotEmpty()) {
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
            Actions.RequestContactsPermission -> if (grantResults.isNotEmpty()) {
                if (isPermissionGranted(Manifest.permission.WRITE_CONTACTS)) {
                    chooseContactResult.launch(
                        Intent(Intent.ACTION_PICK).setType(
                            ContactsContract.Contacts.CONTENT_TYPE
                        )
                    )
                } else {
                    Snackbar.make(
                        binding.root,
                        String.format(
                            R.string.warning_permission_denied.toString(activityMain),
                            R.string.permission_contacts.toString(activityMain)
                        ),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.blue_500,
                R.color.blue_700,
                R.color.red_700,
                R.color.red_500,
                R.color.red_200
            )
            setOnRefreshListener {
                restartActivity()
            }
            isRefreshing = true
        }

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
                            .setIcon(R.drawable.ic_error)
                            .setTitle(R.string.error_bad_exception)
                            .setMessage(R.string.error_sdcard_readonly)
                            .setPositiveButton(R.string.action_ok) { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                        return
                    }
                    status == Environment.MEDIA_SHARED -> {
                        MaterialAlertDialogBuilder(activityMain)
                            .setIcon(R.drawable.ic_error)
                            .setTitle(R.string.error_bad_exception)
                            .setMessage(R.string.error_sdcard_shared)
                            .setPositiveButton(R.string.action_ok) { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                        return
                    }
                    status != Environment.MEDIA_MOUNTED -> {
                        MaterialAlertDialogBuilder(activityMain)
                            .setIcon(R.drawable.ic_error)
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
                    .setIcon(R.drawable.ic_folder)
                    .setTitle(R.string.attention)
                    .setMessage(R.string.storage_permission_request)
                    .setView(linearLayout)
                    .setCancelable(false)
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
            var selection: String
            val selectionArgsList: ArrayList<String> = arrayListOf()
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
            if (filter.isNotEmpty()) {
                filter = "%$filter%"
                selection =
                    "($selection AND ((TITLE LIKE ?) OR (ARTIST LIKE ?) OR (ALBUM LIKE ?)))"
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
                RecyclerAdapter(R.layout.item_sound, sounds.size) { holder, position ->
                    sound = sounds[position]
                    (holder.view(R.id.icon) as ImageView).setImageResource(sound.type.typeIcon)
                    (holder.view(R.id.artist) as TextView).text = sound.artist
                    (holder.view(R.id.album) as TextView).text = sound.album
                    (holder.view(R.id.title) as TextView).text = sound.title
                    (holder.view(R.id.item_layout) as ConstraintLayout).setOnClickListener {
                        val builder = MaterialAlertDialogBuilder(activityMain)
                        builder.setIcon(sound.type.typeIcon)
                        builder.setTitle(sound.title)
                        builder.setPositiveButton(R.string.action_close, null)
                        val dialog = builder.create()
                        val items = arrayListOf<Item>()
                        items.add(
                            Item(
                                R.drawable.ic_open_with,
                                R.string.action_open.toString(activityMain)
                            ) {
                                dialog.dismiss()
                                startActivity(
                                    Intent()
                                        .setAction(Intent.ACTION_VIEW)
                                        .setDataAndType(
                                            Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${sound.id}"),
                                            "audio/*"
                                        )

                                )
                            }
                        )
                        items.add(
                            Item(
                                R.drawable.ic_edit,
                                R.string.action_edit.toString(activityMain)
                            ) {
                                // TODO: On edit button clicked
                            }
                        )
                        items.add(
                            Item(
                                R.drawable.ic_delete,
                                R.string.action_delete.toString(activityMain)
                            ) {
                                dialog.dismiss()
                                MaterialAlertDialogBuilder(activityMain)
                                    .setIcon(R.drawable.ic_delete)
                                    .setTitle(R.string.delete_alert)
                                    .setMessage(R.string.irreversible_action)
                                    .setNegativeButton(R.string.action_no, null)
                                    .setPositiveButton(R.string.action_yes) { _, _ ->
                                        try {
                                            contentResolver.delete(
                                                Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${sound.id}"),
                                                null,
                                                null
                                            )
                                            Snackbar.make(
                                                binding.root,
                                                R.string.delete_success,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                            restartActivity()
                                        } catch (e: Exception) {
                                            Snackbar.make(
                                                binding.root,
                                                R.string.delete_failed,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .show()
                            }
                        )
                        items.add(
                            Item(
                                R.drawable.ic_share,
                                R.string.action_share.toString(activityMain)
                            ) {
                                dialog.dismiss()
                                startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND)
                                            .setType("audio/*")
                                            .putExtra(Intent.EXTRA_STREAM, Uri.parse(sound.path)),
                                        getString(R.string.action_share_chooser)
                                    )
                                )
                            }
                        )
                        when (sound.type) {
                            Types.Ringtone -> {
                                items.add(
                                    Item(
                                        R.drawable.ic_ringtone,
                                        String.format(
                                            R.string.action_set_default.toString(activityMain),
                                            R.string.type_ringtone.toString(activityMain)
                                        )
                                    ) {
                                        dialog.dismiss()
                                        if (isWriteSettingsPermissionGranted) {
                                            try {
                                                RingtoneManager.setActualDefaultRingtoneUri(
                                                    activityMain, RingtoneManager.TYPE_RINGTONE,
                                                    Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${sound.id}")
                                                )
                                                Snackbar.make(
                                                    binding.root, String.format(
                                                        R.string.set_default_success.toString(
                                                            activityMain
                                                        ),
                                                        sound.title,
                                                        R.string.type_ringtone.toString(activityMain)
                                                    ), Snackbar.LENGTH_SHORT
                                                ).show()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Snackbar.make(
                                                    binding.root,
                                                    R.string.error_unknown,
                                                    Snackbar.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            MaterialAlertDialogBuilder(activityMain)
                                                .setIcon(R.drawable.ic_settings)
                                                .setTitle(R.string.attention)
                                                .setMessage(
                                                    String.format(
                                                        R.string.settings_permission_request.toString(
                                                            activityMain
                                                        ),
                                                        R.string.type_ringtone.toString(activityMain)
                                                    )
                                                )
                                                .setPositiveButton(R.string.action_grant) { _, _ ->
                                                    requestWriteSettingsPermission()
                                                }
                                                .setNegativeButton(R.string.action_cancel, null)
                                                .show()
                                        }

                                    }
                                )
                                items.add(
                                    Item(
                                        R.drawable.ic_contacts,
                                        R.string.action_assign_contact.toString(activityMain)
                                    ) {
                                        dialog.dismiss()
                                        if (isPermissionGranted(Manifest.permission.WRITE_CONTACTS)) {
                                            chooseContactResult.launch(
                                                Intent(Intent.ACTION_PICK).setType(
                                                    ContactsContract.Contacts.CONTENT_TYPE
                                                )
                                            )
                                        } else {
                                            MaterialAlertDialogBuilder(activityMain)
                                                .setIcon(R.drawable.ic_contacts)
                                                .setTitle(R.string.attention)
                                                .setMessage(R.string.contacts_permission_request)
                                                .setPositiveButton(R.string.action_grant) { _, _ ->
                                                    ActivityCompat.requestPermissions(
                                                        this@ActivityMain,
                                                        arrayOf(Manifest.permission.WRITE_CONTACTS),
                                                        Actions.RequestContactsPermission
                                                    )
                                                }
                                                .setNegativeButton(R.string.action_cancel, null)
                                                .show()
                                        }
                                    }
                                )
                            }
                            Types.Notification -> {
                                items.add(
                                    Item(
                                        R.drawable.ic_notification,
                                        String.format(
                                            R.string.action_set_default.toString(activityMain),
                                            R.string.type_notification.toString(activityMain)
                                        )
                                    ) {
                                        dialog.dismiss()
                                        if (isWriteSettingsPermissionGranted) {
                                            try {
                                                RingtoneManager.setActualDefaultRingtoneUri(
                                                    activityMain, RingtoneManager.TYPE_NOTIFICATION,
                                                    Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${sound.id}")
                                                )
                                                Snackbar.make(
                                                    binding.root, String.format(
                                                        R.string.set_default_success.toString(
                                                            activityMain
                                                        ),
                                                        sound.title,
                                                        R.string.type_notification.toString(
                                                            activityMain
                                                        )
                                                    ), Snackbar.LENGTH_SHORT
                                                ).show()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Snackbar.make(
                                                    binding.root,
                                                    R.string.error_unknown,
                                                    Snackbar.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            MaterialAlertDialogBuilder(activityMain)
                                                .setIcon(R.drawable.ic_settings)
                                                .setTitle(R.string.attention)
                                                .setMessage(
                                                    String.format(
                                                        R.string.settings_permission_request.toString(
                                                            activityMain
                                                        ),
                                                        R.string.type_notification.toString(
                                                            activityMain
                                                        )
                                                    )
                                                )
                                                .setPositiveButton(R.string.action_grant) { _, _ ->
                                                    requestWriteSettingsPermission()
                                                }
                                                .setNegativeButton(R.string.action_cancel, null)
                                                .show()
                                        }
                                    }
                                )
                            }
                        }
                        items.add(
                            Item(
                                R.drawable.ic_info,
                                R.string.action_info.toString(activityMain)
                            ) {
                                dialog.dismiss()
                                val message = String.format(
                                    getString(R.string.dialog_information),
                                    sound.title, sound.album, sound.artist, sound.path
                                )
                                MaterialAlertDialogBuilder(this@ActivityMain)
                                    .setIcon(R.drawable.ic_info)
                                    .setTitle(R.string.action_info)
                                    .setMessage(
                                        if (Build.VERSION.SDK_INT >= 24) {
                                            Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            Html.fromHtml(message)
                                        }
                                    )
                                    .setNegativeButton(R.string.action_copy_path) { _, _ ->
                                        try {
                                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                                                .setPrimaryClip(
                                                    ClipData.newPlainText(
                                                        sound.title,
                                                        sound.path
                                                    )
                                                )
                                            Snackbar.make(
                                                binding.root,
                                                R.string.copy_success,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            Snackbar.make(
                                                binding.root,
                                                R.string.error_unknown,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }

                                    }
                                    .setPositiveButton(R.string.action_ok, null)
                                    .show()
                            }
                        )

                        val adapter =
                            RecyclerAdapter(R.layout.item, items.size) { holder, position ->
                                val item = items[position]
                                (holder.view(R.id.icon) as ImageView).setImageResource(item.icon)
                                (holder.view(R.id.title) as TextView).text = item.title
                                (holder.view(R.id.item_layout) as ConstraintLayout).setOnClickListener {
                                    item.onClick()
                                }
                            }

                        val linearLayout = LinearLayout(activityMain)
                        val params = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        params.topMargin = 50
                        val recyclerView = RecyclerView(activityMain)
                        recyclerView.layoutManager = LinearLayoutManager(activityMain)
                        recyclerView.adapter = adapter
                        linearLayout.addView(recyclerView, params)
                        dialog.setView(linearLayout)
                        dialog.show()
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
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun restartActivity() {
        binding.swipeRefreshLayout.isRefreshing = true
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity()
        }, 1000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                restartActivity()
                true
            }
            R.id.action_record -> {
                // TODO: Open voice recorder and after record finished open the editor
                true
            }
            R.id.action_about -> {
                // TODO: Show about dialog
                true
            }
            R.id.action_licenses -> {
                // TODO: Show application licenses dialog
                true
            }
            R.id.action_privacy -> {
                // TODO: Show privacy policy information dialog
                true
            }
            R.id.action_settings -> {
                // TODO: Open application settings page
                true
            }
            R.id.action_exit -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}