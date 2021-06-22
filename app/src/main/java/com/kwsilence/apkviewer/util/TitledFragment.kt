package com.kwsilence.apkviewer.util

import androidx.fragment.app.Fragment

abstract class TitledFragment(private val title: String?) : Fragment() {
  fun getTitle() = title
}
