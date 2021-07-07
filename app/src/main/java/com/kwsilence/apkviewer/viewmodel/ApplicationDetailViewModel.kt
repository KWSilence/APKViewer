package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import com.kwsilence.apkviewer.model.ApplicationDetail
import com.kwsilence.apkviewer.util.APKFileUtils
import io.reactivex.rxjava3.core.Single
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ApplicationDetailViewModel(private val pm: PackageManager) : ViewModel() {

  private val simpleFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

  fun oAppDetail(source: String): Single<ApplicationDetail> =
    Single.create { sub ->
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
      val file = ApkFile(apk)
      file.apkSingers.forEach {
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
      file.close()

      sub.onSuccess(ApplicationDetail(pn, ver, size, apk, data, install, update, cert))
    }


  fun oAppManifest(source: String): Single<String> =
    Single.create { sub ->
      val file = ApkFile(APKFileUtils.getApkPath(pm, source))
      val manifest = file.manifestXml
      file.close()
      sub.onSuccess(manifest)
    }

  fun oAppResource(source: String): Single<ArrayList<String>> =
    Single.create { sub ->
      val list = ArrayList<String>()
      val zip = ZipFile(APKFileUtils.getApkPath(pm, source))
      zip.fileHeaders.forEach { header ->
        val name = header.fileName
        if (name.endsWith(".so"))
          list.add(name)
      }
      if (list.isEmpty())
        list.add("None")
      sub.onSuccess(list)
    }
}
