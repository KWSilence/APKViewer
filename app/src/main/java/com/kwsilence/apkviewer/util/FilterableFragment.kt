package com.kwsilence.apkviewer.util

import androidx.fragment.app.Fragment

abstract class FilterableFragment : Fragment() {
  abstract fun filter(constraint: String?)
}