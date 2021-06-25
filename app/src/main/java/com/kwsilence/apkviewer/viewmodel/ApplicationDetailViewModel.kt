package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.model.ApplicationDetail
import io.reactivex.rxjava3.core.Single
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

class ApplicationDetailViewModel(private val pm: PackageManager) : ViewModel() {

  private val simpleFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

  fun oAppHead(source: String): Single<Application> =
    Single.create { sub ->
      val applicationInfo =
        if (isAPK(source)) {
          pm.getPackageArchiveInfo(source, PackageManager.GET_META_DATA)!!.applicationInfo
        } else {
          pm.getApplicationInfo(source, PackageManager.GET_META_DATA)
        }
      val icon = applicationInfo.loadIcon(pm)
      val name = applicationInfo.loadLabel(pm).toString()
      sub.onSuccess(Application(icon, name, source))
    }


  fun oAppDetail(source: String): Single<ApplicationDetail> =
    Single.create { sub ->
      val packageInfo =
        if (isAPK(source)) {
          pm.getPackageArchiveInfo(source, PackageManager.GET_META_DATA)
        } else {
          pm.getPackageInfo(source, PackageManager.GET_META_DATA)
        }
      val appInfo = packageInfo?.applicationInfo

      val pn = packageInfo?.packageName
      val ver = packageInfo?.versionName
      val apk = if (isAPK(source)) source else appInfo?.sourceDir
      val data = if (isAPK(source)) "None" else appInfo?.dataDir
      val install = if (isAPK(source)) "None" else
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
          .format(Date(packageInfo!!.firstInstallTime)).toString()
      val update = if (isAPK(source)) "None" else
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
          .format(Date(packageInfo!!.lastUpdateTime)).toString()
      val size = getStringSize(if (isAPK(source)) getSize(source) else getSize(apk))

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

      sub.onSuccess(ApplicationDetail(pn, ver, size, apk, data, install, update, cert))
    }


  fun oAppManifest(source: String): Single<String> =
    Single.create { sub ->
      val path =
        if (isAPK(source)) {
          source
        } else {
          pm.getPackageInfo(source, PackageManager.GET_META_DATA).applicationInfo.sourceDir
        }
      val manifest = ApkFile(path).manifestXml
      sub.onSuccess(manifest)
    }

  fun oAppResource(source: String): Single<ArrayList<String>> =
    Single.create { sub ->
      TODO("Get App Resources")
    }


  private fun isAPK(source: String) = source.endsWith(".apk")

  private fun getSize(path: String?): Long {
    if (path == null) return 0
    val file = File(path)
    if (file.exists()) {
      var result: Long = if (file.isDirectory) 0 else file.length()
      val fileList = file.listFiles()
      fileList?.forEach {
        result += if (it.isDirectory) {
          getSize(it.absolutePath)
        } else {
          it.length()
        }
      }
      return result
    }
    return 0
  }

  private fun getStringSize(size: Long): String {
    if (size <= 0)
      return "0MB"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
  }
}
