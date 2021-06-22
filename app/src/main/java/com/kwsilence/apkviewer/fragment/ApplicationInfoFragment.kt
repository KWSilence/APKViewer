package com.kwsilence.apkviewer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kwsilence.apkviewer.databinding.FragmentApplicationInfoBinding
import com.kwsilence.apkviewer.util.TitledFragment

class ApplicationInfoFragment(title: String) : TitledFragment(title) {

  private lateinit var binding: FragmentApplicationInfoBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentApplicationInfoBinding.inflate(inflater, container, false)
    return binding.root
  }

  fun setText(txt: String) {
    binding.txtInfo.text = txt
  }

}
