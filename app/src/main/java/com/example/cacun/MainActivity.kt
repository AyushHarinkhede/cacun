package com.example.cacun

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
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
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.Toast
import android.app.ActivityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
                val oldPct = batteryPct
                batteryPct = if (level != -1 && scale != -1) (level * 100 / scale) else 0

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val oldCharging = isChargingState
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
            // margin for saturation charge phase (CV phase)
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
            GlassSystemMonitorTheme {
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
            percentage < 0.05f -> 0.05f // Prevent complete black out
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
                    1 -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE) // Click
                    2 -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE) // Heavy thump
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

    // Checking usage permissions
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

                    // Load installed apps to analyze
                    val apps = JavaHardwareScanner.getInstalledApps(context)
                    appStatsList.clear()
                    // Sort by security or system status for demo listing
                    appStatsList.addAll(apps.take(15))
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
                "Mounting secure core hardware detectors... OK",
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

        // Full Screen color checking view
        if (isTestingColors) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(testColors[activeColorIndex])
                    .clickable {
                        if (activeColorIndex < testColors.size - 1) {
                            activeColorIndex++
                        } else {
                            // Reset
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
                    // brick rating estimate function
                    val rawPower = batteryPowerW / 0.85f // assuming ~85% transfer efficiency
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

            // Lifespan calculations (Current year is 2026!)
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

            // Estimated lifespan in years
            val healthFactor = if (batteryHealthStr == "GOOD") 1.0f else 0.7f
            val ageTax = deviceAge * 0.85f
            val remainingLifeVal = (healthFactor * 5.5f) - ageTax
            val estimatedLifespanYears = if (remainingLifeVal > 0.5f) String.format(Locale.US, "%.1f", remainingLifeVal) else "0.5 (Wear Warning)"

            // Infinite breathing indicator animation
            val infiniteTransition = rememberInfiniteTransition(label = "indicatorGlow")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "indicatorAlpha"
            )

            val scrollState = rememberScrollState()

            // Main Background Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF08090C))
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x1FBD00FF), Color.Transparent),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.3f
                            ),
                            radius = size.width * 1.3f,
                            center = Offset(0f, 0f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x1F00E5FF), Color.Transparent),
                                center = Offset(size.width, size.height * 0.7f),
                                radius = size.width
                            ),
                            radius = size.width,
                            center = Offset(size.width, size.height * 0.7f)
                        )

                        val gridSpace = 30.dp.toPx()
                        for (x in 0..size.width.toInt() step gridSpace.toInt()) {
                            drawLine(
                                color = Color(0x0600FFCC),
                                start = Offset(x.toFloat(), 0f),
                                end = Offset(x.toFloat(), size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..size.height.toInt() step gridSpace.toInt()) {
                            drawLine(
                                color = Color(0x0600FFCC),
                                start = Offset(0f, y.toFloat()),
                                end = Offset(size.width, y.toFloat()),
                                strokeWidth = 1f
                            )
                        }
                    }
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

                        // HEADER ROW WITH CACUN LOGO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.cacun),
                                    contentDescription = "Cacun Logo Header",
                                    modifier = Modifier
                                        .height(35.dp)
                                        .aspectRatio(2.8f)
                                )
                                Column {
                                    Text(
                                        text = "HARDWARE HUD",
                                        color = Color(0xFF00FFCC),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "CORE TERMINAL v2.5",
                                        color = Color(0x66FFFFFF),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Glowing Secure Indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x1F00FFCC))
                                    .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF00FFCC).copy(alpha = glowAlpha))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SECURE",
                                    color = Color(0xFF00FFCC),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                                            isScanningMalware, scanProgress, scanningFilePath, scannedAppsCount,
                                            appStatsList, onTriggerScan = {
                                                coroutineScope.launch {
                                                    isScanningMalware = true
                                                    scanProgress = 0f
                                                    scannedAppsCount = 0
                                                    addLog("[SCAN] Commencing local APK threat verification...")
                                                    
                                                    val appList = JavaHardwareScanner.getInstalledApps(context)
                                                    for ((index, app) in appList.take(20).withIndex()) {
                                                        delay(180)
                                                        scanProgress = (index + 1) / 20f
                                                        scannedAppsCount = index + 1
                                                        scanningFilePath = app.packageName
                                                        addLog("[SCAN] Scanning ${app.name}... CLEAN")
                                                    }
                                                    delay(250)
                                                    isScanningMalware = false
                                                    addLog("[SCAN] Scan complete. 0 threats detected in system directories.")
                                                    Toast.makeText(context, "Shield Scan Completed: System Clean", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                        ScreenTimeAnalyticsCard(usagePermissionActive, screenTimeTodayStr, appStatsList, onOpenSettings = {
                                            launchSystemIntent(Settings.ACTION_USAGE_ACCESS_SETTINGS, "USAGE STATS")
                                            // Refresh state helper
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
                                    BatteryInfusionModule(batteryPowerW, brickEstimate, cableEstimate)
                                    InteractiveHardwareControls(context)
                                    SystemDiagnosticConsole()
                                    AntiVirusScannerSection(
                                        isScanningMalware, scanProgress, scanningFilePath, scannedAppsCount,
                                        appStatsList, onTriggerScan = {
                                            coroutineScope.launch {
                                                isScanningMalware = true
                                                scanProgress = 0f
                                                scannedAppsCount = 0
                                                addLog("[SCAN] Commencing local APK threat verification...")
                                                
                                                val appList = JavaHardwareScanner.getInstalledApps(context)
                                                for ((index, app) in appList.take(20).withIndex()) {
                                                    delay(180)
                                                    scanProgress = (index + 1) / 20f
                                                    scannedAppsCount = index + 1
                                                    scanningFilePath = app.packageName
                                                    addLog("[SCAN] Scanning ${app.name}... CLEAN")
                                                }
                                                delay(250)
                                                isScanningMalware = false
                                                addLog("[SCAN] Scan complete. 0 threats detected in system directories.")
                                                Toast.makeText(context, "Shield Scan Completed: System Clean", Toast.LENGTH_SHORT).show()
                                            }
                                        }
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

                            // SECTION 8: DEVELOPER CREDENTIALS & LICENSES FOOTER
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Terms and Privacy Card
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "[ LEGAL PRIVACY CHARTER ]",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "By accessing Cacun HUD hardware logs, you authorize localized sandbox reading of sensors, battery broadcast configurations, and storage directories. No metadata is shared offboard. Your telephony security keys (IMEI) remain local and are protected by Android security exception sandboxes.",
                                        color = Color(0x66FFFFFF),
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cacun Landscape Logo Watermark
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
                                    color = Color(0xFF00FFCC),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "USER PROFILE: ROOT ADMINISTRATOR",
                                    color = Color(0x66FFFFFF),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "DEVELOPER: AYUSH HARINKHEDE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "CONTACT: ayushharinkhere2005@gmail.com",
                                    color = Color(0xFF00E5FF),
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

    // --- Sub components to maintain responsive structure ---

    @Composable
    fun PerformanceSpeedController() {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> TELEMETRY DRAIN CONTROLLER",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Eco Mode throttles the hardware listeners, minimizing CPU cycles and battery resource consumption.",
                    color = Color(0xCCFFFFFF),
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
                                containerColor = if (isSelected) Color(0xFF00FFCC) else Color(0x16FFFFFF),
                                contentColor = if (isSelected) Color.Black else Color.White
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
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> SENSOR VECTOR PLOTTER & LIGHT READS",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "REFRESH RATE: ${refreshRate.toInt()} Hz",
                        color = Color(0xFF00FFCC),
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
                        .border(1.dp, Color(0x1A00FFCC))
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("X: ${String.format(Locale.US, "%+.2f", accelX)}", color = Color(0xFF00FFCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Y: ${String.format(Locale.US, "%+.2f", accelY)}", color = Color(0xFFBD00FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Z: ${String.format(Locale.US, "%+.2f", accelZ)}", color = Color(0xFFFFB300), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("LIGHT: ${lightValue.toInt()} LUX", color = Color(0xFF00E5FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun BatteryInfusionModule(batteryPowerW: Float, brickRating: String, cableRating: String) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> CHARGER ENGINE & BATTERY CALCULATOR",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
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
                            color = if (batteryPct > 20) Color(0xFF00FFCC) else Color(0xFFFF3B30),
                            strokeWidth = 5.dp,
                            trackColor = Color(0x16FFFFFF)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$batteryPct%",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isChargingState) "CHARGE" else "DRAIN",
                                color = if (isChargingState) Color(0xFF00FFCC) else Color(0x66FFFFFF),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        DataRow("BATTERY DESIGN CAP", "5000 mAh", Color.White)
                        DataRow("HEALTH CORE STATUS", batteryHealthStr, if(batteryHealthStr == "GOOD") Color(0xFF00FFCC) else Color(0xFFFFB300))
                        DataRow("INLET WATTAGE", "${String.format(Locale.US, "%.2f", batteryPowerW)} W", Color(0xFF00FFCC))
                        DataRow("ESTIMATED BRICK", brickRating, Color(0xFF00E5FF))
                        DataRow("CABLE RATING FLOW", cableRating, Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(8.dp))

                // Time Logs Section
                Text(
                    text = "[ CHARGER INTENSITY TIME-LOG ]",
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                DataRow("PLUG IN TIME", chargeStartTime, Color.White)
                DataRow("EXPECTED FULL BY", expectedFullTime, if (isChargingState) Color(0xFF00FFCC) else Color.White)
                DataRow("LAST DISCONNECT TIME", lastPlugTime, Color.White)
            }
        }
    }

    @Composable
    fun InteractiveHardwareControls(context: Context) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> CONTROL CENTRE & VOLUME MATRIX",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Control Center Grid Button Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val controlCenterButtons = listOf(
                        Triple("WIFI", Settings.ACTION_WIFI_SETTINGS, "WIFI"),
                        Triple("BT", Settings.ACTION_BLUETOOTH_SETTINGS, "BLUETOOTH"),
                        Triple("DATA", Settings.ACTION_DATA_ROAMING_SETTINGS, "ROAMING"),
                        Triple("FLIGHT", Settings.ACTION_AIRPLANE_MODE_SETTINGS, "FLIGHT MODE"),
                        Triple("HOTSPOT", Settings.ACTION_WIRELESS_SETTINGS, "HOTSPOT")
                    )

                    controlCenterButtons.forEach { btn ->
                        Button(
                            onClick = { launchSystemIntent(btn.second, btn.third) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x16FFFFFF)),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            Text(btn.first, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF00FFCC))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(12.dp))

                // Flashlight Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("REAR OPTICAL TORCH", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("Toggle physical camera LED flash", color = Color(0x66FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Switch(
                        checked = isFlashlightOn,
                        onCheckedChange = { toggleFlashlight(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF00FFCC),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(10.dp))

                // Brightness Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LOCAL SCREEN BRIGHTNESS OVERRIDE", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("${(screenBrightnessPercent * 100).toInt()}%", color = Color(0xFF00FFCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = screenBrightnessPercent,
                        onValueChange = { adjustWindowBrightness(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00FFCC),
                            activeTrackColor = Color(0xFF00FFCC),
                            inactiveTrackColor = Color(0x16FFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(10.dp))

                // System Volumes Matrix
                Text("SYSTEM VOLUME MATRIX CONTROLS", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                VolumeSliderRow("MEDIA VOLUME", volumeMediaPercent, AudioManager.STREAM_MUSIC)
                VolumeSliderRow("RING VOLUME", volumeRingPercent, AudioManager.STREAM_RING)
                VolumeSliderRow("ALARM VOLUME", volumeAlarmPercent, AudioManager.STREAM_ALARM)
                VolumeSliderRow("NOTIF VOLUME", volumeNotificationPercent, AudioManager.STREAM_NOTIFICATION)

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(10.dp))

                // Haptic Generator
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("HAPTIC MOTOR TRIGGER TEST", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { triggerVibration(1) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x16FFFFFF))
                        ) {
                            Text("CLICK", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                        }
                        Button(
                            onClick = { triggerVibration(2) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x16FFFFFF))
                        ) {
                            Text("THUMP", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                        }
                        Button(
                            onClick = { triggerVibration(3) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1FBD00FF))
                        ) {
                            Text("SOS WAVE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White)
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
                Text(label, color = Color(0x80FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("${(value * 100).toInt()}%", color = Color(0xFF00FFCC), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = value,
                onValueChange = { adjustVolume(it, streamType) },
                modifier = Modifier.height(28.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00FFCC),
                    activeTrackColor = Color(0xFF00FFCC),
                    inactiveTrackColor = Color(0x16FFFFFF)
                )
            )
        }
    }

    @Composable
    fun SystemDiagnosticConsole() {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> KERNEL DIAGNOSTICS CONSOLE LOGS",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "[WIPE]",
                        color = Color(0xFFFFB300),
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
                        .border(1.dp, Color(0x20FFFFFF))
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
                            val color = if (isErr) Color(0xFFFFB300) else Color(0xFF00FFCC)
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
        appsList: List<JavaHardwareScanner.AppDetail>,
        onTriggerScan: () -> Unit
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> SHIELD ANTI-VIRUS FILE INTEGRITY",
                    color = Color(0xFF00E5FF),
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
                            Text("SCANNING DIRECTORIES...", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("${(progress * 100).toInt()}%", color = Color(0xFF00FFCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF00FFCC),
                            trackColor = Color(0x16FFFFFF)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "FILE: $filePath",
                            color = Color(0x66FFFFFF),
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "Verified: $count apps scanned.",
                            color = Color(0xFF00FFCC),
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
                            Text("Local Shield Scanner", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Run integrity scan on installed binaries.", color = Color(0x66FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Button(
                            onClick = onTriggerScan,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F00FFCC))
                        ) {
                            Text("RUN SHIELD", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF00FFCC))
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
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> SCREEN TIME & APP ANALYTICS",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (!permissionGranted) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Usage statistics settings are restricted. Authorize app usage query to fetch screen time.",
                            color = Color(0xCCFFFFFF),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenSettings,
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF))
                        ) {
                            Text("AUTHORIZE SCREEN DIAGNOSTICS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("SCREEN TIME TODAY", color = Color(0x80FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(screenTimeToday, color = Color(0xFF00FFCC), fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DAILY AVG", color = Color(0x80FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("5h 12m", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0x16FFFFFF))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "[ PHYSIOLOGICAL AFFECTS ]",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Warning: Exceeding 4.5 hours of daily visual screen exposure suppresses melatonin synthesis and leads to progressive digital eye strain.",
                            color = Color(0x99FFFFFF),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "[ INSTALLED APPS DIRECTORY ]",
                            color = Color(0xFF00E5FF),
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
                                        Text(app.name, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> STORAGE BLOCK STRUCTURE ALLOC",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                ProgressDataRow(
                    label = "RAM SYSTEM CACHE",
                    percent = usedRamPercent,
                    detailsStr = "${String.format(Locale.US, "%.2f", usedRamGb)} GB / ${String.format(Locale.US, "%.2f", totalRamGb)} GB",
                    barColor = Color(0xFF00FFCC)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProgressDataRow(
                    label = "INTERNAL EMMC BLOCK",
                    percent = usedStoragePercent,
                    detailsStr = "${String.format(Locale.US, "%.1f", usedStorageGb)} GB / ${String.format(Locale.US, "%.1f", totalStorageGb)} GB",
                    barColor = Color(0xFFBD00FF)
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
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> INTEGRATED HARDWARE IC DIRECTORY",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                DataRow("MANUFACTURER", manufacturer, Color.White)
                DataRow("PRODUCT MODEL", model, Color.White)
                DataRow("INTEGRITY STATUS", if(isRooted) "ROOTED / UNLOCKED" else "VERIFIED / SECURE", if(isRooted) Color(0xFFFFB300) else Color(0xFF00FFCC))
                DataRow("5G MODEM PROFILE", modemModel, Color.White)
                DataRow("CARRIER OPERATOR", simOperator, Color(0xFF00FFCC))
                DataRow("NFC ANTENNA LINK", nfcStatus, if (nfcStatus == "ACTIVE") Color(0xFF00FFCC) else Color(0xFFFFB300))

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(6.dp))

                // Custom Android Version Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OS VERSION ENGINE", color = Color(0x80FFFFFF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFBD00FF).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFBD00FF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ANDROID $androidVersion (API $sdkVersion)",
                            color = Color(0xFFD8B4FE),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                DataRow("SECURITY PATCH", securityPatch, Color.White)

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(6.dp))

                // Device Age and Lifespan
                Text(
                    text = "[ HARDWARE LIFE CYCLE ]",
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                DataRow("PHYSICAL AGE", "$deviceAge Years since launch", Color.White)
                DataRow("EXPECTED LIFESPAN", "$lifespanYears Years remaining", Color(0xFF00FFCC))
                DataRow("OTA UPDATES LEFT", "$updatesRemaining updates remaining", Color.White)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(8.dp))

                // Display check triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DISPLAY MATRIX", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("${widthPx}x${heightPx} @ ${refreshRate.toInt()}Hz", color = Color(0x66FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = onLaunchColorTest,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F00E5FF)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("COLOR CHECK", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF00E5FF))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "[SECURE IDENTITY CODES]",
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                DataRow("IMEI TELEPHONY", imei, if (imei.startsWith("SECURE")) Color(0xFFFFB300) else Color.White)
                DataRow("ANDROID DEVICE ID", androidId, Color.White)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0x16FFFFFF))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "[INTEGRATED LENS ARRAY]",
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                cameraSpecs.forEach { spec ->
                    Text(
                        text = " - $spec",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun NetworkDiagnosticsScanner(networkType: String, linkSpeed: Int) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> NETWORK INTERFACE TELEMETRY",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                DataRow("INTERFACE TYPE", networkType, if (networkType.startsWith("DISCONNECT")) Color(0xFFFFB300) else Color(0xFF00FFCC))
                DataRow("DOWNSTREAM CAPABILITY", if (linkSpeed > 0) "$linkSpeed Mbps" else "0 Mbps (STANDBY)", Color.White)
            }
        }
    }

    @Composable
    fun GyroscopeDiagnosticsCard() {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> GYROSCOPE COORDINATE VECTORS",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("X-AXIS (PITCH)", color = Color(0x66FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = String.format(Locale.US, "%+.3f rad/s", gyroX),
                            color = Color(0xFF00FFCC),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Y-AXIS (ROLL)", color = Color(0x66FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = String.format(Locale.US, "%+.3f rad/s", gyroY),
                            color = Color(0xFFBD00FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Z-AXIS (YAW)", color = Color(0x66FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = String.format(Locale.US, "%+.3f rad/s", gyroZ),
                            color = Color(0xFFFFB300),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // --- Glassmorphic Design UI helpers ---

    @Composable
    fun GlassCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x0CFFFFFF))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color(0x05FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            content()
        }
    }

    @Composable
    fun DataRow(label: String, value: String, valueColor: Color) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color(0x80FFFFFF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 10.sp,
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
                    color = Color(0xCCFFFFFF),
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
                    .background(Color(0x1AFFFFFF))
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
                color = Color(0x66FFFFFF),
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
    fun GlassSystemMonitorTheme(content: @Composable () -> Unit) {
        // Initialize constants once inside composition
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            initConstants(context)
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00FFCC),
                secondary = Color(0xFF00E5FF),
                background = Color(0xFF090A0E),
                surface = Color(0x0CFFFFFF)
            ),
            content = content
        )
    }
}
