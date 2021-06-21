package com.kwsilence.apkviewer.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kwsilence.apkviewer.adapter.ApplicationListAdapter
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.databinding.FragmentDiskAppListBinding
import com.kwsilence.apkviewer.util.FilterableFragment
import com.kwsilence.apkviewer.viewmodel.DiskAppViewModel
import com.kwsilence.apkviewer.viewmodel.MainViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class DiskAppListFragment : FilterableFragment() {

  private lateinit var binding: FragmentDiskAppListBinding
  private lateinit var viewModel: DiskAppViewModel
  private var adapter: ApplicationListAdapter? = null
  private val disposeBag = CompositeDisposable()
  private val packageManager: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun filter(constraint: String?) {
    adapter?.filter(constraint)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentDiskAppListBinding.inflate(inflater, container, false)

    val viewModelFactory = MainViewModelFactory(packageManager)
    viewModel = ViewModelProvider(this, viewModelFactory).get(DiskAppViewModel::class.java)

    adapter = viewModel.listAdapter
    binding.listApp.adapter = adapter
    binding.listApp.layoutManager = LinearLayoutManager(requireContext())

    if (haveStoragePermission()){
      initDiskApp()
    } else {
      requestPermission()
    }

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

////  LAGS!!!
//  override fun onResume() {
////    init list after permission granted
//    if (haveStoragePermission()) {
//      initDiskApp()
//    } else {
//      Toast.makeText(requireContext(), "Require permission", Toast.LENGTH_LONG).show()
//    }
//    super.onResume()
//  }

  private fun haveStoragePermission() =
    ActivityCompat.checkSelfPermission(
      requireActivity(),
      Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

  private fun requestPermission() {
    if (!haveStoragePermission()) {
      val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
      ActivityCompat.requestPermissions(requireActivity(), permissions, 1)
    }
  }

  private fun loading(isLoading: Boolean){
    if (isLoading){
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