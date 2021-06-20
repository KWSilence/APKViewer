package com.kwsilence.apkviewer.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.databinding.ApplicationRowBinding
import com.kwsilence.apkviewer.fragment.MainFragmentDirections
import com.kwsilence.apkviewer.model.Application
import java.util.*
import kotlin.collections.ArrayList

class ApplicationListAdapter : RecyclerView.Adapter<ApplicationListAdapter.MyViewHolder>() {

  //to "save" list after return from details fragment
  companion object {
    private var displayedAppList = ArrayList<Application>()
    private var appList = ArrayList<Application>()
  }

  //dunno how observe change in itemCount
  var mItemCount = MutableLiveData<Int>()

  class MyViewHolder(val binding: ApplicationRowBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ApplicationRowBinding.inflate(inflater, parent, false)
    return MyViewHolder(binding)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val currentApp = displayedAppList[position]
    holder.binding.imgApp.setImageDrawable(currentApp.icon)
    holder.binding.nameApp.text = currentApp.name
    holder.binding.packageNameApp.text = currentApp.packageName
    //navigate to details
    holder.binding.appRowLayout.setOnClickListener {
      val action = MainFragmentDirections.fromMainToDetails(currentApp.packageName)
      holder.itemView.findNavController().navigate(action)
    }
  }

  override fun getItemCount(): Int {
    return displayedAppList.size
  }

  fun setData(apps: List<Application>) {
    if (apps.isNotEmpty()) {
      appList.clear()
      appList.addAll(apps)
      displayedAppList.clear()
      displayedAppList.addAll(apps)
    }
    dataSetChanged()
  }

  fun filter(constraint: String?) {
    val nConstraint = constraint?.trim()?.lowercase(Locale.getDefault())
    displayedAppList.clear()
    if (nConstraint == null || nConstraint.isEmpty()) {
      displayedAppList.addAll(appList)
    } else {
      appList.filterTo(
        displayedAppList,
        { it.name.lowercase(Locale.getDefault()).contains(nConstraint) })
    }
    dataSetChanged()
  }

  //dunno how observe change in itemCount
  private fun dataSetChanged() {
    mItemCount.value = itemCount
    notifyDataSetChanged()
  }

}