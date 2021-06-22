package com.kwsilence.apkviewer.viewmodel

import androidx.lifecycle.ViewModel
import com.kwsilence.apkviewer.util.FilterableTitledFragment

class MainViewModel : ViewModel() {
  val fragments = ArrayList<FilterableTitledFragment>()
  private var filled = false

  fun fillFragments(fragments: List<FilterableTitledFragment>) {
    this.fragments.addAll(fragments)
    filled = true
  }

  fun isFilled() = filled
}