package com.example.cacun

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.TrafficStats
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

class MainActivity : ComponentActivity(), SensorEventListener {

    private val permissionRequestCode = 2001
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CAMERA
    )

    // Sensor Management
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var lightSensor: Sensor? = null

    // Audio Matrix
    private var audioManager: AudioManager? = null

    // Sensor State (Compose backed)
    private var accelX by mutableFloatStateOf(0f)
    private var accelY by mutableFloatStateOf(0f)
    private var accelZ by mutableFloatStateOf(0f)

    private var gyroX by mutableFloatStateOf(0f)
    private var gyroY by mutableFloatStateOf(0f)
    private var gyroZ by mutableFloatStateOf(0f)

    private var lightLux by mutableFloatStateOf(0f)

    // Battery Telemetry
    private var batteryPct by mutableIntStateOf(0)
    private var batteryVoltage by mutableFloatStateOf(0f)
    private var batteryCurrent by mutableFloatStateOf(0f) // In mA
    private var batteryTemp by mutableFloatStateOf(0f)
    private var batteryHealthStr by mutableStateOf("UNKNOWN")
    private var chargingPlugStr by mutableStateOf("DISCONNECTED")
    private var isChargingState by mutableStateOf(false)

    // Charging TimeLogs (Persisted in State)
    private var chargeStartTime by mutableStateOf("UNKNOWN")
    private var expectedFullTime by mutableStateOf("UNKNOWN")
    private var lastPlugTime by mutableStateOf("UNKNOWN")

    // Hardware Controls States
    private var isFlashlightOn by mutableStateOf(false)
    private var volumeMediaPercent by mutableFloatStateOf(0.5f)
    private var volumeRingPercent by mutableFloatStateOf(0.5f)
    private var volumeAlarmPercent by mutableFloatStateOf(0.5f)
    private var volumeNotificationPercent by mutableFloatStateOf(0.5f)
    private var screenBrightnessPercent by mutableFloatStateOf(0.7f)

    // Real-time oscilloscope data cap (low overhead)
    private val accelHistoryX = mutableStateListOf<Float>()
    private val accelHistoryY = mutableStateListOf<Float>()
    private val accelHistoryZ = mutableStateListOf<Float>()
    private val maxHistoryPoints = 65

    // Console logs list
    private val consoleLogs = mutableStateListOf<String>()

    // Current Telemetry Mode controlling frequency
    private var currentMode = mutableStateOf(TelemetryMode.STANDARD)

    enum class TelemetryMode(val label: String, val delayUs: Int, val description: String) {
        FAST("FAST EXEC", SensorManager.SENSOR_DELAY_FASTEST, "60ms rate - High Power"),
        STANDARD("STANDARD", SensorManager.SENSOR_DELAY_GAME, "200ms rate - Balances Power"),
        ECO("ECO MODE", SensorManager.SENSOR_DELAY_NORMAL, "1000ms rate - Minimal Battery Drain")
    }

    // Battery receiver to capture states on change
    private val batteryReceiver = object : BroadcastReceiver() {
        @SuppressLint("SimpleDateFormat")
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val oldCharging = isChargingState
                batteryPct = if (level != -1 && scale != -1) (level * 100 / scale) else 0

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isChargingState = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                chargingPlugStr = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB BUS"
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC CHARGER"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS COIL"
                    else -> "DISCONNECTED"
                }

                val tempMilliC = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                batteryTemp = tempMilliC / 10f

                val voltMv = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                batteryVoltage = voltMv / 1000f

                val health = it.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                batteryHealthStr = when (health) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER VOLTAGE"
                    BatteryManager.BATTERY_HEALTH_COLD -> "COLD"
                    else -> "UNKNOWN"
                }

                val bm = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                bm?.let { batteryMgr ->
                    val curNow = batteryMgr.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    batteryCurrent = curNow / 1000f // Convert microamps to milliamps
                }

                // Check plug-in event triggers
                val sdf = java.text.SimpleDateFormat("HH:mm:ss")
                val currentTime = sdf.format(Date())

                if (isChargingState && !oldCharging) {
                    chargeStartTime = currentTime
                    lastPlugTime = currentTime
                    calculateExpectedChargeTime()
                } else if (!isChargingState && oldCharging) {
                    chargeStartTime = "UNKNOWN"
                    expectedFullTime = "UNKNOWN"
                }

                addLog("[BAT] Telemetry packet received. Level: $batteryPct% | Temp: $batteryTemp°C")
            }
        }
    }

    private fun calculateExpectedChargeTime() {
        val remainingPct = 100 - batteryPct
        val currentMa = abs(batteryCurrent)
        if (currentMa > 100f) {
            val capacityMh = 5000f // Default capacity
            val mahNeeded = capacityMh * (remainingPct / 100f)
            val hours = (mahNeeded / currentMa) * 1.25f
            val minutes = (hours * 60).toInt()

            val sdf = java.text.SimpleDateFormat("HH:mm:ss")
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, minutes)
            expectedFullTime = sdf.format(calendar.time) + " ($minutes min)"
        } else {
            expectedFullTime = "CALCULATING..."
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        syncVolumeLevels()

        // Setup Sensor Services
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        }

        // Initialize logging
        addLog("[SYS] Cacun Telemetry Engine Initialized.")
        addLog("[SYS] Core Architecture: Kotlin x Java Interop Native.")
        addLog("[SYS] Low Power optimization toggles active.")

        // Check and request permissions
        if (checkPermissions()) {
            addLog("[SYS] Telephony and Camera permissions: GRANTED.")
        } else {
            addLog("[SYS] Permissions missing. Executing runtime access request...")
            ActivityCompat.requestPermissions(this, requiredPermissions, permissionRequestCode)
        }

        // Register initial battery parameters
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Set refresh rate to 90Hz+ if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val layoutParams = window.attributes
            layoutParams.preferredRefreshRate = 90f // Request 90Hz frame rendering explicitly
            window.attributes = layoutParams
        }

        setContent {
            MaterialYouSystemMonitorTheme {
                MainDashboardScreen()
            }
        }
    }

    private fun syncVolumeLevels() {
        audioManager?.let { am ->
            val maxMedia = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val curMedia = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            volumeMediaPercent = if (maxMedia > 0) curMedia.toFloat() / maxMedia else 0.5f

            val maxRing = am.getStreamMaxVolume(AudioManager.STREAM_RING)
            val curRing = am.getStreamVolume(AudioManager.STREAM_RING)
            volumeRingPercent = if (maxRing > 0) curRing.toFloat() / maxRing else 0.5f

            val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val curAlarm = am.getStreamVolume(AudioManager.STREAM_ALARM)
            volumeAlarmPercent = if (maxAlarm > 0) curAlarm.toFloat() / maxAlarm else 0.5f

            val maxNotification = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            val curNotification = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            volumeNotificationPercent = if (maxNotification > 0) curNotification.toFloat() / maxNotification else 0.5f
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                addLog("[SYS] Permissions granted by user. Reloading metrics...")
                Toast.makeText(this, "Hardware permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                addLog("[WRN] Some permissions denied. Telemetry restricted details shown.")
                Toast.makeText(this, "Hardware restricted details active", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Active Hardware Controls Logic ---

    // 1. Flashlight Control
    private fun toggleFlashlight(enable: Boolean) {
        try {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val rearCameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            if (rearCameraId != null) {
                cameraManager.setTorchMode(rearCameraId, enable)
                isFlashlightOn = enable
                addLog("[HW] Rear Flashlight Torch set to: ${if (enable) "ON" else "OFF"}")
            } else {
                addLog("[ERR] Flashlight toggle failed: No back camera found.")
            }
        } catch (e: Exception) {
            addLog("[ERR] Flashlight toggle error: ${e.message}")
        }
    }

    // 2. Audio Volume control
    private fun adjustVolume(percentage: Float, streamType: Int) {
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(streamType)
            val newVol = (percentage * max).toInt()
            am.setStreamVolume(streamType, newVol, 0)
            when (streamType) {
                AudioManager.STREAM_MUSIC -> volumeMediaPercent = percentage
                AudioManager.STREAM_RING -> volumeRingPercent = percentage
                AudioManager.STREAM_ALARM -> volumeAlarmPercent = percentage
                AudioManager.STREAM_NOTIFICATION -> volumeNotificationPercent = percentage
            }
            addLog("[HW] Volume stream ($streamType) modified to: ${(percentage * 100).toInt()}%")
        }
    }

    // 3. Screen Brightness Override (Local window override, no system permission required)
    private fun adjustWindowBrightness(percentage: Float) {
        val windowVal = when {
            percentage < 0.05f -> 0.05f
            percentage > 1.0f -> 1.0f
            else -> percentage
        }
        val layoutParams = window.attributes
        layoutParams.screenBrightness = windowVal
        window.attributes = layoutParams
        screenBrightnessPercent = percentage
        addLog("[HW] Window Brightness overridden: ${(percentage * 100).toInt()}%")
    }

    // 4. Haptic Pulse Generator
    private fun triggerVibration(type: Int) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    1 -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    2 -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                    else -> {
                        val timings = longArrayOf(0, 100, 100, 100, 100, 100, 300, 300, 100, 300, 100, 300, 300, 100, 100, 100, 100, 100)
                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255)
                        VibrationEffect.createWaveform(timings, amplitudes, -1)
                    }
                }
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    1 -> it.vibrate(50)
                    2 -> it.vibrate(200)
                    else -> it.vibrate(longArrayOf(0, 100, 100, 100, 100, 100, 300, 300, 100, 300, 100, 300, 300, 100, 100, 100, 100, 100), -1)
                }
            }
            addLog("[VIB] Haptic Feedback fired: ${if (type == 1) "CLICK" else if (type == 2) "THUMP" else "SOS PATTERN"}")
        }
    }

    // --- Control Center Shortcuts ---
    private fun launchSystemIntent(settingsAction: String, logLabel: String) {
        try {
            val intent = Intent(settingsAction)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            addLog("[SYS] Intent fired to redirect to: $logLabel settings.")
        } catch (e: Exception) {
            addLog("[ERR] Firing intent failed: ${e.message}")
        }
    }

    // --- Sensor Telemetry Hooks ---

    private fun registerSensors(mode: TelemetryMode) {
        sensorManager?.let { sm ->
            sm.unregisterListener(this)
            accelerometer?.let {
                sm.registerListener(this, it, mode.delayUs)
                addLog("[SYS] Accelerometer bound. Speed delay: ${mode.label}")
            }
            gyroscope?.let {
                sm.registerListener(this, it, mode.delayUs)
                addLog("[SYS] Gyroscope bound. Speed delay: ${mode.label}")
            }
            lightSensor?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                addLog("[SYS] Light Sensor bound.")
            }
        }
    }

    private fun unregisterSensors() {
        sensorManager?.unregisterListener(this)
        addLog("[SYS] Sensors unbound. Hardware shut down for low power.")
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registerSensors(currentMode.value)
        syncVolumeLevels()
    }

    override fun onPause() {
        super.onPause()
        unregisterSensors()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        addLog("[SYS] Background standby entered. Engine paused.")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSensors()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelX = it.values[0]
                accelY = it.values[1]
                accelZ = it.values[2]

                accelHistoryX.add(accelX)
                accelHistoryY.add(accelY)
                accelHistoryZ.add(accelZ)

                if (accelHistoryX.size > maxHistoryPoints) accelHistoryX.removeAt(0)
                if (accelHistoryY.size > maxHistoryPoints) accelHistoryY.removeAt(0)
                if (accelHistoryZ.size > maxHistoryPoints) accelHistoryZ.removeAt(0)

            } else if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                gyroX = it.values[0]
                gyroY = it.values[1]
                gyroZ = it.values[2]
            } else if (it.sensor.type == Sensor.TYPE_LIGHT) {
                lightLux = it.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        sensor?.let {
            addLog("[SYS] Sensor accuracy change: ${it.name} -> $accuracy")
        }
    }

    private fun addLog(message: String) {
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timestamp = formatter.format(Date())
        Handler(Looper.getMainLooper()).post {
            consoleLogs.add("[$timestamp] $message")
            if (consoleLogs.size > 80) {
                consoleLogs.removeAt(0)
            }
        }
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // --- Jetpack Compose UI Views ---

    @Composable
    fun MainDashboardScreen() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // 1. Display Pixel Colors Diagnostics Overlay state
        var isTestingColors by remember { mutableStateOf(false) }
        var activeColorIndex by remember { mutableIntStateOf(0) }
        val testColors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)

        // 2. Simulated Malware scan state
        var isScanningMalware by remember { mutableStateOf(false) }
        var scanProgress by remember { mutableFloatStateOf(0f) }
        var scanningFilePath by remember { mutableStateOf("") }
        var scannedAppsCount by remember { mutableIntStateOf(0) }

        // 3. Screen Time Query values
        var screenTimeTodayStr by remember { mutableStateOf("LOCKED") }
        var usagePermissionActive by remember { mutableStateOf(isUsageAccessGranted()) }
        val appStatsList = remember { mutableStateListOf<JavaHardwareScanner.AppDetail>() }
        val threatApps = remember { mutableStateListOf<JavaHardwareScanner.AppDetail>() }

        // Real-time Traffic Meter states
        var downloadSpeedMbps by remember { mutableFloatStateOf(0f) }
        var uploadSpeedMbps by remember { mutableFloatStateOf(0f) }
        val speedHistoryDownload = remember { mutableStateListOf<Float>() }
        val speedHistoryUpload = remember { mutableStateListOf<Float>() }

        // Bluetooth device states
        val bluetoothAdapter = remember { BluetoothAdapter.getDefaultAdapter() }
        var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled ?: false) }
        val bondedDevices = remember { mutableStateListOf<String>() }
        val bluetoothHistory = remember { mutableStateListOf<Float>() }

        // Core traffic stats & Bluetooth status loop
        LaunchedEffect(Unit) {
            var lastRx = TrafficStats.getTotalRxBytes()
            var lastTx = TrafficStats.getTotalTxBytes()
            var lastTime = System.currentTimeMillis()

            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                val curRx = TrafficStats.getTotalRxBytes()
                val curTx = TrafficStats.getTotalTxBytes()

                val rxDiff = curRx - lastRx
                val txDiff = curTx - lastTx
                val timeDiffSecs = (now - lastTime) / 1000f

                if (timeDiffSecs > 0f && rxDiff >= 0 && txDiff >= 0) {
                    downloadSpeedMbps = (rxDiff * 8f) / (1000000f * timeDiffSecs)
                    uploadSpeedMbps = (txDiff * 8f) / (1000000f * timeDiffSecs)

                    speedHistoryDownload.add(downloadSpeedMbps)
                    speedHistoryUpload.add(uploadSpeedMbps)

                    if (speedHistoryDownload.size > 40) speedHistoryDownload.removeAt(0)
                    if (speedHistoryUpload.size > 40) speedHistoryUpload.removeAt(0)
                }

                lastRx = curRx
                lastTx = curTx
                lastTime = now

                isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
                if (isBluetoothEnabled) {
                    try {
                        val devices = bluetoothAdapter?.bondedDevices
                        bondedDevices.clear()
                        if (devices != null && devices.isNotEmpty()) {
                            for (dev in devices) {
                                bondedDevices.add(dev.name ?: "UNKNOWN DEVICE")
                            }
                        } else {
                            bondedDevices.add("NO DEVS BOUNDED")
                        }
                    } catch (e: Exception) {
                        bondedDevices.clear()
                        bondedDevices.add("PERM RESTRICTED")
                    }
                } else {
                    bondedDevices.clear()
                    bondedDevices.add("BLUETOOTH OFF")
                }

                val nextWave = if (isBluetoothEnabled) (30..80).random().toFloat() else 0f
                bluetoothHistory.add(nextWave)
                if (bluetoothHistory.size > 40) bluetoothHistory.removeAt(0)
            }
        }

        fun triggerHeuristicScan() {
            coroutineScope.launch {
                isScanningMalware = true
                scanProgress = 0f
                scannedAppsCount = 0
                threatApps.clear()
                addLog("[SCAN] Initiating heuristical permissions scan over active packages...")
                
                try {
                    val pm = context.packageManager
                    val appList = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                    val total = appList.size
                    
                    for ((index, packageInfo) in appList.withIndex()) {
                        delay(20) // Smooth progression
                        scanProgress = (index + 1).toFloat() / total
                        scannedAppsCount = index + 1
                        scanningFilePath = packageInfo.packageName
                        
                        val appName = packageInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageInfo.packageName
                        val isSystem = ((packageInfo.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                        
                        if (!isSystem) {
                            val installer = try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    pm.getInstallSourceInfo(packageInfo.packageName).installingPackageName
                                } else {
                                    pm.getInstallerPackageName(packageInfo.packageName)
                                }
                            } catch (e: Exception) { null }
                            
                            val isSideloaded = installer == null || installer.isEmpty()
                            val requested = packageInfo.requestedPermissions
                            var dangerousCount = 0
                            if (requested != null) {
                                val dangerousList = listOf(
                                    "android.permission.READ_SMS",
                                    "android.permission.RECEIVE_SMS",
                                    "android.permission.SEND_SMS",
                                    "android.permission.SYSTEM_ALERT_WINDOW",
                                    "android.permission.WRITE_SETTINGS",
                                    "android.permission.ACCESS_FINE_LOCATION"
                                )
                                for (p in requested) {
                                    if (dangerousList.contains(p)) dangerousCount++
                                }
                            }
                            
                            if (isSideloaded && dangerousCount >= 1) {
                                val detail = JavaHardwareScanner.AppDetail().apply {
                                    name = appName
                                    packageName = packageInfo.packageName
                                    installSource = "APK / SIDE-LOAD"
                                    securityScore = 100 - (dangerousCount * 18)
                                    this.isSystem = false
                                }
                                threatApps.add(detail)
                                addLog("[WRN] Threat detected: $appName ($dangerousCount dangerous permissions)")
                            }
                        }
                    }
                    
                    delay(200)
                    isScanningMalware = false
                    if (threatApps.isEmpty()) {
                        addLog("[SCAN] Scan complete. 0 threats detected. Device is secured.")
                        Toast.makeText(context, "Shield Scan Completed: System Secured", Toast.LENGTH_SHORT).show()
                    } else {
                        addLog("[WRN] Scan complete. Flagged ${threatApps.size} security threats.")
                        Toast.makeText(context, "Shield Scan: Flagged ${threatApps.size} risks!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    isScanningMalware = false
                    addLog("[ERR] Scanning interrupted: ${e.message}")
                }
            }
        }

        // Trigger updates dynamically for Screen Time
        LaunchedEffect(usagePermissionActive) {
            if (usagePermissionActive) {
                try {
                    val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - (1000L * 60 * 60 * 24) // 24 hours
                    val usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                    
                    var totalTimeMs = 0L
                    for (stat in usageStats) {
                        totalTimeMs += stat.totalTimeInForeground
                    }
                    val totalHrs = totalTimeMs / (1000 * 60 * 60)
                    val totalMins = (totalTimeMs % (1000 * 60 * 60)) / (1000 * 60)
                    screenTimeTodayStr = "${totalHrs}h ${totalMins}m"

                    val apps = JavaHardwareScanner.getInstalledApps(context)
                    appStatsList.clear()
                    appStatsList.addAll(apps.sortedBy { it.isSystem }.take(15))
                } catch (e: Exception) {
                    screenTimeTodayStr = "ERR: NO STATS"
                }
            }
        }

        // 4. Boot Screen Animation sequence
        var isBooting by remember { mutableStateOf(true) }
        val bootLogs = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
            val bootSequence = listOf(
                "Initializing Cacun OS Kernel Loader...",
                "Mounting dynamic Material You colour tokens... OK",
                "Establishing telemetry socket connections... OK",
                "Scanning accelerometer, gyroscope, light arrays...",
                "Checking network operators & modem layers...",
                "Calculating charging brick & battery parameters...",
                "Resolving secure codes & updates lifecycle...",
                "Boot Diagnostics Completed. Launching Telemetry HUD..."
            )
            for (log in bootSequence) {
                delay(280)
                bootLogs.add("[BOOT] $log")
            }
            delay(350)
            isBooting = false
        }

        // Color test screen
        if (isTestingColors) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(testColors[activeColorIndex])
                    .clickable {
                        if (activeColorIndex < testColors.size - 1) {
                            activeColorIndex++
                        } else {
                            isTestingColors = false
                            activeColorIndex = 0
                            addLog("[DSP] Pixel color check complete.")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DISPLAY PIXEL CHECKING MODE\nTap to cycle colors (RGB / White / Black)\n\nColor: ${activeColorIndex + 1}/${testColors.size}",
                    color = if (testColors[activeColorIndex] == Color.White) Color.Black else Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }
            return
        }

        // Hacker Terminal style booting view
        AnimatedVisibility(
            visible = isBooting,
            exit = fadeOut(animationSpec = tween(600)) + slideOutVertically(targetOffsetY = { -it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF040508))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cacun),
                        contentDescription = "Cacun Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .aspectRatio(2.8f)
                            .padding(bottom = 16.dp)
                    )
                    Text(
                        text = "CACUN SYSTEM OS v2.5",
                        color = Color(0xFF00FFCC),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color(0xFF07090D))
                            .border(1.dp, Color(0x3300FFCC))
                            .padding(12.dp)
                    ) {
                        bootLogs.forEach { log ->
                            Text(
                                text = log,
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF00FFCC),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // If boot is completed, show main screen
        if (!isBooting) {
            // Hardware constants
            val model = Build.MODEL
            val brand = Build.BRAND.uppercase()
            val manufacturer = Build.MANUFACTURER.uppercase()
            val androidVersion = Build.VERSION.RELEASE
            val sdkVersion = Build.VERSION.SDK_INT
            val patchLevel = Build.VERSION.SECURITY_PATCH

            // RAM status
            val totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f)
            val availRamGb = memoryInfo.availMem / (1024f * 1024f * 1024f)
            val usedRamGb = totalRamGb - availRamGb
            val usedRamPercent = if (memoryInfo.totalMem > 0) {
                ((memoryInfo.totalMem - memoryInfo.availMem) * 100f / memoryInfo.totalMem).toInt()
            } else 0

            // Storage status
            val totalStorageGb = totalStorageBytes / (1024f * 1024f * 1024f)
            val usedStorageGb = (totalStorageBytes - availStorageBytes) / (1024f * 1024f * 1024f)
            val usedStoragePercent = if (totalStorageBytes > 0) {
                ((totalStorageBytes - availStorageBytes) * 100f / totalStorageBytes).toInt()
            } else 0

            // Display parameters
            val metrics = JavaHardwareScanner.getDisplayMetrics(context)
            val widthPx = metrics.widthPixels
            val heightPx = metrics.heightPixels
            val densityDpi = metrics.densityDpi
            val refreshRate = JavaHardwareScanner.getRefreshRate(context)

            // Dynamic network details
            val networkType = JavaHardwareScanner.getNetworkType(context)
            val linkSpeed = JavaHardwareScanner.getLinkSpeedMbps(context)

            // SIM Operator details
            val simOperator = remember { JavaHardwareScanner.getSimOperatorName(context) }
            val imei = remember { JavaHardwareScanner.attemptImeiRead(context) }
            val androidId = remember { JavaHardwareScanner.getAndroidId(context) }
            val nfcStatus = JavaHardwareScanner.getNfcStatus(context)
            val isRooted = remember { JavaHardwareScanner.checkRootAccess() }
            val cameraSpecs = remember { JavaHardwareScanner.getCameraCharacteristics(context) }

            // Estimate 5G modem details
            val chipName = Build.HARDWARE.lowercase()
            val modemDetails = when {
                chipName.contains("qcom") || chipName.contains("sm") || chipName.contains("sdm") -> {
                    if (sdkVersion >= 33) "Snapdragon X70 5G RF (Integrated)"
                    else "Snapdragon X65/X60 5G Modem"
                }
                chipName.contains("exynos") || chipName.contains("s5e") -> {
                    "Exynos 5300 5G Modem (High Efficiency)"
                }
                chipName.contains("mt") || chipName.contains("dimensity") -> {
                    "MediaTek Helio/Dimensity M80 5G"
                }
                else -> "Multi-band LTE/5G Baseband Controller"
            }

            // Power Brick & Cable calculations
            val batteryCurrentA = batteryCurrent / 1000f
            val batteryPowerW = abs(batteryVoltage * batteryCurrentA)
            
            val brickEstimate = when {
                !isChargingState -> "DISCONNECTED"
                batteryPowerW <= 0f -> "DISCONNECTED"
                else -> {
                    val rawPower = batteryPowerW / 0.85f
                    when {
                        rawPower <= 6f -> "5W Standard Brick"
                        rawPower <= 11f -> "10W Fast Brick"
                        rawPower <= 16f -> "15W Pro Charge Brick"
                        rawPower <= 20f -> "18W Power Delivery Brick"
                        rawPower <= 25f -> "22.5W Quick Charge Brick"
                        rawPower <= 36f -> "33W Super Charge Brick"
                        rawPower <= 48f -> "45W Ultra Charge Brick"
                        rawPower <= 75f -> "67W Hyper Charge Brick"
                        rawPower <= 90f -> "80W Turbo Charge Brick"
                        else -> "120W+ Ultra Hyper-Charge Brick"
                    }
                }
            }

            val cableEstimate = when {
                !isChargingState -> "DISCONNECTED"
                abs(batteryCurrent) <= 1000f -> "Standard Cable (1A Rating)"
                abs(batteryCurrent) <= 2100f -> "Fast Charging Cable (2A Rating)"
                abs(batteryCurrent) <= 3200f -> "Hi-Speed Cable (3A Rating)"
                else -> "Heavy Copper Cable (5A/6A Rating)"
            }

            // Lifespan calculations
            val releaseYear = when (sdkVersion) {
                35 -> 2024
                34 -> 2023
                33 -> 2022
                32, 31 -> 2021
                30 -> 2020
                29 -> 2019
                28 -> 2018
                else -> 2017
            }
            val currentYear = 2026
            val deviceAge = currentYear - releaseYear
            val supportDuration = when {
                brand.contains("SAMSUNG") || brand.contains("GOOGLE") -> 7
                brand.contains("ONEPLUS") || brand.contains("XIAOMI") -> 4
                else -> 3
            }
            val updatesRemaining = if ((supportDuration - deviceAge) > 0) (supportDuration - deviceAge) else 0

            val healthFactor = if (batteryHealthStr == "GOOD") 1.0f else 0.7f
            val ageTax = deviceAge * 0.85f
            val remainingLifeVal = (healthFactor * 5.5f) - ageTax
            val estimatedLifespanYears = if (remainingLifeVal > 0.5f) String.format(Locale.US, "%.1f", remainingLifeVal) else "0.5 (Wear Warning)"

            // Entrance Slide animations triggered when loaded
            var isDashboardVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(100)
                isDashboardVisible = true
            }

            val entranceOffsetY by animateDpAsState(
                targetValue = if (isDashboardVisible) 0.dp else 40.dp,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                label = "offset"
            )
            val entranceAlpha by animateFloatAsState(
                targetValue = if (isDashboardVisible) 1.0f else 0f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                label = "alpha"
            )

            val scrollState = rememberScrollState()

            // Dynamic grid colors and background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .alpha(entranceAlpha)
                    .offset(y = entranceOffsetY)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp)
                ) {
                    val isTablet = maxWidth >= 600.dp

                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // CENTERED CACUN LOGO ONLY
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cacun),
                                contentDescription = "Cacun Logo Header",
                                modifier = Modifier
                                    .height(42.dp)
                                    .aspectRatio(2.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Scrollable Main Section
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState)
                        ) {
                            if (isTablet) {
                                // Tablet 2-Column Responsive Layout
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Column 1
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        PerformanceSpeedController()
                                        LiveOscilloscopePlot(lightLux, refreshRate)
                                        NetworkSpeedDiagnosticsWidget(
                                            downloadSpeed = downloadSpeedMbps,
                                            uploadSpeed = uploadSpeedMbps,
                                            downloadHistory = speedHistoryDownload,
                                            uploadHistory = speedHistoryUpload,
                                            simOperator = simOperator,
                                            networkType = modemDetails,
                                            linkSpeed = 433
                                        )
                                        BluetoothDiagnosticsWidget(
                                            isEnabled = isBluetoothEnabled,
                                            devices = bondedDevices,
                                            history = bluetoothHistory,
                                            onToggleBt = {
                                                if (isBluetoothEnabled) {
                                                    try {
                                                        bluetoothAdapter?.disable()
                                                        isBluetoothEnabled = false
                                                        addLog("[BT] Bluetooth disabled.")
                                                    } catch (e: Exception) {
                                                        launchSystemIntent(Settings.ACTION_BLUETOOTH_SETTINGS, "BLUETOOTH")
                                                    }
                                                } else {
                                                    try {
                                                        bluetoothAdapter?.enable()
                                                        isBluetoothEnabled = true
                                                        addLog("[BT] Bluetooth enabled.")
                                                    } catch (e: Exception) {
                                                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                                        context.startActivity(enableBtIntent)
                                                    }
                                                }
                                            }
                                        )
                                        BatteryInfusionModule(batteryPowerW, brickEstimate, cableEstimate)
                                        InteractiveHardwareControls(context)
                                    }

                                    // Column 2
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        SystemDiagnosticConsole()
                                        AntiVirusScannerSection(
                                            isScanning = isScanningMalware,
                                            progress = scanProgress,
                                            filePath = scanningFilePath,
                                            count = scannedAppsCount,
                                            threatApps = threatApps,
                                            onTriggerScan = { triggerHeuristicScan() }
                                        )
                                        ScreenTimeAnalyticsCard(usagePermissionActive, screenTimeTodayStr, appStatsList, onOpenSettings = {
                                            launchSystemIntent(Settings.ACTION_USAGE_ACCESS_SETTINGS, "USAGE STATS")
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                usagePermissionActive = isUsageAccessGranted()
                                            }, 2000)
                                        })
                                        VolatileStorageSectors(usedRamPercent, usedRamGb, totalRamGb, usedStoragePercent, usedStorageGb, totalStorageGb)
                                        HardwareICDirectory(manufacturer, model, androidVersion, sdkVersion, patchLevel, widthPx, heightPx, densityDpi, refreshRate, nfcStatus, imei, androidId, cameraSpecs, isRooted, modemDetails, simOperator, deviceAge, estimatedLifespanYears, updatesRemaining, onLaunchColorTest = {
                                            isTestingColors = true
                                        })
                                    }
                                }
                            } else {
                                // Phone 1-Column Portrait Layout
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    PerformanceSpeedController()
                                    LiveOscilloscopePlot(lightLux, refreshRate)
                                    NetworkSpeedDiagnosticsWidget(
                                        downloadSpeed = downloadSpeedMbps,
                                        uploadSpeed = uploadSpeedMbps,
                                        downloadHistory = speedHistoryDownload,
                                        uploadHistory = speedHistoryUpload,
                                        simOperator = simOperator,
                                        networkType = modemDetails,
                                        linkSpeed = 433
                                    )
                                    BluetoothDiagnosticsWidget(
                                        isEnabled = isBluetoothEnabled,
                                        devices = bondedDevices,
                                        history = bluetoothHistory,
                                        onToggleBt = {
                                            if (isBluetoothEnabled) {
                                                try {
                                                    bluetoothAdapter?.disable()
                                                    isBluetoothEnabled = false
                                                    addLog("[BT] Bluetooth disabled.")
                                                } catch (e: Exception) {
                                                    launchSystemIntent(Settings.ACTION_BLUETOOTH_SETTINGS, "BLUETOOTH")
                                                }
                                            } else {
                                                try {
                                                    bluetoothAdapter?.enable()
                                                    isBluetoothEnabled = true
                                                    addLog("[BT] Bluetooth enabled.")
                                                } catch (e: Exception) {
                                                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                                    context.startActivity(enableBtIntent)
                                                }
                                            }
                                        }
                                    )
                                    BatteryInfusionModule(batteryPowerW, brickEstimate, cableEstimate)
                                    InteractiveHardwareControls(context)
                                    SystemDiagnosticConsole()
                                    AntiVirusScannerSection(
                                        isScanning = isScanningMalware,
                                        progress = scanProgress,
                                        filePath = scanningFilePath,
                                        count = scannedAppsCount,
                                        threatApps = threatApps,
                                        onTriggerScan = { triggerHeuristicScan() }
                                    )
                                    ScreenTimeAnalyticsCard(usagePermissionActive, screenTimeTodayStr, appStatsList, onOpenSettings = {
                                        launchSystemIntent(Settings.ACTION_USAGE_ACCESS_SETTINGS, "USAGE STATS")
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            usagePermissionActive = isUsageAccessGranted()
                                        }, 2500)
                                    })
                                    VolatileStorageSectors(usedRamPercent, usedRamGb, totalRamGb, usedStoragePercent, usedStorageGb, totalStorageGb)
                                    HardwareICDirectory(manufacturer, model, androidVersion, sdkVersion, patchLevel, widthPx, heightPx, densityDpi, refreshRate, nfcStatus, imei, androidId, cameraSpecs, isRooted, modemDetails, simOperator, deviceAge, estimatedLifespanYears, updatesRemaining, onLaunchColorTest = {
                                        isTestingColors = true
                                    })
                                }
                            }

                            // DEVELOPER CREDENTIALS & LICENSES FOOTER
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "[ LEGAL PRIVACY CHARTER ]",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "By accessing Cacun HUD hardware logs, you authorize localized sandbox reading of sensors, battery broadcast configurations, and storage directories. No metadata is shared offboard. Your telephony security keys (IMEI) remain local and are protected by Android security exception sandboxes.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Image(
                                painter = painterResource(id = R.drawable.cacun),
                                contentDescription = "Cacun Watermark",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .height(26.dp)
                                    .aspectRatio(2.8f)
                                    .alpha(0.3f)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // Developer Info Block
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val date = java.text.SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())
                                val day = java.text.SimpleDateFormat("EEEE", Locale.US).format(Date())
                                val time = java.text.SimpleDateFormat("HH:mm z", Locale.US).format(Date())
                                
                                Text(
                                    text = "$day | $date | $time",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "USER PROFILE: ROOT ADMINISTRATOR",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "DEVELOPER: AYUSH HARINKHEDE",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "CONTACT: ayushharinkhere2005@gmail.com",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.clickable {
                                        try {
                                            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:ayushharinkhere2005@gmail.com")
                                            }
                                            context.startActivity(mailIntent)
                                        } catch (e: Exception) {}
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    // --- Sub components ---

    @Composable
    fun PerformanceSpeedController() {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> TELEMETRY DRAIN CONTROLLER",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Eco Mode throttles the hardware listeners, minimizing CPU cycles and battery resource consumption.",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryMode.values().forEach { mode ->
                        val isSelected = currentMode.value == mode
                        Button(
                            onClick = {
                                currentMode.value = mode
                                registerSensors(mode)
                                addLog("[PWR] Telemetry rate adjusted to: ${mode.label}")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Text(
                                text = mode.label,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LiveOscilloscopePlot(lightValue: Float, refreshRate: Float) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CpuIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "> SENSOR VECTOR PLOTTER & LIGHT READS",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "REFRESH RATE: ${refreshRate.toInt()} Hz",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .background(Color(0xFF06070B))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    val width = size.width
                    val height = size.height
                    val midY = height / 2f
                    val points = accelHistoryX.size

                    if (points > 1) {
                        val xStep = width / (maxHistoryPoints - 1)
                        drawLine(
                            color = Color(0x16FFFFFF),
                            start = Offset(0f, midY),
                            end = Offset(width, midY),
                            strokeWidth = 1f
                        )

                        val pathX = Path()
                        val pathY = Path()
                        val pathZ = Path()

                        for (i in 0 until points) {
                            val currentX = i * xStep
                            val scale = midY / 20f

                            val valX = midY - (accelHistoryX.getOrNull(i) ?: 0f) * scale
                            val valY = midY - (accelHistoryY.getOrNull(i) ?: 0f) * scale
                            val valZ = midY - (accelHistoryZ.getOrNull(i) ?: 0f) * scale

                            if (i == 0) {
                                pathX.moveTo(currentX, valX)
                                pathY.moveTo(currentX, valY)
                                pathZ.moveTo(currentX, valZ)
                            } else {
                                pathX.lineTo(currentX, valX)
                                pathY.lineTo(currentX, valY)
                                pathZ.lineTo(currentX, valZ)
                            }
                        }

                        drawPath(pathX, Color(0xFF00FFCC), style = Stroke(width = 2.5f))
                        drawPath(pathY, Color(0xFFBD00FF), style = Stroke(width = 2.5f))
                        drawPath(pathZ, Color(0xFFFFB300), style = Stroke(width = 2.5f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("X: ${String.format(Locale.US, "%+.2f", accelX)}", color = Color(0xFF00FFCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Y: ${String.format(Locale.US, "%+.2f", accelY)}", color = Color(0xFFBD00FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Z: ${String.format(Locale.US, "%+.2f", accelZ)}", color = Color(0xFFFFB300), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BrightnessIcon(color = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                        Text("LIGHT: ${lightValue.toInt()} LUX", color = Color(0xFF00E5FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LIGHT LEVEL INTENSITY BAR", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${lightValue.toInt()} LUMENS", color = Color(0xFFFFB300), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                val lightProgress = (lightValue / 1000f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { lightProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFB300),
                    trackColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }

    @Composable
    fun BatteryInfusionModule(batteryPowerW: Float, brickRating: String, cableRating: String) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BatteryIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "> CHARGER ENGINE & BATTERY CALCULATOR",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { batteryPct / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = if (batteryPct > 20) MaterialTheme.colorScheme.primary else Color(0xFFFF3B30),
                            strokeWidth = 5.dp,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$batteryPct%",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isChargingState) "CHARGE" else "DRAIN",
                                color = if (isChargingState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        CodeDataRow("BATTERY DESIGN CAP", "5000 mAh")
                        CodeDataRow("HEALTH CORE STATUS", batteryHealthStr, if(batteryHealthStr == "GOOD") Color(0xFF00FFCC) else Color(0xFFFFB300))
                        CodeDataRow("INLET WATTAGE", "${String.format(Locale.US, "%.2f", batteryPowerW)} W")
                        CodeDataRow("ESTIMATED BRICK", brickRating, Color(0xFF00E5FF))
                        CodeDataRow("CABLE RATING FLOW", cableRating)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                // Time Logs Section
                Text(
                    text = "[ CHARGER INTENSITY TIME-LOG ]",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                CodeDataRow("PLUG IN TIME", chargeStartTime)
                CodeDataRow("EXPECTED FULL BY", expectedFullTime, if (isChargingState) Color(0xFF00FFCC) else Color.White)
                CodeDataRow("LAST DISCONNECT TIME", lastPlugTime)

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "[ POWER ESTIMATION FORMULAE ]",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Wattage Equation: P = V × I\n" +
                           "  - Live Volts (V): ${String.format(Locale.US, "%.3f", batteryVoltage)} V\n" +
                           "  - Live Current (I): ${String.format(Locale.US, "%.1f", batteryCurrent)} mA\n" +
                           "  - Result Power (P): ${String.format(Locale.US, "%.2f", batteryPowerW)} W\n" +
                           "• Brick Rating: Estimated Power / Efficiency (0.85)\n" +
                           "• Charger Cable Flow Rating: (Current > 3000mA) ? 5A/6A : 3A",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 12.sp
                )
            }
        }
    }

    @Composable
    fun InteractiveHardwareControls(context: Context) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "> QUICK CONTROL CENTRE & VOLUMES",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Control Center Grid Button Toggles in Code-Icon Way
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val controlCenterButtons = listOf(
                        ControlItem("WIFI", Settings.ACTION_WIFI_SETTINGS, "WIFI", { color: Color -> WifiIcon(color, Modifier.size(16.dp)) }),
                        ControlItem("BT", Settings.ACTION_BLUETOOTH_SETTINGS, "BLUETOOTH", { color: Color -> BluetoothIcon(color, Modifier.size(16.dp)) }),
                        ControlItem("DATA", Settings.ACTION_DATA_ROAMING_SETTINGS, "ROAMING", { color: Color -> SignalIcon(4, color, Modifier.size(16.dp)) }),
                        ControlItem("FLIGHT", Settings.ACTION_AIRPLANE_MODE_SETTINGS, "FLIGHT MODE", { color: Color -> SettingsIcon(color, Modifier.size(16.dp)) }),
                        ControlItem("HOTSPOT", Settings.ACTION_WIRELESS_SETTINGS, "HOTSPOT", { color: Color -> SpeedIcon(color, Modifier.size(16.dp)) })
                    )

                    controlCenterButtons.forEach { btn ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { launchSystemIntent(btn.action, btn.logName) }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            btn.icon(MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = btn.label,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Flashlight Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FlashlightIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                        Column {
                            Text("REAR OPTICAL TORCH", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Toggle physical camera LED flash", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Switch(
                        checked = isFlashlightOn,
                        onCheckedChange = { toggleFlashlight(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                // Brightness Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BrightnessIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Text("LOCAL SCREEN BRIGHTNESS OVERRIDE", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Text("${(screenBrightnessPercent * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = screenBrightnessPercent,
                        onValueChange = { adjustWindowBrightness(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                // System Volumes Matrix
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VolumeIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    Text("SYSTEM VOLUME MATRIX CONTROLS", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                VolumeSliderRow("MEDIA VOLUME", volumeMediaPercent, AudioManager.STREAM_MUSIC)
                VolumeSliderRow("RING VOLUME", volumeRingPercent, AudioManager.STREAM_RING)
                VolumeSliderRow("ALARM VOLUME", volumeAlarmPercent, AudioManager.STREAM_ALARM)
                VolumeSliderRow("NOTIF VOLUME", volumeNotificationPercent, AudioManager.STREAM_NOTIFICATION)

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                // Haptic Generator
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("HAPTIC MOTOR TRIGGER TEST", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { triggerVibration(1) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text("CLICK", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Button(
                            onClick = { triggerVibration(2) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text("THUMP", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Button(
                            onClick = { triggerVibration(3) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text("SOS WAVE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun VolumeSliderRow(label: String, value: Float, streamType: Int) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("${(value * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = value,
                onValueChange = { adjustVolume(it, streamType) },
                modifier = Modifier.height(28.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }

    @Composable
    fun SystemDiagnosticConsole() {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> KERNEL DIAGNOSTICS CONSOLE LOGS",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "[WIPE]",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { consoleLogs.clear(); addLog("[SYS] Console swept clean.") }
                            .padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFF040608))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    val logScrollState = rememberScrollState()
                    LaunchedEffect(consoleLogs.size) {
                        logScrollState.animateScrollTo(logScrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(logScrollState)
                    ) {
                        consoleLogs.forEach { log ->
                            val isErr = log.contains("[ERR]") || log.contains("[WRN]")
                            val color = if (isErr) Color(0xFFFF3B30) else Color(0xFF00FFCC)
                            Text(
                                text = log,
                                color = if (log.contains("[SYS]")) Color(0xB3FFFFFF) else color,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AntiVirusScannerSection(
        isScanning: Boolean,
        progress: Float,
        filePath: String,
        count: Int,
        threatApps: List<JavaHardwareScanner.AppDetail>,
        onTriggerScan: () -> Unit
    ) {
        val context = LocalContext.current
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> SHIELD ANTI-VIRUS FILE INTEGRITY",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (isScanning) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SCANNING DIRECTORIES...", color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "FILE: $filePath",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "Verified: $count apps scanned.",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Local Shield Scanner", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Run integrity scan on installed binaries.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Button(
                            onClick = onTriggerScan,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text("RUN SHIELD", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                if (!isScanning && threatApps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "[ FLAGGED RISKS & UNINSTALL SHIELD ]",
                        color = Color(0xFFFF3B30),
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    threatApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
                                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(app.name, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(app.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                                Text("Security Score: ${app.securityScore}%", color = Color(0xFFFFB300), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Button(
                                onClick = {
                                    try {
                                        val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                                            data = Uri.parse("package:${app.packageName}")
                                        }
                                        context.startActivity(uninstallIntent)
                                    } catch (e: Exception) {
                                        addLog("[ERR] Failed to initiate uninstallation.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("RESOLVE", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ScreenTimeAnalyticsCard(
        permissionGranted: Boolean,
        screenTimeToday: String,
        appsList: List<JavaHardwareScanner.AppDetail>,
        onOpenSettings: () -> Unit
    ) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> SCREEN TIME & APP ANALYTICS",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (!permissionGranted) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Usage statistics settings are restricted. Authorize app usage query to fetch screen time.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenSettings,
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text("AUTHORIZE SCREEN DIAGNOSTICS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("SCREEN TIME TODAY", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(screenTimeToday, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DAILY AVG", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("5h 12m", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "[ PHYSIOLOGICAL AFFECTS ]",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Warning: Exceeding 4.5 hours of daily visual screen exposure suppresses melatonin synthesis and leads to progressive digital eye strain.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "[ INSTALLED APPS DIRECTORY ]",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Top apps listing
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            appsList.take(5).forEach { app ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(app.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Text(app.installSource, color = if(app.installSource == "PLAY STORE") Color(0xFF00FFCC) else Color(0xFFFFB300), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    
                                    // Security score percentage
                                    Text(
                                        text = "${app.securityScore}% SECURE",
                                        color = if (app.securityScore >= 90) Color(0xFF00FFCC) else Color(0xFFFFB300),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )

                                    // Significance Star Rating
                                    Text(
                                        text = if(app.isSystem) "★★★★★" else "★★★☆☆",
                                        color = Color(0xFFFFB300),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun VolatileStorageSectors(
        usedRamPercent: Int, usedRamGb: Float, totalRamGb: Float,
        usedStoragePercent: Int, usedStorageGb: Float, totalStorageGb: Float
    ) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> STORAGE BLOCK STRUCTURE ALLOC",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                ProgressDataRow(
                    label = "RAM SYSTEM CACHE",
                    percent = usedRamPercent,
                    detailsStr = "${String.format(Locale.US, "%.2f", usedRamGb)} GB / ${String.format(Locale.US, "%.2f", totalRamGb)} GB",
                    barColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProgressDataRow(
                    label = "INTERNAL EMMC BLOCK",
                    percent = usedStoragePercent,
                    detailsStr = "${String.format(Locale.US, "%.1f", usedStorageGb)} GB / ${String.format(Locale.US, "%.1f", totalStorageGb)} GB",
                    barColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

    @Composable
    fun HardwareICDirectory(
        manufacturer: String, model: String, androidVersion: String, sdkVersion: Int, securityPatch: String,
        widthPx: Int, heightPx: Int, densityDpi: Int, refreshRate: Float,
        nfcStatus: String, imei: String, androidId: String, cameraSpecs: List<String>,
        isRooted: Boolean, modemModel: String, simOperator: String,
        deviceAge: Int, lifespanYears: String, updatesRemaining: Int,
        onLaunchColorTest: () -> Unit
    ) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> INTEGRATED HARDWARE IC DIRECTORY",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val gpuName = remember { JavaHardwareScanner.getGpuRenderer() }
                val socName = remember { JavaHardwareScanner.getSocModel() }
                val cpuFreq = JavaHardwareScanner.getCpuFrequency()
                val cpuMax = JavaHardwareScanner.getCpuMaxFrequency()
                val cpuFreqStr = if (cpuFreq > 0) "$cpuFreq MHz / $cpuMax MHz" else "DYNAMIC CONTROL"

                CodeDataRow("MANUFACTURER", manufacturer)
                CodeDataRow("PRODUCT MODEL", model)
                CodeDataRow("HARDWARE SOC CHIP", socName)
                CodeDataRow("GPU CORE ACCEL", gpuName)
                CodeDataRow("CPU CORES FREQ", "$cpuFreqStr (${Runtime.getRuntime().availableProcessors()} Cores)")
                CodeDataRow("SYSTEM THREADS", "${Thread.activeCount()} Threads Active", Color(0xFFFFB300))
                CodeDataRow("INTEGRITY STATUS", if(isRooted) "ROOTED / UNLOCKED" else "VERIFIED / SECURE", if(isRooted) Color(0xFFFFB300) else Color(0xFF00FFCC))
                CodeDataRow("5G MODEM PROFILE", modemModel)
                CodeDataRow("CARRIER OPERATOR", simOperator, Color(0xFF00FFCC))
                CodeDataRow("NFC ANTENNA LINK", nfcStatus, if (nfcStatus == "ACTIVE") Color(0xFF00FFCC) else Color(0xFFFFB300))

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))

                // Custom Android Version Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OS VERSION ENGINE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ANDROID $androidVersion (API $sdkVersion)",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                CodeDataRow("SECURITY PATCH", securityPatch)

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))

                // Device Age and Lifespan
                Text(
                    text = "[ HARDWARE LIFE CYCLE ]",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                CodeDataRow("PHYSICAL AGE", "$deviceAge Years since launch")
                CodeDataRow("EXPECTED LIFESPAN", "$lifespanYears Years remaining", Color(0xFF00FFCC))
                CodeDataRow("OTA UPDATES LEFT", "$updatesRemaining updates remaining")

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                // Display check triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DISPLAY MATRIX", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("${widthPx}x${heightPx} @ ${refreshRate.toInt()}Hz", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = onLaunchColorTest,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("COLOR CHECK", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "[SECURE IDENTITY CODES]",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                CodeDataRow("IMEI TELEPHONY", imei, if (imei.startsWith("SECURE")) Color(0xFFFFB300) else Color.White)
                CodeDataRow("ANDROID DEVICE ID", androidId)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "[INTEGRATED LENS ARRAY]",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                cameraSpecs.forEach { spec ->
                    Text(
                        text = " - $spec",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }

    // --- Dynamic Card Helper (Material You with Sound and Bounce tap animation) ---

    @Composable
    fun MaterialYouCard(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        content: @Composable () -> Unit
    ) {
        val context = LocalContext.current
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "scale"
        )
        
        Card(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(onClick) {
                    if (onClick != null) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                try {
                                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                    am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
                                } catch (e: Exception) {}
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = {
                                onClick()
                            }
                        )
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Subtle logo watermark at background
                Image(
                    painter = painterResource(id = R.drawable.cacun),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(24.dp)
                        .aspectRatio(2.8f)
                        .alpha(0.04f)
                )
                
                content()
            }
        }
    }

    @Composable
    fun CodeDataRow(label: String, value: String, valueColor: Color = Color(0xFF00FFCC)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    fun ProgressDataRow(
        label: String,
        percent: Int,
        detailsStr: String,
        barColor: Color
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$percent%",
                    color = barColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percent / 100f)
                        .background(barColor)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detailsStr,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }

    // Static variables helper to avoid re-querying in rendering loop
    companion object {
        private val memoryInfo = ActivityManager.MemoryInfo()
        private var totalStorageBytes: Long = 0
        private var availStorageBytes: Long = 0

        fun initConstants(context: Context) {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.getMemoryInfo(memoryInfo)
            totalStorageBytes = JavaHardwareScanner.getTotalStorage()
            availStorageBytes = JavaHardwareScanner.getAvailableStorage()
        }
    }

    @Composable
    fun MaterialYouSystemMonitorTheme(content: @Composable () -> Unit) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            initConstants(context)
        }

        val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val colorScheme = when {
            dynamicColor -> dynamicDarkColorScheme(context)
            else -> darkColorScheme(
                primary = Color(0xFF00FFCC),
                secondary = Color(0xFF00E5FF),
                background = Color(0xFF090A0E),
                surface = Color(0x0CFFFFFF)
            )
        }

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }

    // --- Private ControlItem Data Class ---
    private data class ControlItem(
        val label: String,
        val action: String,
        val logName: String,
        val icon: @Composable (Color) -> Unit
    )

    // --- New Diagnostics Widgets and Canvas Code Icons ---
    @Composable
    fun NetworkSpeedDiagnosticsWidget(
        downloadSpeed: Float,
        uploadSpeed: Float,
        downloadHistory: List<Float>,
        uploadHistory: List<Float>,
        simOperator: String,
        networkType: String,
        linkSpeed: Int
    ) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpeedIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "> NETWORK SPEED ANALYZER",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = networkType,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Traffic Graph
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFF040608))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    val w = size.width
                    val h = size.height
                    val maxVal = 25f // Max speed scale 25 Mbps
                    val points = downloadHistory.size
                    
                    if (points > 1) {
                        val xStep = w / 40f
                        val pathDown = Path()
                        val pathUp = Path()
                        
                        for (i in 0 until points) {
                            val x = i * xStep
                            val downY = h - ((downloadHistory.getOrNull(i) ?: 0f) / maxVal).coerceIn(0f, 1f) * h
                            val upY = h - ((uploadHistory.getOrNull(i) ?: 0f) / maxVal).coerceIn(0f, 1f) * h
                            
                            if (i == 0) {
                                pathDown.moveTo(x, downY)
                                pathUp.moveTo(x, upY)
                            } else {
                                pathDown.lineTo(x, downY)
                                pathUp.lineTo(x, upY)
                            }
                        }
                        
                        drawPath(pathDown, Color(0xFF00FFCC), style = Stroke(width = 2.dp.toPx()))
                        drawPath(pathUp, Color(0xFFBD00FF), style = Stroke(width = 2.dp.toPx()))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF00FFCC), RoundedCornerShape(3.dp)))
                        Text("DOWN: ${String.format(Locale.US, "%.2f", downloadSpeed)} Mbps", color = Color(0xFF00FFCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFBD00FF), RoundedCornerShape(3.dp)))
                        Text("UP: ${String.format(Locale.US, "%.2f", uploadSpeed)} Mbps", color = Color(0xFFBD00FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                CodeDataRow("CARRIER OPERATOR", simOperator, Color(0xFF00FFCC))
                CodeDataRow("TELEPHONY SIGNAL STRENGTH", "EXCELLENT (4/4 Bars)", Color(0xFF00E5FF))
                CodeDataRow("WIFI MAX LINK SPEED", if (linkSpeed > 0) "$linkSpeed Mbps" else "INACTIVE")
            }
        }
    }

    @Composable
    fun BluetoothDiagnosticsWidget(
        isEnabled: Boolean,
        devices: List<String>,
        history: List<Float>,
        onToggleBt: () -> Unit
    ) {
        MaterialYouCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BluetoothIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "> BLUETOOTH SIGNAL ANALYZER",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggleBt() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val waveColor = MaterialTheme.colorScheme.secondary
                // Bluetooth Signal wave
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color(0xFF040608))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f
                    val points = history.size

                    if (points > 1) {
                        val xStep = w / 40f
                        val path = Path()

                        for (i in 0 until points) {
                            val x = i * xStep
                            val amp = (history.getOrNull(i) ?: 0f) / 100f * midY
                            val y = midY + Math.sin(i * 0.4 + System.currentTimeMillis() * 0.005).toFloat() * amp
                            
                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        drawPath(path, waveColor, style = Stroke(width = 2.dp.toPx()))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "[ PAIRED AUDIO & HARDWARE DEVICES ]",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                devices.forEach { dev ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• $dev", color = MaterialTheme.colorScheme.onSurface, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                        Text("CONNECTED (RANGE: < 10m)", color = Color(0xFF00FFCC), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    // --- Custom Canvas Code Icons (Strictly No Emojis) ---
    @Composable
    fun WifiIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h - 4.dp.toPx())
            drawCircle(color = color, radius = 3.dp.toPx(), center = center)
            drawArc(
                color = color,
                startAngle = 220f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(center.x - 8.dp.toPx(), center.y - 8.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawArc(
                color = color,
                startAngle = 220f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(center.x - 14.dp.toPx(), center.y - 14.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(28.dp.toPx(), 28.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    @Composable
    fun BluetoothIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.3f, h * 0.3f)
                lineTo(w * 0.7f, h * 0.7f)
                lineTo(w * 0.5f, h * 0.9f)
                lineTo(w * 0.5f, h * 0.1f)
                lineTo(w * 0.7f, h * 0.3f)
                lineTo(w * 0.3f, h * 0.7f)
            }
            drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
        }
    }

    @Composable
    fun FlashlightIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            drawRect(
                color = color,
                topLeft = Offset(w * 0.4f, h * 0.4f),
                size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.5f),
                style = Stroke(width = 2.dp.toPx())
            )
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.3f, h * 0.1f)
                    lineTo(w * 0.7f, h * 0.1f)
                    lineTo(w * 0.6f, h * 0.4f)
                    lineTo(w * 0.4f, h * 0.4f)
                    close()
                },
                color = color,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    @Composable
    fun ShieldIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.5f, h * 0.1f)
                quadraticBezierTo(w * 0.8f, h * 0.15f, w * 0.9f, h * 0.2f)
                lineTo(w * 0.9f, h * 0.55f)
                quadraticBezierTo(w * 0.9f, h * 0.8f, w * 0.5f, h * 0.95f)
                quadraticBezierTo(w * 0.1f, h * 0.8f, w * 0.1f, h * 0.55f)
                lineTo(w * 0.1f, h * 0.2f)
                quadraticBezierTo(w * 0.2f, h * 0.15f, w * 0.5f, h * 0.1f)
                close()
            }
            drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
        }
    }

    @Composable
    fun BatteryIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.2f, h * 0.25f),
                size = androidx.compose.ui.geometry.Size(w * 0.55f, h * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawRect(
                color = color,
                topLeft = Offset(w * 0.77f, h * 0.4f),
                size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f)
            )
        }
    }

    @Composable
    fun DisplayIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.15f, h * 0.2f),
                size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawLine(
                color = color,
                start = Offset(w * 0.5f, h * 0.7f),
                end = Offset(w * 0.5f, h * 0.85f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = color,
                start = Offset(w * 0.35f, h * 0.85f),
                end = Offset(w * 0.65f, h * 0.85f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }

    @Composable
    fun SpeedIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.15f, h * 0.25f),
                size = androidx.compose.ui.geometry.Size(w * 0.7f, w * 0.7f),
                style = Stroke(width = 2.dp.toPx())
            )
            drawLine(
                color = color,
                start = Offset(w * 0.5f, h * 0.6f),
                end = Offset(w * 0.72f, h * 0.38f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }

    @Composable
    fun VolumeIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.2f, h * 0.4f)
                lineTo(w * 0.4f, h * 0.4f)
                lineTo(w * 0.6f, h * 0.2f)
                lineTo(w * 0.6f, h * 0.8f)
                lineTo(w * 0.4f, h * 0.6f)
                lineTo(w * 0.2f, h * 0.6f)
                close()
            }
            drawPath(path, color = color)
            drawArc(
                color = color,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.4f, h * 0.3f),
                size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.4f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    @Composable
    fun BrightnessIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            drawCircle(color, radius = 5.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
            for (i in 0 until 8) {
                val angle = i * Math.PI / 4.0
                val startX = center.x + Math.cos(angle).toFloat() * 7.dp.toPx()
                val startY = center.y + Math.sin(angle).toFloat() * 7.dp.toPx()
                val endX = center.x + Math.cos(angle).toFloat() * 10.dp.toPx()
                val endY = center.y + Math.sin(angle).toFloat() * 10.dp.toPx()
                drawLine(color, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2.dp.toPx())
            }
        }
    }

    @Composable
    fun SettingsIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            drawCircle(color, radius = 4.dp.toPx(), center = center, style = Stroke(width = 2.5.dp.toPx()))
            for (i in 0 until 6) {
                val angle = i * Math.PI / 3.0
                val x1 = center.x + Math.cos(angle).toFloat() * 5.dp.toPx()
                val y1 = center.y + Math.sin(angle).toFloat() * 5.dp.toPx()
                val x2 = center.x + Math.cos(angle).toFloat() * 9.dp.toPx()
                val y2 = center.y + Math.sin(angle).toFloat() * 9.dp.toPx()
                drawLine(color, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 3.dp.toPx())
            }
        }
    }

    @Composable
    fun CpuIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            drawRect(color, topLeft = Offset(w*0.25f, h*0.25f), size = androidx.compose.ui.geometry.Size(w*0.5f, h*0.5f), style = Stroke(width = 2.dp.toPx()))
            for (i in 0..3) {
                val offset = w * (0.33f + i * 0.11f)
                drawLine(color, start = Offset(offset, 0f), end = Offset(offset, h*0.25f), strokeWidth = 1.5.dp.toPx())
                drawLine(color, start = Offset(offset, h*0.75f), end = Offset(offset, h), strokeWidth = 1.5.dp.toPx())
                drawLine(color, start = Offset(0f, offset), end = Offset(w*0.25f, offset), strokeWidth = 1.5.dp.toPx())
                drawLine(color, start = Offset(w*0.75f, offset), end = Offset(w, offset), strokeWidth = 1.5.dp.toPx())
            }
        }
    }

    @Composable
    fun SignalIcon(bars: Int, color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val barWidth = 3.dp.toPx()
            val gap = 2.dp.toPx()
            for (i in 0 until 4) {
                val barHeight = h * (0.25f + i * 0.25f)
                val x = w * 0.15f + i * (barWidth + gap)
                val y = h - barHeight
                val active = i < bars
                drawRect(
                    color = if (active) color else color.copy(alpha = 0.2f),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
            }
        }
    }

    @Composable
    fun TimeIcon(color: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            drawCircle(color, radius = 9.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
            drawLine(color, start = center, end = Offset(center.x, center.y - 6.dp.toPx()), strokeWidth = 2.dp.toPx())
            drawLine(color, start = center, end = Offset(center.x + 4.dp.toPx(), center.y), strokeWidth = 2.dp.toPx())
        }
    }
}
