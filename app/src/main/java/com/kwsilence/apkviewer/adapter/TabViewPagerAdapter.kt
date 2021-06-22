package com.kwsilence.apkviewer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kwsilence.apkviewer.util.TitledFragment

open class TabViewPagerAdapter<T : TitledFragment>(
  fm: FragmentManager,
  l: Lifecycle,
  private val fragments: ArrayList<T>
) : FragmentStateAdapter(fm, l), TabLayoutMediator.TabConfigurationStrategy {
  override fun getItemCount(): Int = fragments.size

  override fun createFragment(position: Int): Fragment = fragments[position]

  override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
    tab.text = fragments[position].getTitle()
  }
}
