package com.kwsilence.apkviewer.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Environment
import com.kwsilence.apkviewer.helper.ProviderHelper
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.util.APKFileUtils
import com.kwsilence.apkviewer.util.PackageUtils

class ApplicationContentProvider : ContentProvider() {

  private lateinit var pm: PackageManager

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
    val arg = selectionArgs?.get(0)

    val cursor: MatrixCursor? = when (val mode = ProviderHelper.uriMatcher.match(uri)) {
      ProviderHelper.URI_ALL_APPLICATIONS or ProviderHelper.URI_USER_APPLICATIONS or ProviderHelper.URI_SYSTEM_APPLICATIONS ->
        getApplications(mode, sortOrder)
      ProviderHelper.URI_DISK_APKS -> getDiskAPKFiles(sortOrder)
      ProviderHelper.URI_DETAIL_INFO -> getDetailInfo(arg)
      ProviderHelper.URI_DETAIL_MANIFEST -> getDetailManifest(arg)
      ProviderHelper.URI_DETAIL_RESOURCE -> getDetailResource(arg)
      else -> null
    }
    return cursor
  }

  private fun getApplications(mode: Int, sortOrder: String?): MatrixCursor {
    val cursor = MatrixCursor(ProviderHelper.COLUMNS_APPLICATION)
    val list = ArrayList<Application>()
    pm.getInstalledPackages(PackageManager.GET_META_DATA).forEach { info ->
      val selectionMode = when (mode) {
        ProviderHelper.URI_USER_APPLICATIONS -> PackageUtils.USER_APP
        ProviderHelper.URI_SYSTEM_APPLICATIONS -> PackageUtils.SYSTEM_APP
        else -> PackageUtils.ALL_APP
      }
      PackageUtils.sourceToApplication(pm, info.packageName, selectionMode)?.let { list.add(it) }
    }
    when (sortOrder) {
      ProviderHelper.FIELD_NAME -> list.sortBy { it.name }
      ProviderHelper.FIELD_SOURCE -> list.sortBy { it.source }
    }
    list.forEach { cursor.addRow(arrayOf(it.name, it.source, it.icon)) }
    return cursor
  }

  private fun getDiskAPKFiles(sortOrder: String?): MatrixCursor {
    val cursor = MatrixCursor(ProviderHelper.COLUMNS_APPLICATION)
    val list = ArrayList<Application>()

    @Suppress("DEPRECATION")
    val path = Environment.getExternalStorageDirectory()
    val apkPaths = APKFileUtils.findAPK(path)
    apkPaths.forEach { source ->
      PackageUtils.sourceToApplication(pm, source, PackageUtils.ALL_APP)?.let { list.add(it) }
    }
    when (sortOrder) {
      ProviderHelper.FIELD_NAME -> list.sortBy { it.name }
    }
    list.forEach { cursor.addRow(arrayOf(it.name, it.source, it.icon)) }
    return cursor
  }

  private fun getDetailInfo(source: String?): MatrixCursor? {
    val cursor = MatrixCursor(ProviderHelper.COLUMNS_DETAIL_INFO)
    val detail = PackageUtils.sourceToApplicationDetail(pm, source) ?: return null
    cursor.addRow(
      arrayOf(
        detail.packageName,
        detail.version,
        detail.apkFile,
        detail.dataPath,
        detail.installDate,
        detail.updateDate,
        detail.size,
        detail.certificate
      )
    )
    return cursor
  }

  private fun getDetailManifest(source: String?): MatrixCursor? {
    val manifest = PackageUtils.sourceToManifest(pm, source) ?: return null
    val cursor = MatrixCursor(arrayOf(ProviderHelper.FIELD_MANIFEST))
    cursor.addRow(arrayOf(manifest))
    return cursor
  }

  private fun getDetailResource(source: String?): MatrixCursor? {
    val list = PackageUtils.sourceToResource(pm, source) ?: return null
    val cursor = MatrixCursor(arrayOf(ProviderHelper.FIELD_RESOURCE))
    list.forEach { cursor.addRow(arrayOf(it)) }
    return cursor
  }

  override fun getType(uri: Uri): String? = null

  override fun insert(uri: Uri, values: ContentValues?): Uri? = null

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

  override fun update(
    uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
  ): Int = 0
}
