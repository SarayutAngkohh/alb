package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver สำหรับรับคำสั่งหยุดสั่นและปิดระบบติดตาม
 * เมื่อผู้ใช้กดปุ่ม "ปิดการแจ้งเตือน / ถึงแล้ว" จาก Notification
 */
class StopAlertReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_STOP_SERVICE = "com.example.alongthebooks.ACTION_STOP_SERVICE"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        TrackingForegroundService.stopTracking(context)
    }
}
