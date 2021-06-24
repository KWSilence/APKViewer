package com.kwsilence.apkviewer.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kwsilence.apkviewer.R
import com.kwsilence.apkviewer.adapter.segmented.SegmentApp
import com.kwsilence.apkviewer.adapter.segmented.SegmentedAppListAdapter
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
  private var installedAppAdapter: SegmentedAppListAdapter? = null
  private val disposeBag = CompositeDisposable()
  private var listCount = 0 //magic number again :)
  private val pm: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun filter(constraint: String?) {
    installedAppAdapter?.filter(constraint)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentInstalledAppListBinding.inflate(inflater, container, false)

    val viewModelFactory = MainViewModelFactory(pm)
    viewModel = ViewModelProvider(this, viewModelFactory).get(InstalledAppViewModel::class.java)

    listCount = viewModel.listSize

    installedAppAdapter = viewModel.adapter
    installedAppAdapter!!.initSize(listCount)
    binding.listApp.adapter = installedAppAdapter
    binding.listApp.layoutManager = LinearLayoutManager(requireContext())

    installedAppAdapter!!.readySetList.observe(viewLifecycleOwner, { loading(false) })

    loading(true)
    initUserInstalledApp()
    initSystemInstalledApp()

    return binding.root
  }

  private fun initUserInstalledApp() {
    val dispose = viewModel.oInstalledUserApplication
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ apps ->
        val title = getString(R.string.text_installed_user_apps)
        installedAppAdapter!!.setSegment(0, SegmentApp(title, apps))
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun initSystemInstalledApp() {
    val dispose = viewModel.oInstalledSystemApplication
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ apps ->
        val title = getString(R.string.text_installed_system_apps)
        installedAppAdapter!!.setSegment(1, SegmentApp(title, apps))
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
