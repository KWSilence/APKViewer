package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModelFactory(private val pm: PackageManager) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return modelClass.getConstructor(PackageManager::class.java).newInstance(pm)
  }
}
