package com.kwsilence.apkviewer.adapter.segmented

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kwsilence.apkviewer.R
import com.kwsilence.apkviewer.fragment.MainFragmentDirections

class SegmentedAppListAdapter : RecyclerView.Adapter<SegmentedAppListAdapter.MyViewHolder>() {

  companion object {
    const val TYPE_HEADER = 0
    const val TYPE_ROW = 1
  }

  val readySetList = MutableLiveData(false)
  private var setCount = 0

  private var segments = SegmentAppList()

  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

  override fun getItemViewType(position: Int): Int {
    if (segments.getItem(position).displayedList.isNotEmpty())
      return TYPE_ROW
    return TYPE_HEADER
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val layout = when (viewType) {
      TYPE_HEADER -> R.layout.segment_header
      TYPE_ROW -> R.layout.application_row
      else -> R.layout.application_row
    }
    val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
    return MyViewHolder(view)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val item = segments.getItem(position)
    if (item.displayedList.isNotEmpty()) {
      val currentApp = item.displayedList[0]
      holder.itemView.findViewById<ImageView>(R.id.img_app).setImageDrawable(currentApp.icon)
      holder.itemView.findViewById<TextView>(R.id.name_app).text = currentApp.name
      holder.itemView.findViewById<TextView>(R.id.source_app).text = currentApp.source
      holder.itemView.findViewById<ConstraintLayout>(R.id.app_row_layout).setOnClickListener {
        val action = MainFragmentDirections.fromMainToDetails(currentApp.source)
        holder.itemView.findNavController().navigate(action)
      }
    } else {
      holder.itemView.findViewById<TextView>(R.id.segment_header).text = item.title
      holder.itemView.findViewById<TextView>(R.id.segment_header).setOnClickListener {
        val segment = segments.getSegment(position)
        segment?.isCollapsed = !segment?.isCollapsed!!
        notifyDataSetChanged()
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
