package com.hul.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import com.hul.R

/**
 * Created by Admin on 19-08-2016.
 */
object RuntimePermissionUtils {
    @JvmStatic
    fun checkPhoneStatePermission(context: Context): Boolean {
        val permission = Manifest.permission.READ_PHONE_STATE
        val res = context.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun checkLocationPermission(context: Context): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val res = context.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun showRationaleDialog(context: Context, permissionRetryListener: PermissionRetryListener?) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.permission_denied))
            .setMessage(context.getString(R.string.write_permission_denied_text))
            .setPositiveButton(R.string.retry) { dialog, which -> // continue with retry
                permissionRetryListener?.onPermissionRetry()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, which ->
                // do nothing
            }
            .show()
    }

    interface PermissionRetryListener {
        fun onPermissionRetry()
        fun onRedirect()
    }
}