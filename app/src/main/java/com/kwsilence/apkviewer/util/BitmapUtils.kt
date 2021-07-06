package com.kwsilence.apkviewer.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.ByteArrayOutputStream

class BitmapUtils {
  companion object {

    fun drawableToBitmap(drawable: Drawable): Bitmap {
      if (drawable is BitmapDrawable) {
        return drawable.bitmap
      }

      var width = drawable.intrinsicWidth
      width = if (width > 0) width else 1
      var height = drawable.intrinsicHeight
      height = if (height > 0) height else 1

      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      drawable.setBounds(0, 0, canvas.width, canvas.height)
      drawable.draw(canvas)

      return bitmap
    }

    fun pngBitmapToByteArray(bitmap: Bitmap?): ByteArray? {
      if (bitmap == null)
        return null
      val baos = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
      val array = baos.toByteArray()
      baos.close()
      return array
    }

    fun byteArrayToBitmap(src: ByteArray?): Bitmap? =
      if (src != null) BitmapFactory.decodeByteArray(src, 0, src.size) else null
  }
}
