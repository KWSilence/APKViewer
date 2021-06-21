package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwsilence.apkviewer.adapter.ApplicationListAdapter
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.model.Application
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val packageManager: PackageManager) : ViewModel() {


  private var installedAppsUsed = false
  val listAdapter = ApplicationListAdapter()

  //I think it should be single use function
  val oInstalledApplicationFull: Single<List<Application>> = Single.create { sub ->
    val list = ArrayList<Application>()
    if (installedAppsUsed) {
      sub.onSuccess(list)
      return@create
    }
    val jobs = ArrayList<Job>()
    packageManager.getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
      Log.d(Constant.DEBUG_TAG, "SINGLE1")
      val job = viewModelScope.launch(Dispatchers.IO) {
        var icon: Drawable? = null
        var name = ""
        val iconLoad = this.launch {
          icon = info.loadIcon(packageManager)
        }
        val labelLoad = this.launch {
          name = info.loadLabel(packageManager).toString()
        }
        iconLoad.join()
        labelLoad.join()
        val packageName = info.packageName
        list.add(Application(icon!!, name, packageName))
      }
      jobs.add(job)
    }
    viewModelScope.launch {
      jobs.forEach {
        it.join()
      }
      list.sortBy { it.name }
      installedAppsUsed = true
      sub.onSuccess(list)
    }
  }

  val oDiskApplications: Single<ArrayList<String>> = Single.create { sub ->
    //this function deprecated, but it only used to read data
    @Suppress("DEPRECATION")
    val path = Environment.getExternalStorageDirectory()
    val list = ArrayList<String>()
    findAPK(path, list)
    sub.onSuccess(list)
  }

  private fun findAPK(dir: File, list: ArrayList<String>) {
    val files = dir.listFiles()
    files?.filter { it.path.endsWith(".apk") || it.isDirectory }?.toList()?.forEach { file ->
      if (file.isDirectory) {
        findAPK(file, list)
      } else {
        list.add(file.absolutePath)
      }
    }
  }

}