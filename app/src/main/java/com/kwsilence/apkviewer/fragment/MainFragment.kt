package com.kwsilence.apkviewer.fragment

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.kwsilence.apkviewer.R
import com.kwsilence.apkviewer.adapter.AppViewPagerAdapter
import com.kwsilence.apkviewer.databinding.FragmentMainBinding
import com.kwsilence.apkviewer.util.FilterableTitledFragment
import com.kwsilence.apkviewer.viewmodel.MainViewModel

class MainFragment : Fragment() {

  private lateinit var binding: FragmentMainBinding
  private val viewModel by viewModels<MainViewModel>()
  private lateinit var adapter: AppViewPagerAdapter

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentMainBinding.inflate(inflater, container, false)

    initAppViewPagerAdapter()

    binding.viewPagerApp.adapter = adapter
    TabLayoutMediator(binding.tabLayoutApp, binding.viewPagerApp, adapter).attach()

    setHasOptionsMenu(true)
    return binding.root
  }

  private fun initAppViewPagerAdapter() {
    //dunno how save instance of adapter
    if (!viewModel.isFilled()) {
      val list = ArrayList<FilterableTitledFragment>()
      list.add(InstalledAppListFragment(getString(R.string.text_installed_list)))
      list.add(DiskAppListFragment(getString(R.string.text_disk_list)))
      viewModel.fillFragments(list)
    }

    adapter = AppViewPagerAdapter(
      requireActivity().supportFragmentManager,
      lifecycle,
      viewModel.fragments
    )
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.search_app, menu)
    val searchView = menu.findItem(R.id.search_app).actionView as SearchView

    val constraint = adapter.getFilterConstraint()
    if (constraint != null && constraint.isNotEmpty()) {
      searchView.setQuery(constraint, false)
      searchView.isIconified = false
      searchView.clearFocus()
    }

    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean = false
      override fun onQueryTextChange(newText: String?): Boolean {
        adapter.filter(newText)
        return false
      }
    })
    super.onCreateOptionsMenu(menu, inflater)
  }
}
