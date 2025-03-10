package com.example.composetutorial
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.composetutorial.ui.theme.ComposeTutorialTheme
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composetutorial.data.AppDatabase
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel

const val CHANNEL_ID = "channel"
class MainActivity : ComponentActivity() {
    private var hasNotified by mutableStateOf(false)
    private lateinit var sensorListener: SensorListener
    private val sharedViewModel: SharedViewModel by viewModels()


    // List of required permissions
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.CAMERA
    )

    // Permission launcher for requesting multiple permissions
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            // All permissions granted, proceed with app logic
        } else {
            // Handle denied permissions
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ||
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            ) {
                showPermissionRationaleDialog()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {

        }

        // Initialize the sensor listener
        sensorListener = SensorListener(this) { lux ->
            sharedViewModel.updateLightLevel(lux) // Update light level in SharedViewModel
            checkLightLevelAndNotify(lux)
        }

        // Start monitoring light level
        sensorListener.register()

        //  Check for notification permission on cold launch
        checkRequiredPermissions()

        // Create instance of database
        val database = AppDatabase.getInstance(applicationContext)
        var userDao = database.userDao()
        setContent {
            ComposeTutorialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            Conversation(navController = navController, SampleData.conversationSample)
                        }
                        composable("settings") {
                            SettingsScreen(navController = navController, sharedViewModel = sharedViewModel)
                        }
                    }
                }
            }
        }
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the sensor listener when the activity is destroyed
        sensorListener.unregister()
    }

    private fun checkLightLevelAndNotify(lux: Float) {
        if (lux >= 20000 && !hasNotified) {
            showNotification()
            hasNotified = true
        } else if (lux < 20000) {
            hasNotified = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkRequiredPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ||
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            ) {
                showPermissionRationaleDialog()
            } else {
                requestPermissionsLauncher.launch(permissionsToRequest)
            }
        } else {
            // All permissions already granted, proceed with app logic
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Needed")
            .setMessage("This app needs notification and camera permissions to function properly. Please grant the permissions to continue.")
            .setPositiveButton("OK") { _, _ ->
                // Request the permissions again
                requestPermissionsLauncher.launch(requiredPermissions)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Denied")
            .setMessage("You have denied the required permissions multiple times. Please enable them in the app settings to use all features.")
            .setPositiveButton("Go to Settings") { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "1st channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Notification"
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    @SuppressLint("MissingPermission")
    fun showNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Light level is over 20,000 lux!")
            .setContentText("Tap here to open messages")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }
}

