package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Destination
import com.example.service.TrackingStatus
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ReadingAmber
import com.example.ui.theme.ReadingAmberDark
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    destination: Destination,
    trackingStatus: TrackingStatus,
    onBack: () -> Unit,
    onStartReading: (alertDistance: Int) -> Unit,
    onStopTracking: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var selectedAlertDistance by remember(destination.id) {
        mutableIntStateOf(if (destination.alertDistanceMeters > 0) destination.alertDistanceMeters else 500)
    }

    var customDistanceInput by remember(destination.id) {
        mutableStateOf(selectedAlertDistance.toString())
    }

    // สถานะเปิด-ปิด ลิ้นชักตั้งค่าระยะเตือน (Drawer Expanded State)
    var isDrawerExpanded by remember { mutableStateOf(true) }

    val isTrackingThisDest = trackingStatus.isActive && trackingStatus.destinationId == destination.id

    // Animation ความคืบหน้าของหน้าปัดวงกลม (0.0 to 1.0)
    val animatedProgress by animateFloatAsState(
        targetValue = if (isTrackingThisDest) trackingStatus.progress else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "dial_progress"
    )

    // Pulsing animation สำหรับ Alert state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = destination.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Along The Books DEMO • หน้าปัดติดตามตำแหน่ง",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tracking_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "กลับ"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ป้ายเตือนว่ากำลังสั่นเตือนเมื่อถึงเป้าหมาย
            AnimatedVisibility(visible = isTrackingThisDest && trackingStatus.isAlerting) {
                ArrivalAlertBanner(
                    stationName = destination.name,
                    distanceMeters = trackingStatus.currentDistanceMeters,
                    pulseScale = pulseScale,
                    onDismissAlert = onStopTracking
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. หน้าปัดวงกลม (Circular Progress Dial) เริ่มต้นที่ 12 นาฬิกา และหมุนตามเข็มนาฬิกา
            CircularProgressDial(
                progress = animatedProgress,
                distanceMeters = if (isTrackingThisDest) trackingStatus.currentDistanceMeters else 0f,
                speedKmh = if (isTrackingThisDest) trackingStatus.currentSpeedKmh else 0f,
                isTracking = isTrackingThisDest,
                isAlerting = trackingStatus.isAlerting,
                modifier = Modifier
                    .size(270.dp)
                    .testTag("circular_progress_dial")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. ส่วนตั้งค่า "ระยะเตือนล่วงหน้า" แบบลิ้นชัก (Alert Distance Drawer with 3 Quick Presets + Custom TextField)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alert_distance_drawer_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header ลิ้นชัก กดเปิด-ปิดได้
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isDrawerExpanded = !isDrawerExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ReadingAmber.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = ReadingAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ระยะสั่นเตือนล่วงหน้า",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "กดเพื่อเปิด-ปิดลิ้นชักตั้งค่า",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ReadingAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$selectedAlertDistance ม.",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ReadingAmberDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isDrawerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "เปิด/ปิดลิ้นชัก",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ตัวเนื้อหาลิ้นชักตั้งค่าระยะเตือน
                    AnimatedVisibility(visible = isDrawerExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "เลือกการแจ้งเตือนด่วน:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3 ปุ่ม Quick Preset: ซ้าย 300m | กลาง 500m | ขวา 1km (1000m)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // ปุ่มซ้าย: 300m
                                val is300Selected = selectedAlertDistance == 300
                                Button(
                                    onClick = {
                                        selectedAlertDistance = 300
                                        customDistanceInput = "300"
                                        if (isTrackingThisDest) {
                                            onStartReading(300)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("preset_300m_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (is300Selected) ReadingAmber else MaterialTheme.colorScheme.surface,
                                        contentColor = if (is300Selected) TransitNavy else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = "300m",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                // ปุ่มกลาง: 500m (Default)
                                val is500Selected = selectedAlertDistance == 500
                                Button(
                                    onClick = {
                                        selectedAlertDistance = 500
                                        customDistanceInput = "500"
                                        if (isTrackingThisDest) {
                                            onStartReading(500)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("preset_500m_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (is500Selected) ReadingAmber else MaterialTheme.colorScheme.surface,
                                        contentColor = if (is500Selected) TransitNavy else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = "500m",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                // ปุ่มขวา: 1km (1000m)
                                val is1000Selected = selectedAlertDistance == 1000
                                Button(
                                    onClick = {
                                        selectedAlertDistance = 1000
                                        customDistanceInput = "1000"
                                        if (isTrackingThisDest) {
                                            onStartReading(1000)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("preset_1km_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (is1000Selected) ReadingAmber else MaterialTheme.colorScheme.surface,
                                        contentColor = if (is1000Selected) TransitNavy else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = "1km",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ด้านล่างกลาง: ช่อง TextField ให้กรอกตัวเลขหน่วยเป็นเมตรเองได้ (Default 500m)
                            Text(
                                text = "หรือกำหนดระยะสั่นเตือนเอง (เมตร):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = customDistanceInput,
                                onValueChange = { input ->
                                    customDistanceInput = input
                                    val parsed = input.toIntOrNull()
                                    if (parsed != null && parsed > 0) {
                                        selectedAlertDistance = parsed
                                        if (isTrackingThisDest) {
                                            onStartReading(parsed)
                                        }
                                    } else if (input.isEmpty()) {
                                        // Default fallback 500m
                                        selectedAlertDistance = 500
                                    }
                                },
                                label = { Text("ระยะเตือนสั่น (เมตร)") },
                                placeholder = { Text("500") },
                                suffix = { Text("เมตร", style = MaterialTheme.typography.labelLarge) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .testTag("custom_alert_distance_input"),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ReadingAmber,
                                    focusedLabelColor = ReadingAmber
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "ค่าเริ่มต้นตั้งไว้ที่ 500 เมตร (ระบบจะสั่นเตือนเมื่อถึงระยะที่ตั้งไว้)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. ปุ่ม "เริ่มอ่านหนังสือ" (Start Reading Mode)
            Button(
                onClick = { onStartReading(selectedAlertDistance) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TransitBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_reading_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isTrackingThisDest) "เปิดแอปอ่านหนังสือต่อ" else "เริ่มอ่านหนังสือ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // ปุ่มหยุดติดตาม
            if (isTrackingThisDest) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onStopTracking,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AlertRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("stop_tracking_action_button")
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (trackingStatus.isAlerting) "ปิดการแจ้งเตือน / ถึงแล้ว" else "หยุดการเฝ้าระวังตำแหน่ง",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Station Information Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = TransitBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "พิกัดสถานี: ${destination.latitude}, ${destination.longitude}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ประเภท: ${destination.category} • ข้อมูลทดสอบในเครื่อง",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * แบนเนอร์แจ้งเตือนเมื่อถึงเป้าหมายแล้ว พร้อมปุ่ม "ปิดการแจ้งเตือน / ถึงแล้ว"
 */
@Composable
fun ArrivalAlertBanner(
    stationName: String,
    distanceMeters: Float,
    pulseScale: Float,
    onDismissAlert: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AlertRed),
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .padding(bottom = 12.dp)
            .testTag("arrival_alert_banner")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ใกล้ถึงสถานีเป้าหมายแล้ว!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$stationName (ระยะเหลือ ${distanceMeters.toInt()} เมตร)",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDismissAlert,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AlertRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_arrival_alert_button")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ปิดการแจ้งเตือน / ถึงแล้ว",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Custom Canvas Circular Progress Dial:
 * - เริ่มต้นที่ 12 นาฬิกา (-90 องศา)
 * - หมุนตามเข็มนาฬิกาเรื่อยๆ ตามความคืบหน้าที่เข้าใกล้เป้าหมาย
 * - ตรงกลางแสดงระยะทางคงเหลือ (ม./กม.) และเปอร์เซ็นต์
 */
@Composable
fun CircularProgressDial(
    progress: Float,
    distanceMeters: Float,
    speedKmh: Float,
    isTracking: Boolean,
    isAlerting: Boolean,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = if (isAlerting) AlertRed else ReadingAmber
    val progressSecondary = if (isAlerting) Color(0xFFF43F5E) else TransitBlue

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            // วาดวงกลมพื้นหลัง (Background Track)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // วาดเส้นความคืบหน้า (Progress Arc) เริ่มต้นที่ 12 นาฬิกา (-90 องศา) หมุนตามเข็ม
            val sweep = (progress * 360f).coerceIn(0f, 360f)
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(progressSecondary, progressColor, progressColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // วาดจุดมาร์กเกอร์ปลายเข็มที่ตำแหน่งปัจจุบัน
                val angleRad = Math.toRadians((-90f + sweep).toDouble())
                val radius = diameter / 2
                val centerX = size.width / 2
                val centerY = size.height / 2
                val pinX = (centerX + radius * cos(angleRad)).toFloat()
                val pinY = (centerY + radius * sin(angleRad)).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.45f,
                    center = Offset(pinX, pinY)
                )
                drawCircle(
                    color = progressColor,
                    radius = strokeWidth * 0.3f,
                    center = Offset(pinX, pinY)
                )
            }
        }

        // ข้อความและรายละเอียดตรงกลางหน้าปัด
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isAlerting) Icons.Default.Vibration else Icons.Default.DirectionsSubway,
                contentDescription = null,
                tint = if (isAlerting) AlertRed else TransitBlue,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isTracking) {
                val distanceText = if (distanceMeters >= 1000) {
                    String.format("%.1f", distanceMeters / 1000f)
                } else {
                    "${distanceMeters.toInt()}"
                }
                val unitText = if (distanceMeters >= 1000) "กม." else "เมตร"

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unitText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Text(
                    text = "ความคืบหน้า ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ReadingAmberDark
                )

                if (speedKmh > 1f) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${speedKmh.toInt()} กม./ชม.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "พร้อมติดตาม",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "กดเริ่มอ่านหนังสือเพื่อวัดระยะ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
