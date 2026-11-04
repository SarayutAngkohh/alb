package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity เก็บข้อมูลเป้าหมาย/สถานีสำหรับแจ้งเตือน
 * ข้อมูลทั้งหมดถูกจัดเก็บไว้ในเครื่อง (Local Storage) ตามแนวทาง MVP
 */
@Entity(tableName = "destinations")
data class Destination(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val alertDistanceMeters: Int = 500, // ระยะเตือนล่วงหน้า (เมตร) เช่น 200, 500, 1000
    val category: String = "BTS",       // BTS, MRT, Train, Bus, General
    val createdAt: Long = System.currentTimeMillis()
)
