package com.kwsilence.apkviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kwsilence.apkviewer.databinding.InfoRowBinding
import com.kwsilence.apkviewer.helper.AppInfoHelper
import java.util.*
import kotlin.collections.ArrayList

class AppInfoListAdapter : RecyclerView.Adapter<AppInfoListAdapter.MyViewHolder>() {

  private var list = ArrayList<HashMap<String, String?>>()

  class MyViewHolder(val binding: InfoRowBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = InfoRowBinding.inflate(inflater, parent, false)
    return MyViewHolder(binding)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val currentInfo = list[position]
    if (currentInfo[AppInfoHelper.APP_INFO_ROW_NAME] != null)
      holder.binding.infoContent.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
    else
      holder.binding.infoContent.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
    holder.binding.infoName.text = currentInfo[AppInfoHelper.APP_INFO_ROW_NAME]
    holder.binding.infoContent.text = currentInfo[AppInfoHelper.APP_INFO_ROW_CONTENT]
  }

  override fun getItemCount(): Int {
    return list.size
  }

  fun setData(list: List<HashMap<String, String?>>) {
    this.list.clear()
    this.list.addAll(list)
    notifyDataSetChanged()
  }
}
