package com.kwsilence.apkviewer.util

import android.content.pm.PackageManager
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object APKFileUtils {
  fun findAPK(dir: File): ArrayList<String> {
    val list = ArrayList<String>()
    val files = dir.listFiles()
    files?.filter { it.path.endsWith(".apk") || it.isDirectory }?.toList()?.forEach { file ->
      if (file.isDirectory) {
        list.addAll(findAPK(file))
      } else {
        list.add(file.absolutePath)
      }
    }
    return list
  }

  fun isAPK(source: String) = source.endsWith(".apk") and File(source).exists()

  fun getApkPath(pm: PackageManager, source: String): String =
    if (isAPK(source)) {
      source
    } else {
      pm.getPackageInfo(source, PackageManager.GET_META_DATA).applicationInfo.sourceDir
    }

  fun getStringSize(path: String?): String {
    val size = getSize(path)
    if (size <= 0) return "0MB"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
  }

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
}
