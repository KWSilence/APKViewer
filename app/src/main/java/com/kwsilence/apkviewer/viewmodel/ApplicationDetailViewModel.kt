package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.model.ApplicationDetail
import io.reactivex.rxjava3.core.Single
import java.text.SimpleDateFormat
import java.util.*

class ApplicationDetailViewModel(private val pm: PackageManager) : ViewModel() {

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
      val size = "None" //TODO size
      val cert = "None" //TODO certificate

      sub.onSuccess(ApplicationDetail(pn, ver, size, apk, data, install, update, cert))
    }


  fun oAppManifest(source: String): Single<String> =
    Single.create { sub ->
      TODO("Get App Manifest")
    }

  fun oAppResource(source: String): Single<ArrayList<String>> =
    Single.create { sub ->
      TODO("Get App Resources")
    }


  private fun isAPK(source: String) = source.endsWith(".apk")

}
