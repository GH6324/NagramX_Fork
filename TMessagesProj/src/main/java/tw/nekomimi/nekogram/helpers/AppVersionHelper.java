package tw.nekomimi.nekogram.helpers;

import android.content.Context;
import android.os.Build;

import org.telegram.messenger.BuildConfig;

import java.util.Locale;

/**
 * Helper class for retrieving application version information and architecture details.
 */
public class AppVersionHelper {

    /**
     * Detects the architecture of the current APK.
     * 
     * @param context Android context
     * @return Architecture string: "universal", "arm64-v8a", "armeabi-v7a", or the first supported ABI
     */
    public static String getArchitecture(Context context) {
        try {
            boolean isUniversal = false;
            try {
                String sourceDir = context.getApplicationInfo().sourceDir;
                if (sourceDir != null) {
                    java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(sourceDir);
                    boolean hasArm64 = false;
                    boolean hasArm32 = false;

                    java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        java.util.zip.ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith("lib/arm64-v8a/")) {
                            hasArm64 = true;
                        } else if (name.startsWith("lib/armeabi-v7a/")) {
                            hasArm32 = true;
                        }
                    }
                    zipFile.close();

                    isUniversal = hasArm64 && hasArm32;
                }
            } catch (Exception e) {
                // Ignore and continue with fallback
            }

            if (isUniversal) {
                return "universal";
            } else {
                String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
                if (nativeLibraryDir != null) {
                    if (nativeLibraryDir.contains("arm64")) {
                        return "arm64-v8a";
                    } else if (nativeLibraryDir.contains("arm")) {
                        return "armeabi-v7a";
                    } else {
                        return Build.SUPPORTED_ABIS[0].toLowerCase(Locale.ROOT);
                    }
                } else {
                    return Build.SUPPORTED_ABIS[0].toLowerCase(Locale.ROOT);
                }
            }
        } catch (Exception e) {
            return Build.SUPPORTED_ABIS[0].toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Gets the complete version string including version name, code, architecture, and build type.
     * 
     * @param context Android context
     * @return Formatted version string (e.g., "Nagram X v1.0.0(100) arm64-v8a release")
     */
    public static String getVersionString(Context context) {
        String arch = getArchitecture(context);
        return "Nagram X v" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ") " + arch + " " + BuildConfig.BUILD_TYPE;
    }
}
