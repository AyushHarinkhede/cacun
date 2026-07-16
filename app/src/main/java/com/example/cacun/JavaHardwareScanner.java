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
import android.media.AudioManager;
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

    // Helper structure to hold app security details
    public static class AppDetail {
        public String name;
        public String packageName;
        public String installSource;
        public int securityScore;
        public boolean isSystem;
    }

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

    // --- Root Detection (Warranty and Integrity) ---
    public static boolean checkRootAccess() {
        String[] paths = {
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        // Build tags check
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    // --- SIM Card Details ---
    @SuppressLint("MissingPermission")
    public static String getSimOperatorName(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String simOperator = tm.getSimOperatorName();
                if (simOperator != null && !simOperator.isEmpty()) {
                    return simOperator.toUpperCase();
                }
                String networkOperator = tm.getNetworkOperatorName();
                if (networkOperator != null && !networkOperator.isEmpty()) {
                    return networkOperator.toUpperCase();
                }
            }
        } catch (Exception e) {
            return "SIM ERROR / LOCKED";
        }
        return "NO SIM CARD";
    }

    // --- Installed Apps Fetcher ---
    public static List<AppDetail> getInstalledApps(Context context) {
        List<AppDetail> list = new ArrayList<>();
        try {
            android.content.pm.PackageManager pm = context.getPackageManager();
            List<android.content.pm.ApplicationInfo> apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA);
            for (android.content.pm.ApplicationInfo app : apps) {
                // Filter out standard launchers/system packages to show user-relevant apps
                AppDetail detail = new AppDetail();
                detail.name = app.loadLabel(pm).toString();
                detail.packageName = app.packageName;
                detail.isSystem = (app.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0;

                String installer = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        installer = pm.getInstallSourceInfo(app.packageName).getInstallingPackageName();
                    } else {
                        installer = pm.getInstallerPackageName(app.packageName);
                    }
                } catch (Exception e) {
                    // Ignore
                }

                if (installer == null || installer.isEmpty()) {
                    detail.installSource = "APK / SIDE-LOAD";
                    detail.securityScore = detail.isSystem ? 100 : 72;
                } else if (installer.contains("vending") || installer.contains("play")) {
                    detail.installSource = "PLAY STORE";
                    detail.securityScore = 98;
                } else if (installer.contains("amazon")) {
                    detail.installSource = "AMAZON APPSTORE";
                    detail.securityScore = 95;
                } else {
                    detail.installSource = "PACKAGE INSTALLER";
                    detail.securityScore = 88;
                }
                list.add(detail);
            }
        } catch (Exception e) {
            // Ignore
        }
        return list;
    }

    // --- Audio Volume Matrix ---
    public static int getStreamVolume(Context context, int streamType) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            return am.getStreamVolume(streamType);
        }
        return 0;
    }

    public static int getStreamMaxVolume(Context context, int streamType) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            return am.getStreamMaxVolume(streamType);
        }
        return 15;
    }

    public static void setStreamVolume(Context context, int streamType, int index) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.setStreamVolume(streamType, index, 0);
        }
    }
}
