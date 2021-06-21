package com.kwsilence.apkviewer.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kwsilence.apkviewer.R
import com.kwsilence.apkviewer.adapter.ApplicationListAdapter
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.databinding.FragmentMainBinding
import com.kwsilence.apkviewer.viewmodel.MainViewModel
import com.kwsilence.apkviewer.viewmodel.MainViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class MainFragment : Fragment() {

  private lateinit var binding: FragmentMainBinding
  private lateinit var adapter: ApplicationListAdapter
  private lateinit var viewModel: MainViewModel
  private val disposeBag = CompositeDisposable()
  private val packageManager: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentMainBinding.inflate(inflater, container, false)

    val viewModelFactory = MainViewModelFactory(packageManager)
    viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

    adapter = viewModel.listAdapter
    binding.containerInstalledApps.listApp.adapter = adapter
    binding.containerInstalledApps.listApp.layoutManager = LinearLayoutManager(requireContext())

//    initInstalledApp()
    requestPermission()
//    if (!haveStoragePermission())
//      return binding.root

    initDiskApp()
    return binding.root
  }

  private fun initInstalledApp() {
    loading(true)

    //observe itemCount change while filtering
    adapter.mItemCount.observe(viewLifecycleOwner, {
      binding.containerInstalledApps.txtInstalled.text = "${getString(R.string.text_installed)} (${it})"
    })

    val dispose = viewModel.oInstalledApplicationFull
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ apps ->
        adapter.setData(apps)
        loading(false)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  //can't write/read files not in self data directory
  private fun initDiskApp() {

    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//    val path = requireContext().getExternalFilesDir(null)
//    val file = File(path!!.absolutePath+File.separator+"data.txt")
//    Log.d(Constant.DEBUG_TAG, file.absolutePath)
//    val isCreated = file.createNewFile()
//    if (isCreated){
//      Log.d(Constant.DEBUG_TAG, "file created")
//    }
    Log.d(Constant.DEBUG_TAG, path!!.absolutePath)
    find(path)
    Log.d(Constant.DEBUG_TAG, "END")
  }

  private fun find(file: File) {
    val files = file.listFiles()
    Log.d(Constant.DEBUG_TAG, "FIND ${file.absolutePath}")
    files?.forEach { f ->
      if (f.isDirectory) {
        Log.d(Constant.DEBUG_TAG, "DIR ${f.absolutePath}")
        find(f)
      } else {
        Log.d(Constant.DEBUG_TAG, "FILE ${f.absolutePath}")
      }
    }
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

  private fun loading(isLoading: Boolean) {
    if (isLoading) {
      binding.containerInstalledApps.root.visibility = View.INVISIBLE
      binding.progressApps.visibility = View.VISIBLE
    } else {
      binding.containerInstalledApps.root.visibility = View.VISIBLE
      binding.progressApps.visibility = View.GONE
    }
    setHasOptionsMenu(!isLoading)
  }


  private fun haveStoragePermission() =
    ActivityCompat.checkSelfPermission(requireActivity(), Manifest
      .permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED

  private fun requestPermission() {
    if (!haveStoragePermission()) {
      val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
      )
      ActivityCompat.requestPermissions(requireActivity(), permissions, 1)
    }
  }

}