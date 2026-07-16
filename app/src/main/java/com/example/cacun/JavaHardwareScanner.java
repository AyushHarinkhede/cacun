package com.example.cacun;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Java Hardware and Telemetry Integration.
 * Provides high-performance, low-level integration with the Android framework.
 * Optimized for minimal CPU cycle consumption and battery conservation.
 */
public class JavaHardwareScanner {

    // --- RAM Telemetry ---
    public static ActivityManager.MemoryInfo getMemoryInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo);
        }
        return memoryInfo;
    }

    // --- Internal Storage Telemetry ---
    public static long getTotalStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } catch (Exception e) {
            return 0;
        }
    }

    public static long getAvailableStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        } catch (Exception e) {
            return 0;
        }
    }

    // --- Camera Details ---
    @SuppressLint("DefaultLocale")
    public static List<String> getCameraCharacteristics(Context context) {
        List<String> cameraSpecs = new ArrayList<>();
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (manager != null) {
                String[] ids = manager.getCameraIdList();
                for (String id : ids) {
                    CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                    Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                    String facingStr = "UNKNOWN";
                    if (facing != null) {
                        if (facing == CameraCharacteristics.LENS_FACING_FRONT) facingStr = "FRONT";
                        else if (facing == CameraCharacteristics.LENS_FACING_BACK) facingStr = "BACK";
                        else if (facing == CameraCharacteristics.LENS_FACING_EXTERNAL) facingStr = "EXTERNAL";
                    }
                    
                    android.util.Size pixelSize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
                    float megapixels = 0f;
                    if (pixelSize != null) {
                        megapixels = (pixelSize.getWidth() * pixelSize.getHeight()) / 1_000_000f;
                    }
                    cameraSpecs.add(String.format("CAM%s [%s]: %.1f MP (%dx%d)", id, facingStr, megapixels, 
                            pixelSize != null ? pixelSize.getWidth() : 0, 
                            pixelSize != null ? pixelSize.getHeight() : 0));
                }
            }
        } catch (Exception e) {
            cameraSpecs.add("Camera Access Locked: " + e.getMessage());
        }
        
        // Legacy fallback count if Camera2 Manager fails
        if (cameraSpecs.isEmpty()) {
            int cameras = Camera.getNumberOfCameras();
            cameraSpecs.add("Legacy Cam Detect: " + cameras + " available.");
        }
        return cameraSpecs;
    }

    // --- Display Telemetry ---
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            display.getRealMetrics(metrics);
        }
        return metrics;
    }

    public static float getRefreshRate(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            return wm.getDefaultDisplay().getRefreshRate();
        }
        return 60.0f;
    }

    // --- Telephony & Identifier Telemetry ---
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String attemptImeiRead(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return tm.getImei(0);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return tm.getImei();
                } else {
                    return tm.getDeviceId();
                }
            }
        } catch (SecurityException e) {
            return "SECURE_RESTRICTION (API 29+)";
        } catch (Exception e) {
            return "UNKNOWN_TELEPHONY_ERR";
        }
        return "UNKNOWN";
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // --- NFC Telemetry ---
    public static String getNfcStatus(Context context) {
        try {
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
            if (adapter == null) {
                return "UNSUPPORTED";
            }
            return adapter.isEnabled() ? "ACTIVE" : "STANDBY";
        } catch (Exception e) {
            return "UNSUPPORTED";
        }
    }

    // --- Network Telemetry ---
    @SuppressLint("MissingPermission")
    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network activeNetwork = cm.getActiveNetwork();
                    NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                    if (caps != null) {
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return "WIFI LINK";
                        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return "CELLULAR DATA";
                        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return "ETHERNET LAN";
                        }
                    }
                } else {
                    @SuppressWarnings("deprecation")
                    NetworkInfo info = cm.getActiveNetworkInfo();
                    if (info != null && info.isConnected()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI) return "WIFI LINK";
                        if (info.getType() == ConnectivityManager.TYPE_MOBILE) return "CELLULAR DATA";
                    }
                }
            }
        } catch (Exception e) {
            return "ERROR_CHECKING";
        }
        return "DISCONNECTED";
    }

    @SuppressLint("MissingPermission")
    public static int getLinkSpeedMbps(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network activeNetwork = cm.getActiveNetwork();
                    NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                    if (caps != null) {
                        // link downstream speed in Kbps, convert to Mbps
                        return caps.getLinkDownstreamBandwidthKbps() / 1000;
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    // --- Sensors List ---
    public static List<String> getAvailableSensors(Context context) {
        List<String> list = new ArrayList<>();
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sm != null) {
            List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
            for (Sensor s : sensors) {
                list.add(s.getName() + " (" + s.getVendor() + ")");
            }
        }
        return list;
    }
}
