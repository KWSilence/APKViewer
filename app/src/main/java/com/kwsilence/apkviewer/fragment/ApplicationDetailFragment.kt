package com.kwsilence.apkviewer.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.kwsilence.apkviewer.databinding.FragmentApplicationDetailBinding

class ApplicationDetailFragment : Fragment() {

  private val args by navArgs<ApplicationDetailFragmentArgs>()
  private lateinit var binding: FragmentApplicationDetailBinding
  private val pm: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentApplicationDetailBinding.inflate(inflater, container, false)

    //TODO ViewModel

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
}