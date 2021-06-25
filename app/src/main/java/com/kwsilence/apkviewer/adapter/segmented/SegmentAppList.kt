package com.kwsilence.apkviewer.adapter.segmented

import com.kwsilence.apkviewer.model.Application

class SegmentAppList {
  private val segments = ArrayList<SegmentApp>()

  fun getItem(pos: Int): SegmentApp {
    var nPos = pos
    segments.forEach {
      if (nPos == 0) {
        return SegmentApp(it.title, ArrayList())
      }
      --nPos
      if (!it.isCollapsed) {
        if (nPos - it.displayedList.size >= 0 || it.displayedList.size == 0) {
          nPos -= it.displayedList.size
        } else {
          val app = it.displayedList[nPos]
          val list = ArrayList<Application>().apply { add(app) }
          return SegmentApp("", list)
        }
      }
    }
    return SegmentApp("", ArrayList())
  }

  fun getCount(): Int {
    var count = segments.size
    segments.forEach { count += if (it.isCollapsed) 0 else it.displayedList.size }
    return count
  }

  fun addSegment(segment: SegmentApp) {
    if (segment.displayedList.isNotEmpty())
      segments.add(segment)
  }

  fun setSegment(index: Int, segment: SegmentApp) {
    if (segment.displayedList.isNotEmpty())
      segments[index] = segment
  }

  fun initSize(size: Int) {
    for (i in 1..size)
      segments.add(SegmentApp("", ArrayList()))
  }

  fun getSegment(pos: Int): SegmentApp? {
    var nPos = pos
    segments.forEach {
      if (nPos == 0) {
        return it
      }
      --nPos
      if (!it.isCollapsed)
        nPos -= it.displayedList.size
    }
    return null
  }

  fun filter(constraint: String?) {
    segments.forEach { it.filter(constraint) }
  }
}
