package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class ReaderAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isKnownReader: Boolean
)

object ReadingAppsHelper {

    // แพ็กเกจแอปอ่านหนังสือและเอกสารยอดนิยม
    private val KNOWN_READER_PACKAGES = setOf(
        "com.google.android.apps.books",       // Google Play Books
        "com.amazon.kindle",                   // Amazon Kindle
        "com.askmedia.meb",                    // Meb E-Book
        "com.ookbee.ookbee",                   // Ookbee
        "com.dekd.novel",                      // Dek-D นิยาย
        "com.flyersoft.moonreader",            // Moon+ Reader
        "com.flyersoft.moonreaderp",           // Moon+ Reader Pro
        "com.adobe.reader",                    // Adobe Acrobat Reader
        "org.readera",                         // ReadEra
        "com.foobnix.pdf.reader",              // Librera
        "com.google.android.apps.docs",        // Google Drive / Docs
        "com.android.chrome"                   // เว็บบราวเซอร์สำหรับอ่านนิยายออนไลน์
    )

    fun getInstalledReaderApps(context: Context): List<ReaderAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val resultList = mutableListOf<ReaderAppInfo>()

        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            // ข้ามแอปตัวเอง
            if (packageName == context.packageName) continue

            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            val isKnown = KNOWN_READER_PACKAGES.contains(packageName) ||
                    appName.contains("book", ignoreCase = true) ||
                    appName.contains("read", ignoreCase = true) ||
                    appName.contains("อ่าน", ignoreCase = true) ||
                    appName.contains("นิยาย", ignoreCase = true) ||
                    appName.contains("pdf", ignoreCase = true)

            resultList.add(
                ReaderAppInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    isKnownReader = isKnown
                )
            )
        }

        // จัดเรียงให้แอปอ่านหนังสือหรือบราวเซอร์ขึ้นมาก่อน
        return resultList.sortedWith(
            compareByDescending<ReaderAppInfo> { it.isKnownReader }
                .thenBy { it.appName }
        )
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
