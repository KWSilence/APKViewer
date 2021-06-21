package com.kwsilence.apkviewer.viewmodel

import androidx.lifecycle.ViewModel
import com.kwsilence.apkviewer.util.FilterableFragment

class MainViewModel : ViewModel() {
  val fragments = ArrayList<FilterableFragment>()
  val titles = ArrayList<String>()
  var filled = false
}