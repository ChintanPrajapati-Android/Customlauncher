package com.example.customlauncher.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri


fun Context.uninstallApp(packageName: String) {
    val intent = Intent(Intent.ACTION_DELETE)
    intent.data = Uri.parse("package:${packageName}")
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    this.startActivity(intent)
}


fun Context.launchApp(packageName: String) {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    this.startActivity(launchIntent)
}


@SuppressLint("PackageManagerGetSignatures")
fun Context.isSystemApp(packageName: String): Boolean {
    return try {
        val targetPkgInfo: PackageInfo? = packageManager.getPackageInfo(
                packageName, PackageManager.GET_SIGNATURES)
        val sys: PackageInfo = packageManager.getPackageInfo(
                "android", PackageManager.GET_SIGNATURES)
        targetPkgInfo?.signatures != null && (sys.signatures[0] == targetPkgInfo.signatures[0])
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}