package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ReadingAmber
import com.example.ui.theme.StationEmerald
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddDestinationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, latitude: Double, longitude: Double, alertDistance: Int, category: String) -> Unit,
    onFetchCurrentLocation: (onSuccess: (Double, Double) -> Unit, onError: (String) -> Unit) -> Unit,
    isFetchingLocation: Boolean
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lngText by remember { mutableStateOf("") }
    var alertDistance by remember { mutableIntStateOf(500) }
    var customDistanceInput by remember { mutableStateOf("500") }
    var selectedCategory by remember { mutableStateOf("BTS") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("BTS", "MRT", "รถไฟ", "รถเมล์", "ทั่วไป")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TransitBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocation,
                                contentDescription = null,
                                tint = TransitBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "สร้างเป้าหมายใหม่",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "ปิด",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // หมวดหมู่
                Text(
                    text = "ประเภทการเดินทาง",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TransitBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ชื่อเป้าหมาย/สถานี
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("ชื่อเป้าหมาย / ชื่อสถานี") },
                    placeholder = { Text("เช่น สถานีสยาม, ป้ายหน้าสยามพารากอน") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("destination_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ปุ่มดึงพิกัดปัจจุบัน (Fetch Current Location)
                Button(
                    onClick = {
                        onFetchCurrentLocation(
                            { lat, lng ->
                                latText = lat.toString()
                                lngText = lng.toString()
                                Toast.makeText(context, "ดึงพิกัดปัจจุบันเรียบร้อย", Toast.LENGTH_SHORT).show()
                            },
                            { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("fetch_location_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StationEmerald.copy(alpha = 0.15f),
                        contentColor = StationEmerald
                    )
                ) {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = StationEmerald,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("กำลังค้นหาพิกัด GPS...")
                    } else {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ดึงพิกัดปัจจุบัน (บันทึกตอนยืนที่สถานี)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // พิกัด ละติจูด / ลองจิจูด กรอกด้วยตัวเองได้
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = {
                            latText = it
                            errorMessage = null
                        },
                        label = { Text("ละติจูด (Lat)") },
                        placeholder = { Text("13.7456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("latitude_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = {
                            lngText = it
                            errorMessage = null
                        },
                        label = { Text("ลองจิจูด (Lng)") },
                        placeholder = { Text("100.5348") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("longitude_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ระยะเตือนล่วงหน้า (3 Quick Presets: 300m, 500m, 1km + Custom TextField)
                Text(
                    text = "ระยะสั่นเตือนล่วงหน้า",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val is300 = alertDistance == 300
                    Button(
                        onClick = {
                            alertDistance = 300
                            customDistanceInput = "300"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (is300) ReadingAmber else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (is300) TransitNavy else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("300m", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    val is500 = alertDistance == 500
                    Button(
                        onClick = {
                            alertDistance = 500
                            customDistanceInput = "500"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (is500) ReadingAmber else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (is500) TransitNavy else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("500m", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    val is1000 = alertDistance == 1000
                    Button(
                        onClick = {
                            alertDistance = 1000
                            customDistanceInput = "1000"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (is1000) ReadingAmber else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (is1000) TransitNavy else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("1km", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customDistanceInput,
                    onValueChange = { input ->
                        customDistanceInput = input
                        val parsed = input.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            alertDistance = parsed
                        } else if (input.isEmpty()) {
                            alertDistance = 500
                        }
                    },
                    label = { Text("กำหนดระยะเอง (เมตร)") },
                    placeholder = { Text("500") },
                    suffix = { Text("เมตร") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ปุ่ม บันทึก & ยกเลิก
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ยกเลิก")
                    }

                    Button(
                        onClick = {
                            val trimmedName = name.trim()
                            val lat = latText.toDoubleOrNull()
                            val lng = lngText.toDoubleOrNull()

                            if (trimmedName.isEmpty()) {
                                errorMessage = "กรุณากรอกชื่อเป้าหมายหรือชื่อสถานี"
                                return@Button
                            }
                            if (lat == null || lng == null) {
                                errorMessage = "กรุณาระบุพิกัด ละติจูด และ ลองจิจูด ให้ถูกต้อง"
                                return@Button
                            }

                            onSave(trimmedName, lat, lng, alertDistance, selectedCategory)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("save_destination_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TransitBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "บันทึกเป้าหมาย",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}
