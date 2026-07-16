#!/usr/bin/env python3
"""
Cacun Offboard Hardware Telemetry Exporter
A python utility to remotely extract hardware logs, sensors state, and power metrics
via Android Debug Bridge (ADB) connection. Shows cross-platform hardware integration.
"""

import os
import sys
import subprocess
import json
import time

def run_adb_command(args):
    """Executes an adb command and returns the stdout."""
    try:
        cmd = ["adb"] + args
        result = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=5)
        if result.returncode != 0:
            return f"ERROR: {result.stderr.strip()}"
        return result.stdout.strip()
    except FileNotFoundError:
        return "ERROR: 'adb' executable not found. Please install Android Platform Tools and add to PATH."
    except Exception as e:
        return f"ERROR: {str(e)}"

def get_connected_devices():
    """Queries adb for connected devices."""
    raw = run_adb_command(["devices"])
    if "ERROR" in raw:
        return []
    
    lines = raw.split("\n")[1:]
    devices = []
    for line in lines:
        if line.strip():
            parts = line.split()
            if len(parts) >= 2 and parts[1] == "device":
                devices.append(parts[0])
    return devices

def query_telemetry(device_id):
    """Queries hardware information directly via shell commands for low-power diagnostics."""
    print(f"\n[+] Extracting low-level telemetry from device ID: {device_id}...")
    
    # 1. Device Info
    model = run_adb_command(["-s", device_id, "shell", "getprop", "ro.product.model"])
    brand = run_adb_command(["-s", device_id, "shell", "getprop", "ro.product.brand"])
    sdk = run_adb_command(["-s", device_id, "shell", "getprop", "ro.build.version.sdk"])
    
    # 2. Battery & Charging Info
    battery_dump = run_adb_command(["-s", device_id, "shell", "dumpsys", "battery"])
    battery_lines = battery_dump.split("\n")
    battery_data = {}
    for line in battery_lines:
        if ":" in line:
            k, v = line.split(":", 1)
            battery_data[k.strip()] = v.strip()

    # 3. Audio & Brightness Controls telemetry
    brightness = run_adb_command(["-s", device_id, "shell", "settings", "get", "system", "screen_brightness"])
    
    # Try to search for music stream volume in dumpsys audio
    audio_dump = run_adb_command(["-s", device_id, "shell", "dumpsys", "audio"])
    volume_str = "Unknown"
    for line in audio_dump.split("\n"):
        if "STREAM_MUSIC" in line or "- streamType: 3" in line:
            volume_str = line.strip()
            break

    # 4. Wifi & Network status
    wifi_status = run_adb_command(["-s", device_id, "shell", "cmd", "wifi", "status"])
    wifi_state = "Offline/Disconnected"
    if "Wifi is enabled" in wifi_status or "connected" in wifi_status.lower():
        wifi_state = "Connected / Active"

    # Calculate battery estimates
    voltage_mv = int(battery_data.get("voltage", 0))
    voltage_v = voltage_mv / 1000.0
    level = battery_data.get("level", "Unknown")
    status = battery_data.get("status", "Unknown")
    health = battery_data.get("health", "Unknown")
    
    print("\n" + "=" * 55)
    print("        CACUN TELEMETRY INTERACTION ENGINE (PYTHON PORT)")
    print("=" * 55)
    print(f"TELEMETRY DEVICE : {brand.upper()} {model}")
    print(f"FIRMWARE VERSION : ANDROID SDK {sdk}")
    print(f"WIFI TRANSPORT   : {wifi_state}")
    print(f"BATTERY POWER    : {level}% [{status}] - Health: {health}")
    print(f"FEED VOLTAGE     : {voltage_v} V")
    
    # Query current now
    if "current_now" in battery_dump or True:
        current_now = run_adb_command(["-s", device_id, "shell", "cat", "/sys/class/power_supply/battery/current_now"])
        if current_now and not "ERROR" in current_now:
            try:
                current_ma = abs(int(current_now)) / 1000.0
                power_w = (voltage_v * current_ma) / 1000.0
                print(f"CURRENT DRAIN    : {current_ma:.2f} mA")
                print(f"NET DRAIN WATT   : {power_w:.4f} W")
            except:
                pass

    print(f"SCREEN BRIGHTNESS: {brightness} (Scale 0-255)")
    print(f"STREAM VOL BLOCK : {volume_str[:70]}...")
            
    print("\n--- ACTIVE STORAGE SECTOR INFO ---")
    df_dump = run_adb_command(["-s", device_id, "shell", "df", "/data"])
    print(df_dump)
    
    print("\n--- INTEGRATED SENSORS DIRECTORY ---")
    sensors = run_adb_command(["-s", device_id, "shell", "sensorservice", "list"])
    for s in sensors.split("\n")[:10]:
        if s.strip():
            print(f" - {s.strip().split('|')[0]}")
    if len(sensors.split("\n")) > 10:
        print(f" ... and {len(sensors.split('\n')) - 10} more sensors.")
    print("=" * 55)

def main():
    print("[*] Initializing Cacun Offboard Diagnostics Engine...")
    devices = get_connected_devices()
    if not devices:
        print("[!] No Android devices found via ADB. Ensure USB Debugging is active and authorized.")
        sys.exit(1)
        
    print(f"[*] Found {len(devices)} device(s) online.")
    query_telemetry(devices[0])

if __name__ == "__main__":
    main()
