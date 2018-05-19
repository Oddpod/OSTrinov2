package com.odd.ostrinov2.tools

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity

fun requestSystemAlertPermission(context: Activity?, requestCode: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        return
    val packageName = context!!.packageName
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivityForResult(intent, requestCode)
}

fun isSystemAlertPermissionGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

fun launchOverlayPermissionNotGrantedDialog(mainActivity: MainActivity) {
    val builder = AlertDialog.Builder(mainActivity)
    builder.setMessage("Ooops, this app needs permission to draw over other apps in " +
            "order to play youtube videos")
            .setCancelable(false)
            .setNegativeButton("I don't want to play anything", null)
            .setPositiveButton("Give permission") { _, _ ->
                requestSystemAlertPermission(
                        mainActivity, 3)
            }
    val alert = builder.create()
    alert.show()

}

fun checkPermission(mainActivity: MainActivity, callback: Runnable) {
    if (ContextCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

        // No explanation needed, we can request the permission.
        MainActivity.setPermissionCallback(callback)
        ActivityCompat.requestPermissions(mainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Constants.MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE)
    } else {
        callback.run()
    }
}

fun launchAppSettingsIntent(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}