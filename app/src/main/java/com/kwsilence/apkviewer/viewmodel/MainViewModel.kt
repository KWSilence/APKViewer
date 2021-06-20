package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
}