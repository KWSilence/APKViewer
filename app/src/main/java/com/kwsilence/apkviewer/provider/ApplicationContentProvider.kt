package com.kwsilence.apkviewer.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.kwsilence.apkviewer.helper.ProviderHelper
import com.kwsilence.apkviewer.helper.ProviderHelper.FIELD_ICON
import com.kwsilence.apkviewer.helper.ProviderHelper.FIELD_NAME
import com.kwsilence.apkviewer.helper.ProviderHelper.FIELD_SOURCE
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.util.BitmapUtils

class ApplicationContentProvider : ContentProvider() {

  private lateinit var pm: PackageManager

  companion object {
    private const val ALL_APPLICATION = 0
    private const val USER_APPLICATION = 1
    private const val SYSTEM_APPLICATION = 2
  }

  override fun onCreate(): Boolean {
    pm = context!!.packageManager
    return true
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?
  ): Cursor? {
    val cursor: MatrixCursor? = when (uri.encodedPath) {
      ProviderHelper.GET_ALL_APPLICATIONS -> getApplications(ALL_APPLICATION, sortOrder)
      ProviderHelper.GET_USER_APPLICATIONS -> getApplications(USER_APPLICATION, sortOrder)
      ProviderHelper.GET_SYSTEM_APPLICATIONS -> getApplications(SYSTEM_APPLICATION, sortOrder)
      ProviderHelper.GET_ALL_DISK_APK_FILES -> null
      else -> null
    }
    return cursor
  }

  private fun getApplications(mode: Int, sortOrder: String?): MatrixCursor {
    val cursor = MatrixCursor(arrayOf(FIELD_NAME, FIELD_SOURCE, FIELD_ICON))
    val list = ArrayList<Application>()
    pm.getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
      when (mode) {
        USER_APPLICATION -> if (isSystem(info)) return@forEach
        SYSTEM_APPLICATION -> if (!isSystem(info)) return@forEach
      }
      val name = info.loadLabel(pm).toString()
      val packageName = info.packageName
      val icon = BitmapUtils.drawableToBitmap(info.loadIcon(pm))
      list.add(Application(BitmapUtils.pngBitmapToByteArray(icon), name, packageName))
    }
    when (sortOrder) {
      FIELD_NAME -> list.sortBy { it.name }
      FIELD_SOURCE -> list.sortBy { it.source }
    }
    list.forEach { cursor.addRow(arrayOf(it.name, it.source, it.icon)) }
    return cursor
  }

  private fun isSystem(info: ApplicationInfo) = info.flags.and(ApplicationInfo.FLAG_SYSTEM) > 0

  override fun getType(uri: Uri): String? = null

  override fun insert(uri: Uri, values: ContentValues?): Uri? = null

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

  override fun update(
    uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
  ): Int = 0
}
