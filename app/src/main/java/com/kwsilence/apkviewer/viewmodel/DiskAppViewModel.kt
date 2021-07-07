package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwsilence.apkviewer.adapter.ApplicationListAdapter
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.util.APKFileUtils
import com.kwsilence.apkviewer.util.BitmapUtils
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    val apkPaths = APKFileUtils.findAPK(path)
    val jobs = ArrayList<Job>()
    apkPaths.forEach {
      val job = viewModelScope.launch(Dispatchers.IO) {
        val info =
          pm.getPackageArchiveInfo(it, PackageManager.GET_META_DATA)!!.applicationInfo
        info.sourceDir = it
        info.publicSourceDir = it

        var icon: Bitmap? = null
        var name = ""
        val iconLoad = this.launch {
          icon = BitmapUtils.drawableToBitmap(info.loadIcon(pm))
        }
        val labelLoad = this.launch {
          name = info.loadLabel(pm).toString()
        }
        iconLoad.join()
        labelLoad.join()
        list.add(Application(BitmapUtils.pngBitmapToByteArray(icon), name, it))
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
}
