package com.example.cacun

import android.Manifest
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
import android.os.*
import android.widget.Toast
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
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

    // Audio & Haptic
    private var audioManager: AudioManager? = null

    // Sensor State (Compose backed)
    private var accelX by mutableFloatStateOf(0f)
    private var accelY by mutableFloatStateOf(0f)
    private var accelZ by mutableFloatStateOf(0f)

    private var gyroX by mutableFloatStateOf(0f)
    private var gyroY by mutableFloatStateOf(0f)
    private var gyroZ by mutableFloatStateOf(0f)

    // Battery Telemetry
    private var batteryPct by mutableIntStateOf(0)
    private var batteryVoltage by mutableFloatStateOf(0f)
    private var batteryCurrent by mutableFloatStateOf(0f) // In mA
    private var batteryTemp by mutableFloatStateOf(0f)
    private var batteryHealthStr by mutableStateOf("UNKNOWN")
    private var chargingPlugStr by mutableStateOf("DISCONNECTED")
    private var isChargingState by mutableStateOf(false)

    // Hardware Controls States
    private var isFlashlightOn by mutableStateOf(false)
    private var currentVolumePercent by mutableFloatStateOf(0.5f)
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
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
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

                addLog("[BAT] Telemetry packet received. Level: $batteryPct% | Temp: $batteryTemp°C")
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            currentVolumePercent = if (max > 0) cur.toFloat() / max else 0.5f
        }

        // Setup Sensor Services
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
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

        setContent {
            GlassSystemMonitorTheme {
                MainDashboardScreen()
            }
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
    private fun adjustVolume(percentage: Float) {
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newVol = (percentage * max).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
            currentVolumePercent = percentage
            addLog("[HW] Audio Stream Volume modified to: ${(percentage * 100).toInt()}%")
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
                        // Morse code pulse
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

    // --- Jetpack Compose UI Views ---

    @Composable
    fun MainDashboardScreen() {
        val context = LocalContext.current

        // 1. Boot Console Animation sequence on first load
        var isBooting by remember { mutableStateOf(true) }
        val bootLogs = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
            val bootSequence = listOf(
                "Initializing Cacun Kernel Loader v2.0...",
                "Mounting secure core hardware detectors... OK",
                "Establishing telemetry socket connections... OK",
                "Scanning integrated circuits and sensors...",
                " - Accelerometer: Bind success.",
                " - Gyroscope: Bind success.",
                "Querying network capabilities... OK",
                "Analyzing storage arrays... Blocks verified.",
                "Boot Diagnostics Completed. Launching Telemetry HUD..."
            )
            for (log in bootSequence) {
                delay(320)
                bootLogs.add("[BOOT] $log")
            }
            delay(400)
            isBooting = false
        }

        // Animated fading for boot screen
        AnimatedVisibility(
            visible = isBooting,
            exit = fadeOut(animationSpec = tween(600)) + slideOutVertically(targetOffsetY = { -it })
        ) {
            // Hacker Terminal style booting view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF040508))
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cacun),
                        contentDescription = "Cacun Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .aspectRatio(2.8f)
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "CACUN SYSTEM OS",
                        color = Color(0xFF00FFCC),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Color(0xFF0A0D14))
                            .border(1.dp, Color(0x3300FFCC))
                            .padding(12.dp)
                    ) {
                        bootLogs.forEach { log ->
                            Text(
                                text = log,
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).size(30.dp),
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

            // RAM status
            val memoryInfo = JavaHardwareScanner.getMemoryInfo(context)
            val totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f)
            val availRamGb = memoryInfo.availMem / (1024f * 1024f * 1024f)
            val usedRamGb = totalRamGb - availRamGb
            val usedRamPercent = if (memoryInfo.totalMem > 0) {
                ((memoryInfo.totalMem - memoryInfo.availMem) * 100f / memoryInfo.totalMem).toInt()
            } else 0

            // Storage status
            val totalStorageBytes = JavaHardwareScanner.getTotalStorage()
            val availStorageBytes = JavaHardwareScanner.getAvailableStorage()
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

            // Hardware details
            val cameraSpecs = remember { JavaHardwareScanner.getCameraCharacteristics(context) }
            val imei = remember { JavaHardwareScanner.attemptImeiRead(context) }
            val androidId = remember { JavaHardwareScanner.getAndroidId(context) }
            val nfcStatus = JavaHardwareScanner.getNfcStatus(context)

            // Battery calculations
            val batteryCurrentA = batteryCurrent / 1000f
            val batteryPowerW = abs(batteryVoltage * batteryCurrentA)

            // Infinite rotation or glow animations for active states
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

            // Layout settings
            val scrollState = rememberScrollState()

            // Drawing grid backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090A0E))
                    .drawBehind {
                        // Drawing cyber magenta and cyan light splashes
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x1FBD00FF), Color.Transparent),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.2f
                            ),
                            radius = size.width * 1.2f,
                            center = Offset(0f, 0f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x1A00FFCC), Color.Transparent),
                                center = Offset(size.width, size.height * 0.7f),
                                radius = size.width
                            ),
                            radius = size.width,
                            center = Offset(size.width, size.height * 0.7f)
                        )

                        // Draw Grid Lines
                        val gridSpace = 32.dp.toPx()
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
                // Adaptive layout: detects screen width. Switches columns for phone/tablets
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp)
                ) {
                    val isTablet = maxWidth >= 600.dp

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // APP LOGO HEADER ROW
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
                                        text = "PLATFORM INTERFACE v2.0",
                                        color = Color(0x66FFFFFF),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Glowing HUD Status
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

                        // Scrollable section, switches to 2-column or 1-column responsive grid
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
                                        LiveOscilloscopePlot()
                                        BatteryInfusionModule(batteryPowerW)
                                        InteractiveHardwareControls()
                                    }

                                    // Column 2
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        SystemDiagnosticConsole()
                                        VolatileStorageSectors(usedRamPercent, usedRamGb, totalRamGb, usedStoragePercent, usedStorageGb, totalStorageGb)
                                        HardwareICDirectory(manufacturer, model, androidVersion, sdkVersion, widthPx, heightPx, densityDpi, refreshRate, nfcStatus, imei, androidId, cameraSpecs)
                                        NetworkDiagnosticsScanner(networkType, linkSpeed)
                                        GyroscopeDiagnosticsCard()
                                    }
                                }
                            } else {
                                // Phone 1-Column Portrait Layout
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    PerformanceSpeedController()
                                    LiveOscilloscopePlot()
                                    BatteryInfusionModule(batteryPowerW)
                                    InteractiveHardwareControls()
                                    SystemDiagnosticConsole()
                                    VolatileStorageSectors(usedRamPercent, usedRamGb, totalRamGb, usedStoragePercent, usedStorageGb, totalStorageGb)
                                    HardwareICDirectory(manufacturer, model, androidVersion, sdkVersion, widthPx, heightPx, densityDpi, refreshRate, nfcStatus, imei, androidId, cameraSpecs)
                                    NetworkDiagnosticsScanner(networkType, linkSpeed)
                                    GyroscopeDiagnosticsCard()
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Image(
                                painter = painterResource(id = R.drawable.cacun),
                                contentDescription = "Cacun Watermark",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .height(26.dp)
                                    .aspectRatio(2.8f)
                                    .alpha(0.2f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                    text = "> KERNEL DRAIN CONTROLLER",
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
    fun LiveOscilloscopePlot() {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> ACCELERATION VECTOR OSCILLOSCOPE",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Hz: ${if(currentMode.value == TelemetryMode.FAST) "60" else if(currentMode.value == TelemetryMode.STANDARD) "20" else "1"}",
                        color = Color(0x66FFFFFF),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
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
                    Text("X: ${String.format(Locale.US, "%+.3f", accelX)} m/s²", color = Color(0xFF00FFCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Y: ${String.format(Locale.US, "%+.3f", accelY)} m/s²", color = Color(0xFFBD00FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Z: ${String.format(Locale.US, "%+.3f", accelZ)} m/s²", color = Color(0xFFFFB300), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    @Composable
    fun BatteryInfusionModule(batteryPowerW: Float) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> SYSTEM ENERGY & FEED VOLTAGE",
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
                            .size(75.dp)
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
                        DataRow("HEALTH STATUS", batteryHealthStr, if(batteryHealthStr == "GOOD") Color(0xFF00FFCC) else Color(0xFFFFB300))
                        DataRow("LINK VOLTAGE", "${String.format(Locale.US, "%.3f", batteryVoltage)} V", Color.White)
                        DataRow("LINK CURRENT", "${String.format(Locale.US, "%.1f", batteryCurrent)} mA", Color.White)
                        DataRow("NET POWER", "${String.format(Locale.US, "%.3f", batteryPowerW)} W", Color(0xFF00FFCC))
                        DataRow("TEMP LEVEL", "$batteryTemp °C", Color.White)
                        DataRow("CHARGE INLET", chargingPlugStr, Color(0xFF00E5FF))
                    }
                }
            }
        }
    }

    @Composable
    fun InteractiveHardwareControls() {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "> HARDWARE INTERCEPT INTERFACES",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Flashlight Torch Toggle Button
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

                // 2. Volume Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SYSTEM MUSIC STREAM VOL", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("${(currentVolumePercent * 100).toInt()}%", color = Color(0xFF00FFCC), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = currentVolumePercent,
                        onValueChange = { adjustVolume(it) },
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

                // 3. Screen Brightness slider
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

                // 4. Haptic generator triggers
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
        manufacturer: String, model: String, androidVersion: String, sdkVersion: Int,
        widthPx: Int, heightPx: Int, densityDpi: Int, refreshRate: Float,
        nfcStatus: String, imei: String, androidId: String, cameraSpecs: List<String>
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
                DataRow("HARDWARE BOARD", Build.HARDWARE.uppercase(), Color.White)
                DataRow("ANDROID VERSION", "RELEASE $androidVersion (API $sdkVersion)", Color.White)
                DataRow("NFC ANTENNA LINK", nfcStatus, if (nfcStatus == "ACTIVE") Color(0xFF00FFCC) else Color(0xFFFFB300))
                DataRow("DISPLAY RESOLUTION", "${widthPx}x${heightPx} @ ${refreshRate.toInt()}Hz (${densityDpi} dpi)", Color.White)

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
                .background(Color(0x0CFFFFFF)) // Translucent background
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF), // Glossy top highlights
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

    @Composable
    fun GlassSystemMonitorTheme(content: @Composable () -> Unit) {
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
