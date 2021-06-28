package com.kwsilence.apkviewer.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionManager {
  companion object {
    const val READ_PERMISSION = 1
    const val WRITE_PERMISSION = 2

    private fun getPermission(permission: Int): String {
      return when (permission) {
        READ_PERMISSION -> Manifest.permission.READ_EXTERNAL_STORAGE
        WRITE_PERMISSION -> Manifest.permission.WRITE_EXTERNAL_STORAGE
        else -> ""
      }
    }

    private fun havePermission(activity: Activity, permission: Int) =
      ActivityCompat.checkSelfPermission(
        activity,
        getPermission(permission)
      ) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(activity: Activity, permission: Int): Boolean {
      val isRequested = !havePermission(activity, permission)
      if (isRequested) {
        val permissions = arrayOf(getPermission(permission))
        ActivityCompat.requestPermissions(activity, permissions, permission)
      }
      return isRequested
    }
  }
}