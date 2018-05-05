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
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity

fun requestSystemAlertPermission(context: Activity?, requestCode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return
        val packageName = context!!.packageName
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivityForResult(intent, requestCode)
    }

    fun isSystemAlertPermissionGranted(context: Context): Boolean {
        val result = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
        return result
    }

fun checkPermission(mainActivity: MainActivity) {
        if (ContextCompat.checkSelfPermission(mainActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mainActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(mainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Constants.MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE)
            // MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }
}