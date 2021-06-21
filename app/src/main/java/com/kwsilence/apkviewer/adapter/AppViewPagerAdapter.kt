package com.kwsilence.apkviewer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kwsilence.apkviewer.util.FilterableFragment

class AppViewPagerAdapter(
  fm: FragmentManager,
  l: Lifecycle
) : FragmentStateAdapter(fm, l), TabLayoutMediator.TabConfigurationStrategy {

  //temporary solution
  companion object {
    private var filterConstraint: String? = null
  }

  private val fragments = ArrayList<FilterableFragment>()
  private val titles = ArrayList<String>()

  override fun getItemCount(): Int = fragments.size

  override fun createFragment(position: Int): Fragment = fragments[position]

  fun addFragment(fragment: FilterableFragment, title: String) {
    fragments.add(fragment)
    titles.add(title)
  }

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = titles[position]
  }

  fun filter(constraint: String?) {
    filterConstraint = constraint
    fragments.forEach { it.filter(constraint) }
  }

  fun getFilterConstraint() = filterConstraint

}