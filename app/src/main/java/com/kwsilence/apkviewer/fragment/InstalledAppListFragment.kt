package com.kwsilence.apkviewer.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kwsilence.apkviewer.adapter.ApplicationListAdapter
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.databinding.FragmentInstalledAppListBinding
import com.kwsilence.apkviewer.util.FilterableTitledFragment
import com.kwsilence.apkviewer.viewmodel.InstalledAppViewModel
import com.kwsilence.apkviewer.viewmodel.MainViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class InstalledAppListFragment(title: String?) : FilterableTitledFragment(title) {

  private lateinit var binding: FragmentInstalledAppListBinding
  private lateinit var viewModel: InstalledAppViewModel
  private var adapter: ApplicationListAdapter? = null
  private val disposeBag = CompositeDisposable()
  private val pm: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun filter(constraint: String?) {
    adapter?.filter(constraint)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentInstalledAppListBinding.inflate(inflater, container, false)

    val viewModelFactory = MainViewModelFactory(pm)
    viewModel = ViewModelProvider(this, viewModelFactory).get(InstalledAppViewModel::class.java)

    adapter = viewModel.listAdapter
    binding.listApp.adapter = adapter
    binding.listApp.layoutManager = LinearLayoutManager(requireContext())

    initInstalledApp()

    return binding.root
  }

  private fun initInstalledApp() {
    loading(true)
    val dispose = viewModel.oInstalledApplicationFull
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ apps ->
        adapter!!.setData(apps)
        loading(false)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun loading(isLoading: Boolean) {
    if (isLoading) {
      binding.progressBar.visibility = View.VISIBLE
      binding.listApp.visibility = View.INVISIBLE
    } else {
      binding.progressBar.visibility = View.GONE
      binding.listApp.visibility = View.VISIBLE
    }
  }

  override fun onDestroy() {
    disposeBag.clear()
    super.onDestroy()
  }
}