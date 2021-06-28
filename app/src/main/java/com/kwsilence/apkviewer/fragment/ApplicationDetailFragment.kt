package com.kwsilence.apkviewer.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.kwsilence.apkviewer.R
import com.kwsilence.apkviewer.adapter.TabViewPagerAdapter
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.databinding.FragmentApplicationDetailBinding
import com.kwsilence.apkviewer.util.PermissionManager
import com.kwsilence.apkviewer.viewmodel.ApplicationDetailViewModel
import com.kwsilence.apkviewer.viewmodel.MainViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ApplicationDetailFragment : Fragment() {

  private val args by navArgs<ApplicationDetailFragmentArgs>()
  private lateinit var binding: FragmentApplicationDetailBinding
  private val fragments = ArrayList<ApplicationInfoFragment>()
  private lateinit var adapter: TabViewPagerAdapter<ApplicationInfoFragment>
  private lateinit var viewModel: ApplicationDetailViewModel
  private val disposeBag = CompositeDisposable()
  private val pm: PackageManager by lazy {
    requireContext().packageManager
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentApplicationDetailBinding.inflate(inflater, container, false)

    val vmf = MainViewModelFactory(pm)
    viewModel = ViewModelProvider(this, vmf).get(ApplicationDetailViewModel::class.java)

    initAdapter()
    binding.appPager.adapter = adapter
    TabLayoutMediator(binding.appTabs, binding.appPager, adapter).attach()

    initAppHead()
    initAppDetail(fragments[0])
    initAppManifest(fragments[1])
    initAppResource(fragments[2])

    if (!PermissionManager.requestPermission(requireActivity(), PermissionManager.WRITE_PERMISSION))
      initAppDexFiles(fragments[2])
    else
      Toast.makeText(requireContext(), "Require WRITE_PERMISSION", Toast.LENGTH_SHORT).show()

    return binding.root
  }

  private fun initAdapter() {
    fragments.apply {
      add(ApplicationInfoFragment(getString(R.string.app_detail_tab_info)))
      add(ApplicationInfoFragment(getString(R.string.app_detail_tab_manifest)))
      add(ApplicationInfoFragment(getString(R.string.app_detail_tab_resource)))
    }

    adapter = TabViewPagerAdapter(
      requireActivity().supportFragmentManager,
      lifecycle,
      fragments
    )
  }

  private fun initAppHead() {
    val dispose = viewModel.oAppHead(args.source)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        binding.appHead.imgApp.setImageDrawable(it.icon)
        binding.appHead.nameApp.text = it.name
        binding.appHead.sourceApp.text = it.source
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun initAppDetail(fragment: ApplicationInfoFragment) {
    val dispose = viewModel.oAppDetail(args.source)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ detail ->
        val res = listOf(
          getInfoMap(getString(R.string.app_detail_package_name), detail.packageName),
          getInfoMap(getString(R.string.app_detail_version), detail.version),
          getInfoMap(getString(R.string.app_detail_size), detail.size),
          getInfoMap(getString(R.string.app_detail_apk_file), detail.apkFile),
          getInfoMap(getString(R.string.app_detail_data_path), detail.dataPath),
          getInfoMap(getString(R.string.app_detail_install_date), detail.installDate),
          getInfoMap(getString(R.string.app_detail_update_date), detail.updateDate),
          getInfoMap(getString(R.string.app_detail_certificate), detail.certificate)
        )
        fragment.setData(res)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun initAppManifest(fragment: ApplicationInfoFragment) {
    val dispose = viewModel.oAppManifest(args.source)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        fragment.setData(listOf(getInfoMap(null, it)))
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun initAppResource(fragment: ApplicationInfoFragment) {
    val dispose = viewModel.oAppResource(args.source)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ list ->
        val res = ArrayList<HashMap<String, String?>>()
        list.forEach {
          res.add(getInfoMap(null, it))
        }
        fragment.setData(res)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun initAppDexFiles(fragment: ApplicationInfoFragment) {
    val dispose = viewModel.oAppDexFiles(args.source)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        Log.d(Constant.DEBUG_TAG, "initDexFiles OK")
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun getInfoMap(name: String?, content: String?) =
    HashMap<String, String?>().apply { put("name", name); put("content", content) }

  override fun onDestroy() {
    disposeBag.clear()
    super.onDestroy()
  }
}
