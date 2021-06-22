package com.kwsilence.apkviewer.util

abstract class FilterableTitledFragment(title: String?) : TitledFragment(title) {
  abstract fun filter(constraint: String?)
}
