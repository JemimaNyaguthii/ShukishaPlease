package com.jem.shukishaplease

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jem.shukishaplease.core.model.Destination
import com.jem.shukishaplease.core.ui.theme.ShukishaPleaseTheme
import com.jem.shukishaplease.features.home.DestinationPickerDialog
import com.jem.shukishaplease.features.home.HomeViewModel
import com.jem.shukishaplease.features.home.ShukishaHome
import com.jem.shukishaplease.features.home.UnselectedDestinationDialog
import com.jem.shukishaplease.features.play.ShukishaPlayHubActivity
import com.jem.shukishaplease.features.trip.TripActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShukishaPleaseTheme {
                val viewModel: HomeViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val selectedDestination by viewModel.selectedDestination.collectAsState()
                val context = LocalContext.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.fetchLocation(context)
                    } else {
                        viewModel.setPermissionDenied()
                    }
                }

                var showDestinationPicker by remember { mutableStateOf(false) }
                var showUnselectedPrompt by remember { mutableStateOf(false) }

                ShukishaHome(
                    uiState = uiState,
                    selectedDestination = selectedDestination,
                    sampleDestinations = viewModel.sampleDestinations,
                    onGetLocationClick = {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                viewModel.fetchLocation(context)
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    },
                    onOpenDestinationPicker = { showDestinationPicker = true },
                    onSelectSavedPlace = { dest ->
                        viewModel.selectDestination(dest)
                        Toast.makeText(context, "Alighting destination set to ${dest.name}!", Toast.LENGTH_SHORT).show()
                    },
                    onStartTripClick = {
                        val dest = selectedDestination
                        if (dest != null) {
                            val intent = Intent(context, TripActivity::class.java).apply {
                                putExtra("EXTRA_DEST_NAME", dest.name)
                                putExtra("EXTRA_DEST_LAT", dest.latitude)
                                putExtra("EXTRA_DEST_LON", dest.longitude)
                            }
                            context.startActivity(intent)
                        } else {
                            showUnselectedPrompt = true
                        }
                    },
                    onPlayTriviaClick = {
                        val intent = Intent(context, ShukishaPlayHubActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                if (showUnselectedPrompt) {
                    UnselectedDestinationDialog(
                        onDismiss = { showUnselectedPrompt = false },
                        onPickStage = {
                            showUnselectedPrompt = false
                            showDestinationPicker = true
                        }
                    )
                }

                if (showDestinationPicker) {
                    DestinationPickerDialog(
                        sampleDestinations = viewModel.sampleDestinations,
                        initialQuery = selectedDestination?.name ?: "",
                        onDismiss = { showDestinationPicker = false },
                        onSelectDestination = { dest: Destination ->
                            viewModel.selectDestination(dest)
                            showDestinationPicker = false
                        },
                        onSearchDestination = { query, callback ->
                            viewModel.searchDestinationByName(context, query) { success ->
                                callback(success)
                                if (success) {
                                    showDestinationPicker = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
