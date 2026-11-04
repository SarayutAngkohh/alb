package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Destination
import com.example.data.repository.DestinationRepository
import com.example.service.TrackingForegroundService
import com.example.service.TrackingStatus
import com.example.util.PreferencesManager
import com.example.util.ReaderAppInfo
import com.example.util.ReadingAppsHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Main : ScreenDestination()
    data class Monitoring(val destination: Destination) : ScreenDestination()
}

class MainViewModel(
    private val repository: DestinationRepository,
    private val preferencesManager: PreferencesManager,
    context: Context
) : ViewModel() {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    // ข้อมูลรายการเป้าหมายจาก Room
    val destinations: StateFlow<List<Destination>> = repository.allDestinations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // สถานะการติดตามแบบเรียลไทม์จาก Foreground Service
    val trackingStatus: StateFlow<TrackingStatus> = TrackingForegroundService.trackingStatus

    // สถานะหน้าจอปัจจุบัน (Main หรือ Monitoring)
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Main)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // แสดงหน้าจอ Disclaimer ครั้งแรก
    private val _showDisclaimer = MutableStateFlow(!preferencesManager.isDisclaimerAccepted)
    val showDisclaimer: StateFlow<Boolean> = _showDisclaimer.asStateFlow()

    // แสดง Dialog เพิ่มเป้าหมายใหม่
    private val _showAddDestinationDialog = MutableStateFlow(false)
    val showAddDestinationDialog: StateFlow<Boolean> = _showAddDestinationDialog.asStateFlow()

    // แสดง Dialog ช่วยเปิดแอปอ่านหนังสือ
    private val _showReaderLauncherDialog = MutableStateFlow(false)
    val showReaderLauncherDialog: StateFlow<Boolean> = _showReaderLauncherDialog.asStateFlow()

    // แสดง Dialog วิธีการใช้งานและเครดิต
    private val _showUserGuideDialog = MutableStateFlow(false)
    val showUserGuideDialog: StateFlow<Boolean> = _showUserGuideDialog.asStateFlow()

    // รายการแอปสำหรับอ่านหนังสือในเครื่อง
    private val _readerApps = MutableStateFlow<List<ReaderAppInfo>>(emptyList())
    val readerApps: StateFlow<List<ReaderAppInfo>> = _readerApps.asStateFlow()

    // ข้อความเริ่มต้นเมื่อกด "เริ่มอ่านหนังสือ"
    private val _initialReadingMessage = MutableStateFlow<String?>(null)
    val initialReadingMessage: StateFlow<String?> = _initialReadingMessage.asStateFlow()

    // สถานะกำลังดึงพิกัดปัจจุบัน
    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> = _isFetchingLocation.asStateFlow()

    // Event แจ้งเตือน Toast
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun acceptDisclaimer() {
        preferencesManager.isDisclaimerAccepted = true
        _showDisclaimer.value = false
    }

    fun openAddDestinationDialog() {
        _showAddDestinationDialog.value = true
    }

    fun closeAddDestinationDialog() {
        _showAddDestinationDialog.value = false
    }

    fun closeReaderLauncherDialog() {
        _showReaderLauncherDialog.value = false
    }

    fun openUserGuideDialog() {
        _showUserGuideDialog.value = true
    }

    fun closeUserGuideDialog() {
        _showUserGuideDialog.value = false
    }

    fun clearInitialReadingMessage() {
        _initialReadingMessage.value = null
    }

    /**
     * ดึงพิกัดปัจจุบันด้วย FusedLocationProviderClient เพื่อความแม่นยำสูง
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        _isFetchingLocation.value = true
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    _isFetchingLocation.value = false
                    if (location != null) {
                        onSuccess(location.latitude, location.longitude)
                    } else {
                        // ลองดึง lastLocation สำรอง
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onSuccess(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                onError("ไม่สามารถระบุพิกัดได้ กรุณาเปิด GPS")
                            }
                        }.addOnFailureListener {
                            onError("ไม่สามารถระบุพิกัดได้: ${it.message}")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    _isFetchingLocation.value = false
                    onError("เกิดข้อผิดพลาดในการดึงพิกัด: ${e.message}")
                }
        } catch (e: SecurityException) {
            _isFetchingLocation.value = false
            onError("ยังไม่ได้รับอนุญาตให้เข้าถึงสิทธิ์ตำแหน่ง GPS")
        }
    }

    /**
     * บันทึกข้อมูลเป้าหมายลง Room Database
     */
    fun saveDestination(
        name: String,
        latitude: Double,
        longitude: Double,
        alertDistanceMeters: Int,
        category: String
    ) {
        viewModelScope.launch {
            val destination = Destination(
                name = name.trim(),
                latitude = latitude,
                longitude = longitude,
                alertDistanceMeters = alertDistanceMeters,
                category = category
            )
            repository.insert(destination)
            _showAddDestinationDialog.value = false
            _toastEvent.emit("บันทึกสำเร็จแล้ว")
        }
    }

    fun deleteDestination(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            _toastEvent.emit("ลบรายการแล้ว")
        }
    }

    /**
     * เริ่มโหมดติดตามและเริ่มอ่านหนังสือ (Start Reading Mode)
     * - คำนวณระยะทางแบบเส้นตรง (Location.distanceBetween)
     * - เริ่มรัน Foreground Service สั่นเตือนเมื่อถึงเป้าหมาย
     * - แจ้งเตือนระยะทางเริ่มต้น เช่น "เป้าหมาย: [ชื่อสถานี] ระยะห่างอีก [X] กิโลเมตร / เมตร"
     * - สแกนและเปิด Dialog แนะนำแอปอ่านหนังสือในเครื่อง
     */
    @SuppressLint("MissingPermission")
    fun startReadingMode(
        context: Context,
        destination: Destination,
        alertDistance: Int
    ) {
        viewModelScope.launch {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    val initialDist = if (loc != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            loc.latitude,
                            loc.longitude,
                            destination.latitude,
                            destination.longitude,
                            results
                        )
                        results[0]
                    } else {
                        // หากยังไม่มีพิกัดล่าสุด ให้ใช้ระยะเผื่อเป็น default
                        (alertDistance * 2).toFloat()
                    }

                    // เริ่ม Foreground Service
                    TrackingForegroundService.startTracking(
                        context = context,
                        destinationId = destination.id,
                        destinationName = destination.name,
                        targetLat = destination.latitude,
                        targetLng = destination.longitude,
                        alertDistance = alertDistance,
                        initialDistance = initialDist
                    )

                    // สร้างข้อความแจ้งระยะเริ่มต้น
                    val distMsg = if (initialDist <= alertDistance) {
                        "เป้าหมาย: ${destination.name} ถึงแล้ว!"
                    } else {
                        val formattedDist = if (initialDist >= 1000) {
                            String.format("%.1f กิโลเมตร", initialDist / 1000f)
                        } else {
                            "${initialDist.toInt()} เมตร"
                        }
                        "เป้าหมาย: ${destination.name} ระยะห่างอีก $formattedDist"
                    }
                    _initialReadingMessage.value = distMsg

                    // โหลดรายการแอปอ่านหนังสือ
                    val apps = ReadingAppsHelper.getInstalledReaderApps(context)
                    _readerApps.value = apps
                    _showReaderLauncherDialog.value = true
                }
            } catch (e: Exception) {
                // Fallback start
                TrackingForegroundService.startTracking(
                    context = context,
                    destinationId = destination.id,
                    destinationName = destination.name,
                    targetLat = destination.latitude,
                    targetLng = destination.longitude,
                    alertDistance = alertDistance,
                    initialDistance = (alertDistance * 2).toFloat()
                )
            }
        }
    }

    /**
     * หยุดการติดตามและปิดการสั่นทันที
     */
    fun stopTracking(context: Context) {
        TrackingForegroundService.stopTracking(context)
        viewModelScope.launch {
            _toastEvent.emit("หยุดการติดตามเรียบร้อย")
        }
    }

    fun launchReaderApp(context: Context, packageName: String) {
        val success = ReadingAppsHelper.launchApp(context, packageName)
        if (!success) {
            viewModelScope.launch {
                _toastEvent.emit("ไม่สามารถเปิดแอปพลิเคชันได้")
            }
        }
        _showReaderLauncherDialog.value = false
    }
}

class MainViewModelFactory(
    private val repository: DestinationRepository,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, preferencesManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
