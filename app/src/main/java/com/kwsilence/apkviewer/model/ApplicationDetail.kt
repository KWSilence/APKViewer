package com.kwsilence.apkviewer.model

data class ApplicationDetail(
  val packageName: String?,
  val version: String?,
  val size: String?,
  val apkFile: String?,
  val dataPath: String?,
  val installDate: String?,
  val updateDate: String?,
  val certificate: String?
)
