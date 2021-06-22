package com.kwsilence.apkviewer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kwsilence.apkviewer.util.FilterableTitledFragment

class AppViewPagerAdapter(
  fm: FragmentManager,
  l: Lifecycle
) : FragmentStateAdapter(fm, l), TabLayoutMediator.TabConfigurationStrategy {

  //(not)temporary solution
  companion object {
    private var filterConstraint: String? = null
  }

  private val fragments = ArrayList<FilterableTitledFragment>()

  override fun getItemCount(): Int = fragments.size

  override fun createFragment(position: Int): Fragment = fragments[position]

  fun addFragments(fragments: List<FilterableTitledFragment>) = this.fragments.addAll(fragments)

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = fragments[position].getTitle()
  }

  fun filter(constraint: String?) {
    filterConstraint = constraint
    fragments.forEach { it.filter(constraint) }
  }

  fun getFilterConstraint() = filterConstraint

}