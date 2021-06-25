package com.kwsilence.apkviewer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kwsilence.apkviewer.adapter.AppInfoListAdapter
import com.kwsilence.apkviewer.databinding.FragmentApplicationInfoBinding
import com.kwsilence.apkviewer.util.TitledFragment

class ApplicationInfoFragment(title: String) : TitledFragment(title) {

  private lateinit var binding: FragmentApplicationInfoBinding
  private val adapter = AppInfoListAdapter()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentApplicationInfoBinding.inflate(inflater, container, false)
    binding.listAppInfo.adapter = adapter
    binding.listAppInfo.layoutManager = LinearLayoutManager(requireContext())
    return binding.root
  }

  fun setData(data: List<HashMap<String, String?>>) {
    adapter.setData(data)
  }
}
