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
  private val packageManager: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentApplicationDetailBinding.inflate(inflater, container, false)

    //TODO ViewModel
    val applicationInfo =
      packageManager.getApplicationInfo(args.packageName, PackageManager.GET_META_DATA)
    binding.appHead.imgApp.setImageDrawable(applicationInfo.loadIcon(packageManager))
    binding.appHead.nameApp.text = applicationInfo.loadLabel(packageManager)

    return binding.root
  }
}