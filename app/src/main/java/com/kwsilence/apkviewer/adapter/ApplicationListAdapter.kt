package com.kwsilence.apkviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kwsilence.apkviewer.databinding.ApplicationRowBinding
import com.kwsilence.apkviewer.fragment.MainFragmentDirections
import com.kwsilence.apkviewer.model.Application
import com.kwsilence.apkviewer.util.BitmapUtils
import java.util.*
import kotlin.collections.ArrayList

class ApplicationListAdapter : RecyclerView.Adapter<ApplicationListAdapter.MyViewHolder>() {

  private var displayedAppList = ArrayList<Application>()
  private var appList = ArrayList<Application>()

  class MyViewHolder(val binding: ApplicationRowBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ApplicationRowBinding.inflate(inflater, parent, false)
    return MyViewHolder(binding)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val currentApp = displayedAppList[position]
    holder.binding.imgApp.setImageBitmap(BitmapUtils.byteArrayToBitmap(currentApp.icon))
    holder.binding.nameApp.text = currentApp.name
    holder.binding.sourceApp.text = currentApp.source
    //navigate to details
    holder.binding.appRowLayout.setOnClickListener {
      val action = MainFragmentDirections.fromMainToDetails(currentApp)
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
      notifyDataSetChanged()
    }
  }

  fun filter(constraint: String?) {
    val nConstraint = constraint?.trim()?.lowercase(Locale.getDefault())
    displayedAppList.clear()
    if (nConstraint == null || nConstraint.isEmpty()) {
      displayedAppList.addAll(appList)
    } else {
      appList.filterTo(
        displayedAppList,
        {
          it.name.lowercase(Locale.getDefault()).contains(nConstraint) ||
              if (it.source.endsWith(".apk")) false else
                it.source.lowercase(Locale.getDefault()).contains(nConstraint)
        })
    }
    notifyDataSetChanged()
  }
}
