package com.kwsilence.apkviewer.adapter.segmented

import com.kwsilence.apkviewer.model.Application
import java.util.*
import kotlin.collections.ArrayList

class SegmentApp(
  private val name: String,
  private val fullList: ArrayList<Application>
) {
  val displayedList = ArrayList<Application>(fullList)
  var title: String = name
  var isCollapsed = false
    set(value) {
      title = if (value) {
        "[ $name ]"
      } else {
        name
      }
      field = value
    }

  fun filter(constraint: String?) {
    val nConstraint = constraint?.trim()?.lowercase(Locale.getDefault())
    displayedList.clear()
    if (nConstraint == null || nConstraint.isEmpty()) {
      displayedList.addAll(fullList)
    } else {
      fullList.filterTo(
        displayedList,
        {
          it.name.lowercase(Locale.getDefault()).contains(nConstraint) ||
              if (it.source.endsWith(".apk")) false else
                it.source.lowercase(Locale.getDefault()).contains(nConstraint)
        })
    }
  }
}