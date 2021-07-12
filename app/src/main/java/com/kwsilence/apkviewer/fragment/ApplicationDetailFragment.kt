package com.kwsilence.apkviewer.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.kwsilence.apkviewer.R
import com.kwsilence.apkviewer.adapter.TabViewPagerAdapter
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.databinding.FragmentApplicationDetailBinding
import com.kwsilence.apkviewer.helper.AppInfoHelper
import com.kwsilence.apkviewer.util.BitmapUtils
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

    setHasOptionsMenu(true)
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
    binding.appHead.imgApp.setImageBitmap(BitmapUtils.byteArrayToBitmap(args.app.icon))
    binding.appHead.nameApp.text = args.app.name
    binding.appHead.sourceApp.text = args.app.source
  }

  private fun initAppDetail(fragment: ApplicationInfoFragment) {
    val dispose = viewModel.oAppDetail(args.app.source)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ detail ->
        val res = listOf(
          getInfoMap(getString(R.string.app_detail_package_name), detail?.packageName),
          getInfoMap(getString(R.string.app_detail_version), detail?.version),
          getInfoMap(getString(R.string.app_detail_size), detail?.size),
          getInfoMap(getString(R.string.app_detail_apk_file), detail?.apkFile),
          getInfoMap(getString(R.string.app_detail_data_path), detail?.dataPath),
          getInfoMap(getString(R.string.app_detail_install_date), detail?.installDate),
          getInfoMap(getString(R.string.app_detail_update_date), detail?.updateDate),
          getInfoMap(getString(R.string.app_detail_certificate), detail?.certificate)
        )
        fragment.setData(res)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun initAppManifest(fragment: ApplicationInfoFragment) {
    val dispose = viewModel.oAppManifest(args.app.source)
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
    val dispose = viewModel.oAppResource(args.app.source)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ list ->
        val res = ArrayList<HashMap<String, String?>>()
        list?.forEach {
          res.add(getInfoMap(null, it))
        }
        fragment.setData(res)
      }, {
        Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
      })
    disposeBag.add(dispose)
  }

  private fun getInfoMap(name: String?, content: String?) =
    HashMap<String, String?>().apply {
      put(AppInfoHelper.APP_INFO_ROW_NAME, name)
      put(AppInfoHelper.APP_INFO_ROW_CONTENT, content)
    }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.open_folder, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.open_folder -> decompileDex()
    }
    return super.onOptionsItemSelected(item)
  }

  private fun decompileDex() {
    AlertDialog.Builder(requireContext()).apply {
      setTitle("Decompile dex files")
      setMessage("Decompile dex files to smali")
      setPositiveButton("Yes") { _, _ ->
        Toast.makeText(requireContext(), "Decompile started", Toast.LENGTH_LONG).show()
        viewModel.oAppSmaliFile(args.app.source)
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
            AlertDialog.Builder(requireContext()).apply {
              setTitle("Success")
              setMessage("Successfully decompiled in \"$it\"")
              setPositiveButton("OK") { _, _ -> }
            }.create().show()
          }, {
            Log.e(Constant.DEBUG_TAG, "it ${it.localizedMessage}")
          })
      }
      setNegativeButton("No") { _, _ -> }
    }.create().show()
  }

  override fun onDestroy() {
    disposeBag.clear()
    super.onDestroy()
  }
}
