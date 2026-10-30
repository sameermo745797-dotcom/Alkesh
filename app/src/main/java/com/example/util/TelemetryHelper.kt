package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.example.model.SystemTelemetry

object TelemetryHelper {

    fun readTelemetry(context: Context): SystemTelemetry {
        // 1. Battery
        var batteryPct = 100
        var isCharging = false
        try {
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, iFilter)
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryPct = ((level / scale.toFloat()) * 100).toInt()
            }
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            // Ignore
        }

        // 2. Memory
        var availMb = 0L
        var totalMb = 0L
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            availMb = memInfo.availMem / (1024 * 1024)
            totalMb = memInfo.totalMem / (1024 * 1024)
        } catch (e: Exception) {
            // Ignore
        }

        // 3. Network
        var isConnected = false
        var netType = "OFFLINE"
        try {
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = connManager?.activeNetwork
            val caps = connManager?.getNetworkCapabilities(network)
            if (caps != null) {
                isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                netType = when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI (QUANTUM LINK)"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR (5G/LTE)"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET DIRECT"
                    else -> "CONNECTED"
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        return SystemTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            availableMemoryMb = availMb,
            totalMemoryMb = totalMb,
            isNetworkConnected = isConnected,
            networkType = netType
        )
    }
}
