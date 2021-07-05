package com.kwsilence.apkviewer.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kwsilence.apkviewer.BuildConfig
import com.kwsilence.apkviewer.adapter.ApplicationListAdapter
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.databinding.FragmentDiskAppListBinding
import com.kwsilence.apkviewer.util.FilterableTitledFragment
import com.kwsilence.apkviewer.viewmodel.DiskAppViewModel
import com.kwsilence.apkviewer.viewmodel.MainViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class DiskAppListFragment(title: String?) : FilterableTitledFragment(title) {

  private lateinit var binding: FragmentDiskAppListBinding
  private lateinit var viewModel: DiskAppViewModel
  private var adapter: ApplicationListAdapter? = null
  private val disposeBag = CompositeDisposable()
  private val pm: PackageManager by lazy {
    requireContext().packageManager
  }

  private val resultLauncher by lazy {
    registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager())
        initDiskApp()
    }
  }

  private val permissionRequestLauncher by lazy {
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) initDiskApp() }
  }

  override fun filter(constraint: String?) {
    adapter?.filter(constraint)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentDiskAppListBinding.inflate(inflater, container, false)

    val viewModelFactory = MainViewModelFactory(pm)
    viewModel = ViewModelProvider(this, viewModelFactory).get(DiskAppViewModel::class.java)

    adapter = viewModel.listAdapter
    binding.listApp.adapter = adapter
    binding.listApp.layoutManager = LinearLayoutManager(requireContext())

    requestPermissionAndInit()

    return binding.root
  }

  private fun initDiskApp() {
    loading(true)
    val dispose = viewModel.oDiskApplications
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ list ->
        adapter!!.setData(list)
        loading(false)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun requestPermissionAndInit() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!Environment.isExternalStorageManager()) {
        resultLauncher.launch(
          Intent(
            ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
          )
        )
      } else {
        initDiskApp()
      }
    } else {
      permissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
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
