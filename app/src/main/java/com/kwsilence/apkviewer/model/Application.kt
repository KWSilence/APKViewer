package com.kwsilence.apkviewer.model

import android.graphics.drawable.Drawable

data class Application(
  val icon: Drawable,
  val name: String,
  val packageName: String
)
