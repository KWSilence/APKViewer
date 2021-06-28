package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.model.ApplicationDetail
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import org.jf.baksmali.Baksmali
import org.jf.baksmali.BaksmaliOptions
import org.jf.dexlib2.DexFileFactory
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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
      val apkFile = ApkFile(apk)
      apkFile.apkSingers.forEach {
        cert += "path: ${it.path}"
        it.certificateMetas.forEach { cm ->
          cert += "\nalgorithm: ${cm.signAlgorithm}"
          cert += "\nstart: ${simpleFormatter.format(cm.startDate)}"
          cert += "\nend: ${simpleFormatter.format(cm.endDate)}"
          cert += "\nmd5: ${cm.certMd5}"
        }
      }
      apkFile.close()
      if (cert.isEmpty())
        cert = "None"

      sub.onSuccess(ApplicationDetail(pn, ver, size, apk, data, install, update, cert))
    }


  fun oAppManifest(source: String): Single<String> =
    Single.create { sub ->
      val file = ApkFile(getApkPath(source))
      val manifest = file.manifestXml
      file.close()
      sub.onSuccess(manifest)
    }

  fun oAppResource(source: String): Single<ArrayList<String>> =
    Single.create { sub ->
      val list = ArrayList<String>()
      val zip = ZipFile(getApkPath(source))
      zip.fileHeaders.forEach { header ->
        val name = header.fileName
        if (name.endsWith(".so"))
          list.add(name)
      }
      if (list.isEmpty())
        list.add("None")
      sub.onSuccess(list)
    }

  fun oAppDexFiles(source: String): Single<Unit> =
    Single.create { sub ->
      clearDir(Constant.TMP_DIR)
      val zip = ZipFile(getApkPath(source))
      zip.fileHeaders.forEach { header ->
        val name = header.fileName
        if (name.endsWith(".dex"))
          zip.extractFile(header, Constant.TMP_DIR)
      }
      val jobs = ArrayList<Job>()
      File(Constant.TMP_DIR).listFiles { _, name -> name.endsWith(".dex") }?.forEach {
        val job = viewModelScope.launch(Dispatchers.IO) {
          Log.d(Constant.DEBUG_TAG, it.name)
          Baksmali.disassembleDexFile(
            DexFileFactory.loadDexFile(it.absolutePath, null),
            File("${Constant.TMP_DIR}${File.separator}${it.name.split(".")[0]}"),
            2,
            BaksmaliOptions()
          )
          Log.d(Constant.DEBUG_TAG, "end ${it.name}")
        }
        jobs.add(job)
      }
      viewModelScope.launch {
        jobs.forEach { it.join() }
        sub.onSuccess(Unit)
      }
    }

  private fun clearDir(path: String) {
    Log.d(Constant.DEBUG_TAG, path)
    val dir = File(path)
    if (dir.exists())
      dir.listFiles()?.forEach { it.deleteRecursively() }
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

  private fun getApkPath(source: String) =
    if (isAPK(source)) {
      source
    } else {
      pm.getPackageInfo(source, PackageManager.GET_META_DATA).applicationInfo.sourceDir
    }
}
