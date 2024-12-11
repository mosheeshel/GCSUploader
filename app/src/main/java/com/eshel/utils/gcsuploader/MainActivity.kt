package com.eshel.utils.gcsuploader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private var storage: Storage? = null
    private lateinit var bucketSpinner: Spinner
    private lateinit var btnClearCredentials: Button
    private var fileUri: Uri? = null

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val PREFS_NAME = "GCSUploaderPrefs"
        private const val PREF_LAST_BUCKET = "LastSelectedBucket"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(StorageScopes.DEVSTORAGE_READ_WRITE))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize UI components
        bucketSpinner = findViewById(R.id.bucketSpinner)
        btnClearCredentials = findViewById(R.id.btnClearCredentials)

        // Handle incoming share intent
        handleIncomingIntent(intent)

        // Clear credentials button
        btnClearCredentials.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                bucketSpinner.adapter = null
            }
        }

        // Trigger sign-in if not already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            initializeGoogleCloudStorage(account)
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {
            fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
            if (GoogleSignIn.getLastSignedInAccount(this) == null) {
                performSignInAndUpload()
            } else if (fileUri != null) {
                // If already signed in and file is ready, enable upload
                setupBucketSpinner()
            }
        }
    }

    private fun performSignInAndUpload() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.result
                    initializeGoogleCloudStorage(account)
                } catch (e: Exception) {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeGoogleCloudStorage(account: GoogleSignInAccount?) {
        account?.let {
            val credential = GoogleAccountCredential.usingOAuth2(
                this,
                listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
            ).setSelectedAccount(it.account)

            storage = Storage.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName("GCS Uploader")
                .build()

            // Fetch and setup bucket list
            setupBucketSpinner()
        }
    }

    private fun setupBucketSpinner() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch list of buckets
                val buckets = storage?.buckets()?.list("your-project-id")?.execute()
                val bucketNames = buckets?.items?.map { it.name } ?: listOf()

                withContext(Dispatchers.Main) {
                    // Restore last selected bucket
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val lastSelectedBucket = prefs.getString(PREF_LAST_BUCKET, null)

                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        bucketNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    bucketSpinner.adapter = adapter

                    // Set previously selected bucket if exists
                    lastSelectedBucket?.let { lastBucket ->
                        val position = bucketNames.indexOf(lastBucket)
                        if (position != -1) {
                            bucketSpinner.setSelection(position)
                        }
                    }

                    // Spinner selection listener
                    bucketSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                            val selectedBucket = bucketNames[pos]

                            // Save selected bucket
                            prefs.edit().putString(PREF_LAST_BUCKET, selectedBucket).apply()

                            // If file is ready, enable upload
                            fileUri?.let { uploadFile(it, selectedBucket) }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch buckets: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun uploadFile(fileUri: Uri, bucketName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(fileUri)
                val file = File(fileUri.path ?: return@launch)

                val objectName = file.name

                // Actual upload logic would go here
                // This is a placeholder - you'll need to implement actual GCS upload
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Uploading ${file.name} to $bucketName",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}