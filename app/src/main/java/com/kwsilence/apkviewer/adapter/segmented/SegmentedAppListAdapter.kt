package com.kwsilence.apkviewer.adapter.segmented

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kwsilence.apkviewer.databinding.ApplicationRowBinding
import com.kwsilence.apkviewer.databinding.SegmentHeaderBinding
import com.kwsilence.apkviewer.fragment.MainFragmentDirections
import com.kwsilence.apkviewer.util.BitmapUtils

class SegmentedAppListAdapter : RecyclerView.Adapter<SegmentedAppListAdapter.MyViewHolder>() {

  companion object {
    const val TYPE_HEADER = 0
    const val TYPE_ROW = 1
  }

  val readySetList = MutableLiveData(false)
  private var setCount = 0

  private var segments = SegmentAppList()

  class MyViewHolder(
    val headerBinding: SegmentHeaderBinding?,
    val rowBinding: ApplicationRowBinding?
  ) : RecyclerView.ViewHolder(headerBinding?.root ?: rowBinding!!.root)

  override fun getItemViewType(position: Int): Int {
    if (segments.getItem(position).displayedList.isNotEmpty())
      return TYPE_ROW
    return TYPE_HEADER
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val headerBinding =
      if (viewType == TYPE_HEADER) SegmentHeaderBinding.inflate(inflater, parent, false) else null
    val rowBinding =
      if (viewType == TYPE_ROW) ApplicationRowBinding.inflate(inflater, parent, false) else null
    return MyViewHolder(headerBinding, rowBinding)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val item = segments.getItem(position)
    if (item.displayedList.isNotEmpty()) {
      val currentApp = item.displayedList[0]
      val binding = holder.rowBinding!!
      binding.imgApp.setImageBitmap(BitmapUtils.byteArrayToBitmap(currentApp.icon))
      binding.nameApp.text = currentApp.name
      binding.sourceApp.text = currentApp.source
      binding.appRowLayout.setOnClickListener {
        val action = MainFragmentDirections.fromMainToDetails(currentApp)
        holder.itemView.findNavController().navigate(action)
      }
    } else {
      val binding = holder.headerBinding!!
      binding.segmentHeader.apply {
        text = item.title
        setOnClickListener {
          val segment = segments.getSegment(position)
          segment?.isCollapsed = !segment?.isCollapsed!!
          notifyDataSetChanged()
        }
      }
    }
  }

  override fun getItemCount(): Int {
    return segments.getCount()
  }

  fun addSegment(segment: SegmentApp) {
    segments.addSegment(segment)
  }

  fun setSegment(index: Int, segment: SegmentApp) {
    segments.setSegment(index, segment)
    if (--setCount == 0) {
      notifyDataSetChanged()
      readySetList.value = true
    }
  }

  fun initSize(size: Int) {
    setCount = size
    segments.initSize(size)
  }

  fun filter(constraint: String?) {
    segments.filter(constraint)
    notifyDataSetChanged()
  }
}
