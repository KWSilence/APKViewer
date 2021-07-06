package com.kwsilence.apkviewer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Application(
  val icon: ByteArray?,
  val name: String,
  val source: String
) : Parcelable
