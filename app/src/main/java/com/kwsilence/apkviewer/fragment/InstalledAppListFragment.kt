package com.kwsilence.apkviewer.fragment

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kwsilence.apkviewer.R
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
  private var userInstalledAdapter: ApplicationListAdapter? = null
  private var systemInstalledAdapter: ApplicationListAdapter? = null
  private var listCount = 0
  private val disposeBag = CompositeDisposable()
  private val readyList = MutableLiveData(0)
  private val pm: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun filter(constraint: String?) {
    userInstalledAdapter?.filter(constraint)
    systemInstalledAdapter?.filter(constraint)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentInstalledAppListBinding.inflate(inflater, container, false)

    val viewModelFactory = MainViewModelFactory(pm)
    viewModel = ViewModelProvider(this, viewModelFactory).get(InstalledAppViewModel::class.java)

    listCount = viewModel.listAdapterCount

    userInstalledAdapter = viewModel.userInstalledListAdapter
    binding.listUserApp.adapter = userInstalledAdapter
    binding.listUserApp.layoutManager = LinearLayoutManager(requireContext())

    systemInstalledAdapter = viewModel.systemInstalledListAdapter
    binding.listSystemApp.adapter = systemInstalledAdapter
    binding.listSystemApp.layoutManager = LinearLayoutManager(requireContext())

    binding.txtUserApp.setOnClickListener {
      collapseList(
        it as TextView,
        binding.listUserApp,
        R.string.text_installed_user_apps,
        R.string.app_list_collapsed_chars
      )
    }

    binding.txtSystemApp.setOnClickListener {
      collapseList(
        it as TextView,
        binding.listSystemApp,
        R.string.text_installed_system_apps,
        R.string.app_list_collapsed_chars
      )
    }

    readyList.observe(viewLifecycleOwner, { if (it >= listCount) loading(false) })

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
        userInstalledAdapter!!.setData(apps)
        readyList.value = readyList.value?.plus(1)
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
        systemInstalledAdapter!!.setData(apps)
        readyList.value = readyList.value?.plus(1)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  @SuppressLint("SetTextI18n")
  private fun collapseList(textView: TextView, list: RecyclerView, textID: Int, collapseID: Int) {
    val collapseChars = getString(collapseID).split(" ")
    val defaultString = getString(textID)
    if (textView.text.startsWith(collapseChars[0])) {
      textView.text = defaultString
      list.visibility = View.VISIBLE
    } else {
      textView.text = "${collapseChars[0]} $defaultString ${collapseChars[1]}"
      list.visibility = View.GONE
    }
  }

  private fun loading(isLoading: Boolean) {
    if (isLoading) {
      binding.progressBar.visibility = View.VISIBLE
      binding.containerListApp.visibility = View.INVISIBLE
    } else {
      binding.progressBar.visibility = View.GONE
      binding.containerListApp.visibility = View.VISIBLE
    }
  }

  override fun onDestroy() {
    disposeBag.clear()
    super.onDestroy()
  }
}
