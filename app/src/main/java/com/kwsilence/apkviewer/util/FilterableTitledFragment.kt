package com.kwsilence.apkviewer.util

import androidx.fragment.app.Fragment

abstract class FilterableTitledFragment(private val title: String?) : Fragment() {
  abstract fun filter(constraint: String?)
  fun getTitle() = title
}