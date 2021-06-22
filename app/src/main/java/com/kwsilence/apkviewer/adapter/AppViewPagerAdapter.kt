package com.kwsilence.apkviewer.adapter

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.kwsilence.apkviewer.util.FilterableTitledFragment

class AppViewPagerAdapter(
  fm: FragmentManager,
  l: Lifecycle,
  private val fragments: ArrayList<FilterableTitledFragment>
) : TabViewPagerAdapter<FilterableTitledFragment>(fm, l, fragments) {

  //(not)temporary solution
  companion object {
    private var filterConstraint: String? = null
  }

  fun filter(constraint: String?) {
    filterConstraint = constraint
    fragments.forEach { it.filter(constraint) }
  }

  fun getFilterConstraint() = filterConstraint

}
