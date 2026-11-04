package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.AddDestinationDialog
import com.example.ui.dialogs.ReaderAppLauncherDialog
import com.example.ui.dialogs.UserGuideDialog
import com.example.ui.screens.DisclaimerScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.AlongTheBooksTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as AlongTheBooksApp
        MainViewModelFactory(
            repository = app.repository,
            preferencesManager = app.preferencesManager,
            context = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlongTheBooksTheme {
                val context = LocalContext.current
                val showDisclaimer by viewModel.showDisclaimer.collectAsStateWithLifecycle()
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val destinations by viewModel.destinations.collectAsStateWithLifecycle()
                val trackingStatus by viewModel.trackingStatus.collectAsStateWithLifecycle()
                val showAddDialog by viewModel.showAddDestinationDialog.collectAsStateWithLifecycle()
                val showReaderDialog by viewModel.showReaderLauncherDialog.collectAsStateWithLifecycle()
                val showUserGuideDialog by viewModel.showUserGuideDialog.collectAsStateWithLifecycle()
                val readerApps by viewModel.readerApps.collectAsStateWithLifecycle()
                val initialMsg by viewModel.initialReadingMessage.collectAsStateWithLifecycle()
                val isFetchingLocation by viewModel.isFetchingLocation.collectAsStateWithLifecycle()

                // รับ Toast Event จาก ViewModel
                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showDisclaimer) {
                        // หน้าจอ Disclaimer / Privacy Policy สำหรับการเปิดแอปครั้งแรก
                        DisclaimerScreen(
                            onAccept = {
                                viewModel.acceptDisclaimer()
                            }
                        )
                    } else {
                        // หน้าจอหลักหรือหน้าปัดติดตาม
                        when (val screen = currentScreen) {
                            is ScreenDestination.Main -> {
                                MainScreen(
                                    destinations = destinations,
                                    trackingStatus = trackingStatus,
                                    onSelectDestination = { dest ->
                                        viewModel.navigateTo(ScreenDestination.Monitoring(dest))
                                    },
                                    onAddClick = {
                                        viewModel.openAddDestinationDialog()
                                    },
                                    onDeleteDestination = { id ->
                                        viewModel.deleteDestination(id)
                                    },
                                    onStopTracking = {
                                        viewModel.stopTracking(context)
                                    },
                                    onOpenTrackingDial = {
                                        val activeDest = destinations.find { it.id == trackingStatus.destinationId }
                                        if (activeDest != null) {
                                            viewModel.navigateTo(ScreenDestination.Monitoring(activeDest))
                                        }
                                    },
                                    onShowUserGuide = {
                                        viewModel.openUserGuideDialog()
                                    }
                                )
                            }
                            is ScreenDestination.Monitoring -> {
                                TrackingScreen(
                                    destination = screen.destination,
                                    trackingStatus = trackingStatus,
                                    onBack = {
                                        viewModel.navigateTo(ScreenDestination.Main)
                                    },
                                    onStartReading = { alertDist ->
                                        viewModel.startReadingMode(context, screen.destination, alertDist)
                                    },
                                    onStopTracking = {
                                        viewModel.stopTracking(context)
                                    }
                                )
                            }
                        }

                        // Dialog สร้างเป้าหมายใหม่ (Manual Coordinate Saver)
                        if (showAddDialog) {
                            AddDestinationDialog(
                                onDismiss = { viewModel.closeAddDestinationDialog() },
                                onSave = { name, lat, lng, alertDist, cat ->
                                    viewModel.saveDestination(name, lat, lng, alertDist, cat)
                                },
                                onFetchCurrentLocation = { onSuccess, onError ->
                                    viewModel.fetchCurrentLocation(onSuccess, onError)
                                },
                                isFetchingLocation = isFetchingLocation
                            )
                        }

                        // Dialog ช่วยเปิดแอปอ่านหนังสือ (App Launcher Dialog)
                        if (showReaderDialog) {
                            ReaderAppLauncherDialog(
                                initialMessage = initialMsg,
                                readerApps = readerApps,
                                onLaunchApp = { pkg ->
                                    viewModel.launchReaderApp(context, pkg)
                                },
                                onDismiss = {
                                    viewModel.closeReaderLauncherDialog()
                                }
                            )
                        }

                        // Dialog วิธีใช้งานแอปพลิเคชันและเครดิต
                        if (showUserGuideDialog) {
                            UserGuideDialog(
                                onDismiss = {
                                    viewModel.closeUserGuideDialog()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
