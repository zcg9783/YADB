package com.ysbing.yadb;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {
    public static final String PACKAGE_NAME = "com.android.shell";
    public static final int USER_ID = 0;
    private static final String ARG_KEY_BOARD = "-keyboard";
    private static final String ARG_TOUCH = "-touch";
    private static final String ARG_LAYOUT = "-layout";
    private static final String ARG_SCREENSHOT = "-screenshot";
    private static final String ARG_READ_CLIPBOARD = "-readClipboard";
    private static final String ARG_DEVICE_INFO = "-deviceInfo";

    public static void main(String[] args) {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> System.out.println(t.getName() + ",UncaughtException:" + getStackTraceAsString(e)));
            Looper.prepareMainLooper();
            if (check(args[0])) {
                switch (args[0]) {
                    case ARG_KEY_BOARD:
                        Keyboard.run(args[1]);
                        break;
                    case ARG_TOUCH:
                        if (args.length == 4) {
                            Touch.run(Float.parseFloat(args[1]), Float.parseFloat(args[2]), Long.parseLong(args[3]));
                        } else {
                            Touch.run(Float.parseFloat(args[1]), Float.parseFloat(args[2]), -1L);
                        }
                        break;
                    case ARG_LAYOUT:
                        if (args.length == 2) {
                            Layout.run(args[1]);
                        } else {
                            Layout.run(null);
                        }
                        break;
                    case ARG_SCREENSHOT:
                        if (args.length == 2) {
                            Screenshot.run(args[1]);
                        } else {
                            Screenshot.run(null);
                        }
                        break;
                    case ARG_READ_CLIPBOARD:
                        Keyboard.readClipboard();
                        break;
                    case ARG_DEVICE_INFO:
                        getDeviceInfo();
                        break;
                    default:
                        break;
                }
            } else {
                System.out.println("Invalid argument");
            }
        } catch (Throwable e) {
            System.out.println("MainException:" + getStackTraceAsString(e));
        }
    }

    private static boolean check(String arg) {
        return arg.equals(ARG_KEY_BOARD) || arg.equals(ARG_TOUCH) || 
               arg.equals(ARG_LAYOUT) || arg.equals(ARG_SCREENSHOT) || 
               arg.equals(ARG_READ_CLIPBOARD) || arg.equals(ARG_DEVICE_INFO);
    }

    private static String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }

    private static void getDeviceInfo() {
        try {
            Context context = ActivityThread.currentApplication().getApplicationContext();
            System.out.println("\n===== Basic Info =====");
            System.out.println("Model: " + Build.MODEL);
            System.out.println("Manufacturer: " + Build.MANUFACTURER);
            System.out.println("Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
            System.out.println("Language: " + Locale.getDefault().getLanguage());
            System.out.println("Android ID: " + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            System.out.println("\n===== Display Info =====");
            System.out.println("Resolution: " + metrics.widthPixels + "x" + metrics.heightPixels);
            System.out.println("DPI: " + metrics.densityDpi);
            System.out.println("Density: " + metrics.density);

            System.out.println("\n===== Network Info =====");
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (nc != null) {
                    System.out.println("Network Type: " + getNetworkType(nc));
                    System.out.println("Download Speed: " + nc.getLinkDownstreamBandwidthKbps() + " Kbps");
                    System.out.println("Upload Speed: " + nc.getLinkUpstreamBandwidthKbps() + " Kbps");
                }
                
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                System.out.println("WiFi SSID: " + wifiInfo.getSSID().replace("\"", ""));
                System.out.println("WiFi BSSID: " + wifiInfo.getBSSID());
                System.out.println("WiFi Signal: " + wifiInfo.getRssi() + " dBm");
                
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                System.out.println("SIM Operator: " + tm.getSimOperatorName());
                System.out.println("Network Operator: " + tm.getNetworkOperatorName());
                System.out.println("Network Type: " + getNetworkTypeName(tm.getDataNetworkType()));
            } catch (SecurityException e) {
                System.out.println("Network info error: " + e.getMessage());
            }

            System.out.println("\n===== Battery Info =====");
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            System.out.println("Capacity: " + bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) + "%");
            System.out.println("Charging Status: " + getChargingStatus(bm));
            System.out.println("Health: " + bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_HEALTH));
            System.out.println("Technology: " + bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_TECHNOLOGY));
            System.out.println("Temperature: " + bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE) / 10.0 + "°C");

            System.out.println("\n===== Sensor Info =====");
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sensor : sensors) {
                System.out.println("Sensor: " + sensor.getName() + " (Type: " + sensor.getType() + ", Vendor: " + sensor.getVendor() + ")");
            }

            System.out.println("\n===== Camera Info =====");
            try {
                CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                for (String cameraId : cameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    System.out.println("Camera " + cameraId + ": " + 
                                     (lensFacing == CameraCharacteristics.LENS_FACING_BACK ? "Back" : "Front"));
                    System.out.println("  Resolutions: " + Arrays.toString(
                            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                            .getOutputSizes(android.media.ImageFormat.JPEG)));
                }
            } catch (Exception e) {
                System.out.println("Camera info error: " + e.getMessage());
            }

            System.out.println("\n===== Storage Info =====");
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long blockSize = statFs.getBlockSizeLong();
            long totalBlocks = statFs.getBlockCountLong();
            long availableBlocks = statFs.getAvailableBlocksLong();
            System.out.println("Total: " + formatFileSize(totalBlocks * blockSize));
            System.out.println("Available: " + formatFileSize(availableBlocks * blockSize));
            
            System.out.println("\n===== App Sandbox =====");
            System.out.println("Files Dir: " + context.getFilesDir().getAbsolutePath());
            System.out.println("Cache Dir: " + context.getCacheDir().getAbsolutePath());
            System.out.println("External State: " + Environment.getExternalStorageState());
        } catch (Throwable e) {
            System.out.println("DeviceInfoError:" + getStackTraceAsString(e));
        }
    }

    private static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private static String getNetworkTypeName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_NR: return "5G";
            default: return "Unknown (" + type + ")";
        }
    }

    private static String getNetworkType(NetworkCapabilities nc) {
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "WiFi";
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Cellular";
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) return "Bluetooth";
        return "Unknown";
    }

    private static String getChargingStatus(BatteryManager bm) {
        switch (bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)) {
            case BatteryManager.BATTERY_STATUS_CHARGING: return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING: return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL: return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING: return "Not Charging";
            default: return "Unknown";
        }
    }
}