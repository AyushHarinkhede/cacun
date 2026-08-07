package com.example.cacun

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class FloatingHudService : Service(), SensorEventListener {

    private lateinit var windowManager: WindowManager
    private var hudView: View? = null
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null

    // Telemetry Values
    private var currentLux: Float = 0f
    private var batteryPercent: Int = 0
    private var batteryVoltage: Float = 0f
    private var batteryCurrent: Float = 0f
    private var batteryPowerW: Float = 0f

    // UI Elements
    private lateinit var textRefresh: TextView
    private lateinit var textLux: TextView
    private lateinit var textBattery: TextView
    private lateinit var extraDetailsLayout: LinearLayout
    private lateinit var btnToggleMore: TextView
    private lateinit var btnClose: TextView

    private var isExpanded = false

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateRealtimeStats()
            updateHandler.postDelayed(this, 1000)
        }
    }

    private var isCharging: Boolean = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                batteryPercent = if (level != -1 && scale != -1) {
                    (level * 100) / scale
                } else 0

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                val voltageMv = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                batteryVoltage = voltageMv / 1000f

                val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val currentMicroAmp = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                batteryCurrent = Math.abs(currentMicroAmp / 1000f) // convert microAmp to milliAmp

                batteryPowerW = (batteryVoltage * batteryCurrent) / 1000f
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Setup Sensors & Receivers
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }

        // Build Programmatic Glassmorphic Overlay Layout
        createFloatingHud()

        // Start Periodic Updater
        updateHandler.post(updateRunnable)
    }

    private fun createFloatingHud() {
        val context = this

        // Root Layout
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#E00C0E14")) // Dark semitransparent
                cornerRadius = 16f
                setStroke(2, Color.parseColor("#4400FFCC")) // Cyan neon border
            }
            background = shape
            setPadding(24, 20, 24, 20)
        }

        // Header Row (Refresh Rate + Toggle + Close)
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        textRefresh = TextView(context).apply {
            text = "REFRESH: -- Hz"
            setTextColor(Color.parseColor("#00FFCC"))
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        btnToggleMore = TextView(context).apply {
            text = "[ + ]"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 12f
            setPadding(10, 0, 10, 0)
            typeface = android.graphics.Typeface.MONOSPACE
            setOnClickListener {
                isExpanded = !isExpanded
                extraDetailsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                text = if (isExpanded) "[ - ]" else "[ + ]"
            }
        }

        btnClose = TextView(context).apply {
            text = " [X] "
            setTextColor(Color.parseColor("#FF3B30"))
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            setOnClickListener {
                stopSelf()
            }
        }

        headerRow.addView(textRefresh)
        headerRow.addView(btnToggleMore)
        headerRow.addView(btnClose)
        root.addView(headerRow)

        // Expanded/Extra Details Layout
        extraDetailsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, 10, 0, 0)
        }

        textLux = TextView(context).apply {
            text = "LUX: -- lm"
            setTextColor(Color.parseColor("#FFB300"))
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
        }

        textBattery = TextView(context).apply {
            text = "BATT: --% (--W)"
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
        }

        extraDetailsLayout.addView(textLux)
        extraDetailsLayout.addView(textBattery)
        root.addView(extraDetailsLayout)

        hudView = root

        // Layout Parameters for Overlay
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // Drag to Move Touch Handler
        hudView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.let {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = it.rawX
                            initialTouchY = it.rawY
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (it.rawX - initialTouchX).toInt()
                            params.y = initialY + (it.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(hudView, params)
                            return true
                        }
                    }
                }
                return false
            }
        })

        windowManager.addView(hudView, params)
    }

    private fun updateRealtimeStats() {
        // 1. Fetch Dynamic Screen Refresh Rate
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.display
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        val rate = display?.refreshRate ?: 60f
        textRefresh.text = String.format(Locale.US, "REFRESH: %.1f Hz", rate)

        // 2. Fetch Lux
        textLux.text = String.format(Locale.US, "LIGHT: %.1f LUX", currentLux)

        // 3. Fetch Charging Wattage / Battery
        textBattery.text = String.format(Locale.US, "BATT: %d%% (%.2f W)", batteryPercent, batteryPowerW)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_LIGHT) {
                currentLux = it.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacks(updateRunnable)
        sensorManager?.unregisterListener(this)
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {}
        hudView?.let {
            windowManager.removeView(it)
        }
    }
}
