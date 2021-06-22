package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwsilence.apkviewer.adapter.ApplicationListAdapter
import com.kwsilence.apkviewer.model.Application
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class DiskAppViewModel(private val pm: PackageManager) : ViewModel() {

  private var diskAppsUsed = false
  val listAdapter = ApplicationListAdapter()

  val oDiskApplications: Single<ArrayList<Application>> = Single.create { sub ->
    val list = ArrayList<Application>()
    if (diskAppsUsed) {
      sub.onSuccess(list)
      return@create
    }
    //this function deprecated, but it only used to read data
    @Suppress("DEPRECATION")
    val path = Environment.getExternalStorageDirectory()
    val apkPaths = ArrayList<String>()
    findAPK(path, apkPaths)
    val jobs = ArrayList<Job>()
    apkPaths.forEach {
      val job = viewModelScope.launch(Dispatchers.IO) {
        val info =
          pm.getPackageArchiveInfo(it, PackageManager.GET_META_DATA)!!.applicationInfo
        info.sourceDir = it
        info.publicSourceDir = it

        var icon: Drawable? = null
        var name = ""
        val iconLoad = this.launch {
          icon = info.loadIcon(pm)
        }
        val labelLoad = this.launch {
          name = info.loadLabel(pm).toString()
        }
        iconLoad.join()
        labelLoad.join()
        list.add(Application(icon, name, it))
      }
      jobs.add(job)
    }
    viewModelScope.launch {
      jobs.forEach {
        it.join()
      }
      list.sortBy { it.name }
      diskAppsUsed = true
      sub.onSuccess(list)
    }
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