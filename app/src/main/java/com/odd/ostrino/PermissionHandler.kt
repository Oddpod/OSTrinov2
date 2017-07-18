package com.odd.ostrino

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

    fun requestSystemAlertPermission(context: Activity?, requestCode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return
        val packageName = context!!.packageName
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
        if (context != null)
            context.startActivityForResult(intent, requestCode)
        else
            context.startActivityForResult(intent, requestCode)
    }

    //@TargetApi(23)
    fun isSystemAlertPermissionGranted(context: Context): Boolean {
        val result = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
        return result
    }