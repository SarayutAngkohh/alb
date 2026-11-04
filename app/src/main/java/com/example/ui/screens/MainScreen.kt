package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Destination
import com.example.service.TrackingStatus
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ReadingAmber
import com.example.ui.theme.ReadingAmberDark
import com.example.ui.theme.StationEmerald
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    destinations: List<Destination>,
    trackingStatus: TrackingStatus,
    onSelectDestination: (Destination) -> Unit,
    onAddClick: () -> Unit,
    onDeleteDestination: (Long) -> Unit,
    onStopTracking: () -> Unit,
    onOpenTrackingDial: () -> Unit,
    onShowUserGuide: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = ReadingAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Along The Books DEMO",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "By Sarayut Angkoh",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onShowUserGuide,
                        modifier = Modifier.testTag("info_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "วิธีใช้งานแอปพลิเคชันและเครดิต",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = ReadingAmber,
                contentColor = TransitNavy,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .testTag("add_destination_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "สร้างเป้าหมายใหม่",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // การ์ดสถานะเบื้องหลัง (Active Tracking Bar)
            if (trackingStatus.isActive) {
                item {
                    ActiveTrackingBanner(
                        trackingStatus = trackingStatus,
                        onOpenTrackingDial = onOpenTrackingDial,
                        onStopTracking = onStopTracking
                    )
                }
            }

            // คำแนะนำโหมดนาฬิกาปลุกสถานี (Alarm Clock Destination List)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = ReadingAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "รายการเป้าหมาย",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "${destinations.size} สถานี",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (destinations.isEmpty()) {
                item {
                    EmptyDestinationsCard(onAddClick = onAddClick)
                }
            } else {
                items(
                    items = destinations,
                    key = { it.id }
                ) { destination ->
                    DestinationAlarmCard(
                        destination = destination,
                        isCurrentlyTracking = trackingStatus.isActive && trackingStatus.destinationId == destination.id,
                        onClick = { onSelectDestination(destination) },
                        onDelete = { onDeleteDestination(destination.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

/**
 * แถบแสดงสถานะการติดตามที่กำลังทำงานในพื้นหลัง (Active Tracking Banner)
 */
@Composable
fun ActiveTrackingBanner(
    trackingStatus: TrackingStatus,
    onOpenTrackingDial: () -> Unit,
    onStopTracking: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (trackingStatus.isAlerting) AlertRed else TransitBlue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_tracking_banner")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (trackingStatus.isAlerting) Icons.Default.Vibration else Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (trackingStatus.isAlerting) "สั่นเตือน: ถึงเป้าหมายแล้ว!" else "กำลังเฝ้าระวังตำแหน่งเบื้องหลัง",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "เป้าหมาย: ${trackingStatus.destinationName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                val distanceFormatted = if (trackingStatus.currentDistanceMeters >= 1000) {
                    String.format("%.1f กม.", trackingStatus.currentDistanceMeters / 1000f)
                } else {
                    "${trackingStatus.currentDistanceMeters.toInt()} ม."
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = distanceFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenTrackingDial,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = TransitNavy
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_dial_button")
                ) {
                    Text(
                        text = "ดูหน้าปัดติดตาม",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onStopTracking,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("stop_tracking_button")
                ) {
                    Icon(imageVector = Icons.Default.StopCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (trackingStatus.isAlerting) "ปิดสั่นเตือน" else "หยุด",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * การ์ดเป้าหมายดีไซน์สไตล์นาฬิกาปลุก (Alarm Clock Style Destination Card)
 */
@Composable
fun DestinationAlarmCard(
    destination: Destination,
    isCurrentlyTracking: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyTracking) {
                TransitBlue.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("destination_card_${destination.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // ไอคอนประเภทการเดินทาง
                val (categoryIcon, categoryColor) = when (destination.category.uppercase()) {
                    "BTS" -> Pair(Icons.Default.DirectionsSubway, StationEmerald)
                    "MRT" -> Pair(Icons.Default.DirectionsSubway, TransitBlue)
                    "BTS/MRT" -> Pair(Icons.Default.DirectionsSubway, TransitBlue)
                    "TRAIN", "รถไฟ" -> Pair(Icons.Default.Train, ReadingAmber)
                    "BUS", "รถเมล์" -> Pair(Icons.Default.DirectionsBus, Color(0xFFF97316))
                    else -> Pair(Icons.Default.NearMe, TransitBlue)
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = destination.category,
                        tint = categoryColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = destination.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Badge เตือนล่วงหน้า
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ReadingAmber.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "เตือนล่วงหน้า ${destination.alertDistanceMeters} ม.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = ReadingAmberDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // พิกัด Lat/Lng ย่อ
                        Text(
                            text = String.format("%.4f, %.4f", destination.latitude, destination.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_destination_${destination.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "ลบเป้าหมาย",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "เข้าสู่หน้าจอติดตาม",
                    tint = TransitBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyDestinationsCard(onAddClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ReadingAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = ReadingAmber,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ยังไม่มีเป้าหมายที่บันทึกไว้",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "กดปุ่ม (+) เพื่อสร้างเป้าหมายใหม่ หรือดึงพิกัดปัจจุบันขณะยืนอยู่ที่สถานี",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReadingAmber,
                    contentColor = TransitNavy
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "สร้างเป้าหมายแรก",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
