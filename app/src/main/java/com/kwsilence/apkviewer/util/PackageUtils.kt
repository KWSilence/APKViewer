package com.kwsilence.apkviewer.util

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.model.ApplicationDetail
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object PackageUtils {

  const val ALL_APP = 0
  const val USER_APP = 1
  const val SYSTEM_APP = 2
  const val APK_FILE = 3

  @SuppressLint("ConstantLocale")
  private val simpleFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

  fun sourceToApplication(pm: PackageManager, source: String?, mode: Int): Application? {
    if (source == null) return null
    val isApk = APKFileUtils.isAPK(source)
    if ((mode != APK_FILE) and isApk) return null
    if (isApk) {
      pm.getPackageArchiveInfo(source, PackageManager.GET_META_DATA)!!.applicationInfo.apply {
        sourceDir = source
        publicSourceDir = source
      }
    } else {
      val res = pm.getApplicationInfo(source, PackageManager.GET_META_DATA)
      when (mode) {
        ALL_APP -> res
        USER_APP -> if (isSystem(res)) null else res
        SYSTEM_APP -> if (!isSystem(res)) null else res
        else -> null
      }
    }?.let {
      val icon = BitmapUtils.drawableToBitmap(it.loadIcon(pm))
      val name = it.loadLabel(pm).toString()
      return Application(BitmapUtils.pngBitmapToByteArray(icon), name, source)
    }
    return null
  }

  fun sourceToApplicationDetail(pm: PackageManager, source: String?): ApplicationDetail? {
    if (source == null) return null
    val isApk = APKFileUtils.isAPK(source)

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

    return ApplicationDetail(pn, ver, size, apk, data, install, update, cert)
  }

  fun sourceToManifest(pm: PackageManager, source: String?): String? {
    if (source == null) return null
    val file = ApkFile(APKFileUtils.getApkPath(pm, source))
    val manifest = file.manifestXml
    file.close()
    return manifest
  }

  fun sourceToResource(pm: PackageManager, source: String?): ArrayList<String>? {
    if (source == null) return null
    val list = ArrayList<String>()
    val zip = ZipFile(APKFileUtils.getApkPath(pm, source))
    zip.fileHeaders.forEach { header ->
      val name = header.fileName
      if (name.endsWith(".so"))
        list.add(name)
    }
    if (list.isEmpty())
      list.add("None")
    return list
  }

  private fun isSystem(info: ApplicationInfo) = info.flags.and(ApplicationInfo.FLAG_SYSTEM) > 0
}