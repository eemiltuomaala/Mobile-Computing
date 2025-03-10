package com.example.composetutorial

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.composetutorial.data.User
import com.example.composetutorial.data.UserViewModel
import com.example.composetutorial.data.UserViewModelFactory
import java.io.File
import java.io.FileOutputStream

@Composable
fun SettingsScreen(navController: NavHostController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val mUserViewModel: UserViewModel =
        viewModel(factory = UserViewModelFactory(context.applicationContext as Application))
    //mUserViewModel.insertUser(User(1, "Default"))
    val user by mUserViewModel.userData.collectAsState(initial = null)


    // Observe light level from SharedViewModel
    val lightLevel by sharedViewModel.lightLevel.collectAsState()

    val activity = LocalContext.current as? MainActivity

    var imageFile = File(context.filesDir, "profile.jpg")
    var updateImage by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri ->
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val outputFile = File(context.filesDir, "profile.jpg")
            FileOutputStream(outputFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            imageFile = outputFile
            updateImage = !updateImage
        }
    }
    Column {
        Row(modifier = Modifier.padding(all = 8.dp)) {

            // Button on top of the screen
            IconButton(onClick = {
                navController.navigate("home") {
                    popUpTo("home") {
                        inclusive = true
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }

            // Text on top of the screen
            Text(
                text = "Settings Screen",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )
        }

        Row(modifier = Modifier.padding(all = 8.dp)) {
            Button(onClick = {
                activity?.showNotification()
            }) {
                Text("Send a test notification")
            }
        }
        Row(modifier = Modifier.padding(all = 8.dp)) {
            val painter = if (updateImage) {
                rememberAsyncImagePainter(imageFile)
            } else {
                rememberAsyncImagePainter(imageFile)
            }

            // User image
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            )
        }

        // User name
        Row(modifier = Modifier.padding(all = 8.dp)) {
            OutlinedTextField(
                value = user?.userName ?: "",
                onValueChange = { newName ->
                    mUserViewModel.updateUser(
                        user?.copy(userName = newName) ?: User(userName = newName)
                    )
                },
                label = { Text("Enter your name") },

                )
        }

        // Light Level
        Row(modifier = Modifier.padding(all = 8.dp)) {
            Text(text = "Ambient Light: ${"%.1f".format(lightLevel)} lux")
        }
        Row(modifier = Modifier.padding(all = 8.dp)) {
            if (lightLevel >= 20000) {
                Text(text = "Light level is over 20,000 lux!")
            }
        }
    }
}