package com.tianshang.health.feature.steps.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import com.tianshang.health.core.common.R

enum class OemType(
    val manufacturer: String,
    @StringRes val displayNameResId: Int,
    @StringRes val batteryGuideResId: Int,
    @StringRes val autoStartGuideResId: Int,
    @StringRes val lockGuideResId: Int
) {
    XIAOMI(
        manufacturer = "Xiaomi",
        displayNameResId = R.string.oem_xiaomi_display,
        batteryGuideResId = R.string.oem_xiaomi_battery,
        autoStartGuideResId = R.string.oem_xiaomi_autostart,
        lockGuideResId = R.string.oem_xiaomi_lock
    ),
    HUAWEI(
        manufacturer = "Huawei",
        displayNameResId = R.string.oem_huawei_display,
        batteryGuideResId = R.string.oem_huawei_battery,
        autoStartGuideResId = R.string.oem_huawei_autostart,
        lockGuideResId = R.string.oem_huawei_lock
    ),
    HONOR(
        manufacturer = "Honor",
        displayNameResId = R.string.oem_honor_display,
        batteryGuideResId = R.string.oem_honor_battery,
        autoStartGuideResId = R.string.oem_honor_autostart,
        lockGuideResId = R.string.oem_honor_lock
    ),
    OPPO(
        manufacturer = "OPPO",
        displayNameResId = R.string.oem_oppo_display,
        batteryGuideResId = R.string.oem_oppo_battery,
        autoStartGuideResId = R.string.oem_oppo_autostart,
        lockGuideResId = R.string.oem_oppo_lock
    ),
    VIVO(
        manufacturer = "vivo",
        displayNameResId = R.string.oem_vivo_display,
        batteryGuideResId = R.string.oem_vivo_battery,
        autoStartGuideResId = R.string.oem_vivo_autostart,
        lockGuideResId = R.string.oem_vivo_lock
    ),
    SAMSUNG(
        manufacturer = "Samsung",
        displayNameResId = R.string.oem_samsung_display,
        batteryGuideResId = R.string.oem_samsung_battery,
        autoStartGuideResId = R.string.oem_samsung_autostart,
        lockGuideResId = R.string.oem_samsung_lock
    ),
    ONEPLUS(
        manufacturer = "OnePlus",
        displayNameResId = R.string.oem_oneplus_display,
        batteryGuideResId = R.string.oem_oneplus_battery,
        autoStartGuideResId = R.string.oem_oneplus_autostart,
        lockGuideResId = R.string.oem_oneplus_lock
    ),
    REALME(
        manufacturer = "realme",
        displayNameResId = R.string.oem_realme_display,
        batteryGuideResId = R.string.oem_realme_battery,
        autoStartGuideResId = R.string.oem_realme_autostart,
        lockGuideResId = R.string.oem_realme_lock
    ),
    OTHER(
        manufacturer = "",
        displayNameResId = R.string.oem_other_display,
        batteryGuideResId = R.string.oem_other_battery,
        autoStartGuideResId = R.string.oem_other_autostart,
        lockGuideResId = R.string.oem_other_lock
    );

    companion object {
        fun detect(): OemType {
            val manufacturer = Build.MANUFACTURER.lowercase()
            val brand = Build.BRAND.lowercase()
            val fingerprint = Build.FINGERPRINT.lowercase()

            return when {
                manufacturer.contains("xiaomi") || brand.contains("xiaomi") ||
                    manufacturer.contains("redmi") || brand.contains("redmi") ||
                    manufacturer.contains("poco") || getSystemProperty("ro.miui.ui.version.name") != null -> XIAOMI

                manufacturer.contains("honor") || brand.contains("honor") ||
                    fingerprint.contains("honor") -> HONOR

                manufacturer.contains("huawei") || brand.contains("huawei") -> HUAWEI

                manufacturer.contains("oppo") || brand.contains("oppo") ||
                    manufacturer.contains("realme") || brand.contains("realme") -> {
                    if (manufacturer.contains("realme") || brand.contains("realme")) {
                        REALME
                    } else {
                        OPPO
                    }
                }

                manufacturer.contains("vivo") || brand.contains("vivo") ||
                    manufacturer.contains("iqoo") -> VIVO

                manufacturer.contains("samsung") || brand.contains("samsung") -> SAMSUNG

                manufacturer.contains("oneplus") || brand.contains("oneplus") -> ONEPLUS

                else -> OTHER
            }
        }

        private fun getSystemProperty(name: String): String? {
            return try {
                val cl = Class.forName("android.os.SystemProperties")
                val method = cl.getMethod("get", String::class.java)
                val value = method.invoke(null, name) as? String
                if (value.isNullOrEmpty()) null else value
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
                null
            }
        }
    }

    fun openBatterySettings(context: Context) {
        val success = when (this) {
            XIAOMI -> tryOpen(
                context,
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
            HUAWEI, HONOR -> tryOpen(
                context,
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectedActivity"
            )
            OPPO, REALME -> tryOpen(
                context,
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
            VIVO -> tryOpen(
                context,
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BootStartActivity"
            )
            SAMSUNG -> tryOpen(context, "com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
            ONEPLUS -> tryOpen(context, "com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity") ||
                tryOpen(context, "com.oplus.security", "com.oplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            else -> false
        }
        if (!success) openAppSettings(context)
    }

    fun openAutoStartSettings(context: Context) {
        val success = when (this) {
            XIAOMI -> tryOpen(context, "com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity") ||
                tryOpenIntent(context, Intent("miui.intent.action.OP_AUTO_START"))
            HUAWEI, HONOR -> tryOpen(context, "com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity") ||
                tryOpen(context, "com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
            OPPO, REALME -> tryOpen(context, "com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity") ||
                tryOpen(context, "com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")
            VIVO -> tryOpen(context, "com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BootStartActivity") ||
                tryOpen(context, "com.iqoo.secure", "com.iqoo.secure.permission.PermissionManagerActivity")
            SAMSUNG -> tryOpen(context, "com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
            ONEPLUS -> tryOpen(context, "com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity") ||
                tryOpen(context, "com.oplus.security", "com.oplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            else -> false
        }
        if (!success) openAppSettings(context)
    }

    private fun tryOpen(context: Context, pkg: String, cls: String): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(pkg, cls)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
            false
        }
    }

    private fun tryOpenIntent(context: Context, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
            false
        }
    }

    private fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
        }
    }
}
