package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.model.ApplicationDetail
import io.reactivex.rxjava3.core.Single

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

    }


  fun oAppManifest(source: String): Single<String> =
    Single.create { sub ->

    }

  fun oAppResource(source: String): Single<ArrayList<String>> =
    Single.create { sub ->

    }


  private fun isAPK(source: String) = source.endsWith(".apk")

}