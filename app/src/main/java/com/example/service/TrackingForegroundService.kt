package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

data class TrackingStatus(
    val isActive: Boolean = false,
    val destinationId: Long = 0,
    val destinationName: String = "",
    val targetLatitude: Double = 0.0,
    val targetLongitude: Double = 0.0,
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val initialDistanceMeters: Float = 0f,
    val currentDistanceMeters: Float = 0f,
    val alertDistanceMeters: Int = 500,
    val currentSpeedKmh: Float = 0f,
    val isAlerting: Boolean = false,
    val progress: Float = 0f // 0.0 to 1.0 (1.0 = reached)
)

/**
 * Foreground Service สำหรับการติดตามพิกัด GPS ระหว่างอ่านหนังสือ
 * ปฏิบัติตามหลักความเป็นส่วนตัว: ไม่ส่งเสียงใดๆ (No Sound) แต่ใช้การสั่นต่อเนื่อง (Vibration)
 */
class TrackingForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID_TRACKING = "channel_along_books_silent_tracking"
        const val CHANNEL_ID_ALERT = "channel_along_books_silent_alert"
        const val NOTIFICATION_ID_TRACKING = 1001
        const val NOTIFICATION_ID_ALERT = 1002

        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"

        const val EXTRA_DEST_ID = "EXTRA_DEST_ID"
        const val EXTRA_DEST_NAME = "EXTRA_DEST_NAME"
        const val EXTRA_DEST_LAT = "EXTRA_DEST_LAT"
        const val EXTRA_DEST_LNG = "EXTRA_DEST_LNG"
        const val EXTRA_ALERT_DISTANCE = "EXTRA_ALERT_DISTANCE"
        const val EXTRA_INITIAL_DISTANCE = "EXTRA_INITIAL_DISTANCE"

        private val _trackingStatus = MutableStateFlow(TrackingStatus())
        val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

        fun startTracking(
            context: Context,
            destinationId: Long,
            destinationName: String,
            targetLat: Double,
            targetLng: Double,
            alertDistance: Int,
            initialDistance: Float
        ) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DEST_ID, destinationId)
                putExtra(EXTRA_DEST_NAME, destinationName)
                putExtra(EXTRA_DEST_LAT, targetLat)
                putExtra(EXTRA_DEST_LNG, targetLng)
                putExtra(EXTRA_ALERT_DISTANCE, alertDistance)
                putExtra(EXTRA_INITIAL_DISTANCE, initialDistance)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTracking(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initVibrator()
        createNotificationChannels()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val destId = intent.getLongExtra(EXTRA_DEST_ID, 0L)
                val destName = intent.getStringExtra(EXTRA_DEST_NAME) ?: "สถานีเป้าหมาย"
                val destLat = intent.getDoubleExtra(EXTRA_DEST_LAT, 0.0)
                val destLng = intent.getDoubleExtra(EXTRA_DEST_LNG, 0.0)
                val alertDist = intent.getIntExtra(EXTRA_ALERT_DISTANCE, 500)
                val initialDist = intent.getFloatExtra(EXTRA_INITIAL_DISTANCE, 0f)

                _trackingStatus.value = TrackingStatus(
                    isActive = true,
                    destinationId = destId,
                    destinationName = destName,
                    targetLatitude = destLat,
                    targetLongitude = destLng,
                    alertDistanceMeters = alertDist,
                    initialDistanceMeters = max(initialDist, alertDist.toFloat()),
                    currentDistanceMeters = initialDist,
                    isAlerting = false,
                    progress = 0f
                )

                startForegroundWithNotification(destName, initialDist)
                startLocationUpdates()
            }
            ACTION_STOP -> {
                stopTrackingAndSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification(stationName: String, distanceMeters: Float) {
        val notification = buildTrackingNotification(stationName, distanceMeters)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID_TRACKING,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID_TRACKING, notification)
        }
    }

    private fun buildTrackingNotification(stationName: String, distanceMeters: Float): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, StopAlertReceiver::class.java).apply {
            action = StopAlertReceiver.ACTION_STOP_SERVICE
        }
        val pendingStop = PendingIntent.getBroadcast(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceText = if (distanceMeters >= 1000) {
            String.format("%.1f กม.", distanceMeters / 1000f)
        } else {
            "${distanceMeters.toInt()} ม."
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_TRACKING)
            .setContentTitle("Along The Books: กำลังเฝ้าระวัง")
            .setContentText("เป้าหมาย: $stationName • อีก $distanceText")
            .setSmallIcon(R.drawable.ic_launcher_fg)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setSound(null) // ไม่มีเสียงเด็ดขาด
            .setSilent(true)
            .addAction(0, "หยุดติดตาม", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                onNewLocation(location)
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .setMinUpdateDistanceMeters(2.0f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permission might have been revoked
            stopTrackingAndSelf()
        }
    }

    private fun onNewLocation(location: Location) {
        val currentStatus = _trackingStatus.value
        if (!currentStatus.isActive) return

        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            currentStatus.targetLatitude,
            currentStatus.targetLongitude,
            results
        )
        val distance = results[0]
        val speedKmh = (location.speed * 3.6f).coerceAtLeast(0f)

        // คำนวณความคืบหน้า 0.0 - 1.0 (1.0 = ถึงเป้าหมาย)
        val initialDist = max(currentStatus.initialDistanceMeters, currentStatus.alertDistanceMeters.toFloat())
        val progress = if (initialDist > 0) {
            (1f - (distance / initialDist)).coerceIn(0f, 1f)
        } else 1f

        val shouldAlert = distance <= currentStatus.alertDistanceMeters

        _trackingStatus.value = currentStatus.copy(
            currentLatitude = location.latitude,
            currentLongitude = location.longitude,
            currentDistanceMeters = distance,
            currentSpeedKmh = speedKmh,
            progress = progress,
            isAlerting = shouldAlert
        )

        // อัปเดต Notification ติดตามเบื้องหลัง
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            NOTIFICATION_ID_TRACKING,
            buildTrackingNotification(currentStatus.destinationName, distance)
        )

        // ถ้าถึงระยะเตือนล่วงหน้า และยังไม่ได้เริ่มสั่น
        if (shouldAlert && !currentStatus.isAlerting) {
            triggerArrivalAlert(currentStatus.destinationName, distance)
        }
    }

    /**
     * แจ้งเตือนเมื่อถึงระยะที่ตั้งไว้:
     * - สั่นต่อเนื่องเป็นจังหวะ (Continuous Vibration Pattern)
     * - ไม่ส่งเสียงใดๆ ทั้งสิ้น (Silent Notification)
     * - แสดงปุ่ม "ปิดการแจ้งเตือน / ถึงแล้ว"
     */
    private fun triggerArrivalAlert(stationName: String, distanceMeters: Float) {
        startContinuousVibration()
        showArrivalAlertNotification(stationName, distanceMeters)
    }

    private fun startContinuousVibration() {
        val pattern = longArrayOf(0, 600, 300, 600, 300, 800, 400)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // repeat = 0 หมายถึงสั่นวนลูปไปเรื่อยๆ จนกว่าจะมีคำสั่งยกเลิก (vibrator.cancel())
                val effect = VibrationEffect.createWaveform(pattern, 0)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showArrivalAlertNotification(stationName: String, distanceMeters: Float) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ALERT_SHEET", true)
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, StopAlertReceiver::class.java).apply {
            action = StopAlertReceiver.ACTION_STOP_SERVICE
        }
        val pendingStop = PendingIntent.getBroadcast(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceText = if (distanceMeters >= 1000) {
            String.format("%.1f กม.", distanceMeters / 1000f)
        } else {
            "${distanceMeters.toInt()} ม."
        }

        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
            .setContentTitle("ใกล้ถึงเป้าหมายแล้ว!")
            .setContentText("สถานี: $stationName (อีกประมาณ $distanceText)")
            .setSmallIcon(R.drawable.ic_launcher_fg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSound(null) // ไม่ส่งเสียงเด็ดขาด
            .setContentIntent(pendingOpenApp)
            .addAction(0, "ปิดการแจ้งเตือน / ถึงแล้ว", pendingStop)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_ALERT, alertNotification)
    }

    private fun stopTrackingAndSelf() {
        stopVibration()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // ignore
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_TRACKING)
        notificationManager.cancel(NOTIFICATION_ID_ALERT)

        _trackingStatus.value = TrackingStatus(isActive = false, isAlerting = false)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // ช่องที่ 1: สถานะติดตามเบื้องหลัง (เงียบ ไม่สั่น)
            val trackingChannel = NotificationChannel(
                CHANNEL_ID_TRACKING,
                "สถานะการติดตาม (โหมดเงียบ)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "แสดงสถานะการเฝ้าระวังตำแหน่งระหว่างอ่านหนังสือ"
                setSound(null, null)
                enableVibration(false)
            }

            // ช่องที่ 2: แจ้งเตือนเมื่อถึงเป้าหมาย (ระดับสูงสุด แต่ไม่มีเสียง มีการสั่น)
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERT,
                "แจ้งเตือนใกล้ถึงสถานี (สั่นเงียบ)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "สั่นเตือนเมื่อเข้าใกล้สถานีเป้าหมาย โดยไม่รบกวนผู้อื่นในที่สาธารณะ"
                setSound(null, null) // เงียบสนิท ไม่มีเสียง
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 300, 600, 300)
            }

            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    override fun onDestroy() {
        stopTrackingAndSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
