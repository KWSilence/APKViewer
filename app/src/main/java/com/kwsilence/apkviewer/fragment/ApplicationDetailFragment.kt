package com.kwsilence.apkviewer.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.kwsilence.apkviewer.adapter.TabViewPagerAdapter
import com.kwsilence.apkviewer.databinding.FragmentApplicationDetailBinding

class ApplicationDetailFragment : Fragment() {

  private val args by navArgs<ApplicationDetailFragmentArgs>()
  private lateinit var binding: FragmentApplicationDetailBinding
  private val fragments = ArrayList<ApplicationInfoFragment>()
  private lateinit var adapter: TabViewPagerAdapter<ApplicationInfoFragment>
  private val pm: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentApplicationDetailBinding.inflate(inflater, container, false)

    //TODO ViewModel

    initAdapter()
    binding.appPager.adapter = adapter
    TabLayoutMediator(binding.appTabs, binding.appPager, adapter).attach()

    val src = args.source
    val applicationInfo =
      if (src.endsWith(".apk")) {
        pm.getPackageArchiveInfo(src, PackageManager.GET_META_DATA)!!.applicationInfo
      } else {
        pm.getApplicationInfo(src, PackageManager.GET_META_DATA)
      }

    binding.appHead.imgApp.setImageDrawable(applicationInfo.loadIcon(pm))
    binding.appHead.nameApp.text = applicationInfo.loadLabel(pm)
    binding.appHead.packageNameApp.text = src

    return binding.root
  }

  private fun initAdapter() {
    fragments.apply {
      add(ApplicationInfoFragment("Info"))
      add(ApplicationInfoFragment("Manifest"))
      add(ApplicationInfoFragment("Recourse"))
    }

    adapter = TabViewPagerAdapter(
      requireActivity().supportFragmentManager,
      lifecycle,
      fragments
    )
  }

}
