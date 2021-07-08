package com.kwsilence.apkviewer.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Environment
import com.kwsilence.apkviewer.helper.ProviderHelper
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.util.APKFileUtils
import com.kwsilence.apkviewer.util.BitmapUtils
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ApplicationContentProvider : ContentProvider() {

  private lateinit var pm: PackageManager

  private val simpleFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

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
    pm.getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
      when (mode) {
        ProviderHelper.URI_USER_APPLICATIONS -> if (isSystem(info)) return@forEach
        ProviderHelper.URI_SYSTEM_APPLICATIONS -> if (!isSystem(info)) return@forEach
      }
      val name = info.loadLabel(pm).toString()
      val packageName = info.packageName
      val icon = BitmapUtils.drawableToBitmap(info.loadIcon(pm))
      list.add(Application(BitmapUtils.pngBitmapToByteArray(icon), name, packageName))
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
    apkPaths.forEach {
      val info = pm.getPackageArchiveInfo(it, PackageManager.GET_META_DATA)!!.applicationInfo
      info.sourceDir = it
      info.publicSourceDir = it

      val icon = BitmapUtils.drawableToBitmap(info.loadIcon(pm))
      val name = info.loadLabel(pm).toString()
      list.add(Application(BitmapUtils.pngBitmapToByteArray(icon), name, it))
    }
    when (sortOrder) {
      ProviderHelper.FIELD_NAME -> list.sortBy { it.name }
    }
    list.forEach { cursor.addRow(arrayOf(it.name, it.source, it.icon)) }
    return cursor
  }

  private fun getDetailInfo(source: String?): MatrixCursor? {
    if (source == null) return null
    val isApk = APKFileUtils.isAPK(source)
    val cursor = MatrixCursor(ProviderHelper.COLUMNS_DETAIL_INFO)

    val packageInfo =
      if (isApk) {
        pm.getPackageArchiveInfo(source, PackageManager.GET_META_DATA)
      } else {
        pm.getPackageInfo(source, PackageManager.GET_META_DATA)
      }
    val appInfo = packageInfo?.applicationInfo

    val pn = packageInfo?.packageName
    val ver = packageInfo?.versionName
    val apk = if (isApk) source else appInfo?.sourceDir
    val data = if (isApk) "None" else appInfo?.dataDir
    val install = if (isApk) "None" else
      simpleFormatter.format(Date(packageInfo!!.firstInstallTime)).toString()
    val update = if (isApk) "None" else
      simpleFormatter.format(Date(packageInfo!!.lastUpdateTime)).toString()
    val size = APKFileUtils.getStringSize(apk)

    var cert = ""
    ApkFile(apk).apkSingers.forEach {
      cert += "path: ${it.path}"
      it.certificateMetas.forEach { cm ->
        cert += "\nalgorithm: ${cm.signAlgorithm}"
        cert += "\nstart: ${simpleFormatter.format(cm.startDate)}"
        cert += "\nend: ${simpleFormatter.format(cm.endDate)}"
        cert += "\nmd5: ${cm.certMd5}"
      }
    }
    if (cert.isEmpty())
      cert = "None"

    cursor.addRow(arrayOf(pn, ver, apk, data, install, update, size, cert))
    return cursor
  }

  private fun getDetailManifest(source: String?): MatrixCursor? {
    if (source == null) return null
    val cursor = MatrixCursor(arrayOf(ProviderHelper.FIELD_MANIFEST))
    val file = ApkFile(APKFileUtils.getApkPath(pm, source))
    val manifest = file.manifestXml
    file.close()
    cursor.addRow(arrayOf(manifest))
    return cursor
  }

  private fun getDetailResource(source: String?): MatrixCursor? {
    if (source == null) return null
    val cursor = MatrixCursor(arrayOf(ProviderHelper.FIELD_RESOURCE))
    val list = ArrayList<String>()
    val zip = ZipFile(APKFileUtils.getApkPath(pm, source))
    zip.fileHeaders.forEach { header ->
      val name = header.fileName
      if (name.endsWith(".so"))
        list.add(name)
    }
    if (list.isEmpty())
      list.add("None")
    list.forEach { cursor.addRow(arrayOf(it)) }
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
