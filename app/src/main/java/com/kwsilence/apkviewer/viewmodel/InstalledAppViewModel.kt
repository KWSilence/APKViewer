package com.kwsilence.apkviewer.viewmodel

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwsilence.apkviewer.adapter.segmented.SegmentedAppListAdapter
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.util.BitmapUtils
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class InstalledAppViewModel(private val pm: PackageManager) : ViewModel() {

  private var installedUserAppsUsed = false
  private var installedSystemAppsUsed = false
  val adapter = SegmentedAppListAdapter()
  val listSize = 2 //Oh yes, magic number

  //I think it should be single use function
  val oInstalledUserApplication: Single<ArrayList<Application>> = Single.create { sub ->
    if (installedUserAppsUsed) {
      sub.onSuccess(ArrayList())
      return@create
    }
    viewModelScope.launch {
      sub.onSuccess(getAppList(false))
    }
  }

  val oInstalledSystemApplication: Single<ArrayList<Application>> = Single.create { sub ->
    if (installedSystemAppsUsed) {
      sub.onSuccess(ArrayList())
      return@create
    }
    viewModelScope.launch {
      sub.onSuccess(getAppList(true))
    }
  }

  private suspend fun getAppList(isSystem: Boolean): ArrayList<Application> {
    val list = ArrayList<Application>()
    val jobs = ArrayList<Job>()
    pm.getInstalledApplications(PackageManager.GET_META_DATA).forEach { info ->
      val job = viewModelScope.launch(Dispatchers.IO) {
        if (isSystem xor (info.flags.and(ApplicationInfo.FLAG_SYSTEM) > 0))
          return@launch
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
        val packageName = info.packageName
        list.add(Application(BitmapUtils.pngBitmapToByteArray(icon), name, packageName))
      }
      jobs.add(job)
    }
    jobs.forEach {
      it.join()
    }
    list.sortBy { it.name }

    if (isSystem)
      installedSystemAppsUsed = true
    else
      installedUserAppsUsed = true

    return list
  }
}
