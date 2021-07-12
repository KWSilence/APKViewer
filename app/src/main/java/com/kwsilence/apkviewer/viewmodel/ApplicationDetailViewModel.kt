package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwsilence.apkviewer.constant.Constant
import com.kwsilence.apkviewer.constant.Constant.WORK_DIR
import com.kwsilence.apkviewer.model.ApplicationDetail
import com.kwsilence.apkviewer.util.APKFileUtils.getApkPath
import com.kwsilence.apkviewer.util.PackageUtils
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import org.jf.baksmali.Baksmali
import org.jf.baksmali.BaksmaliOptions
import org.jf.dexlib2.DexFileFactory
import java.io.File

class ApplicationDetailViewModel(private val pm: PackageManager) : ViewModel() {

  fun oAppDetail(source: String): Single<ApplicationDetail?> =
    Single.create { sub -> sub.onSuccess(PackageUtils.sourceToApplicationDetail(pm, source)) }

  fun oAppManifest(source: String): Single<String?> =
    Single.create { sub -> sub.onSuccess(PackageUtils.sourceToManifest(pm, source)) }

  fun oAppResource(source: String): Single<ArrayList<String>?> =
    Single.create { sub -> sub.onSuccess(PackageUtils.sourceToResource(pm, source)) }

  fun oAppSmaliFile(source: String): Single<String> =
    Single.create { sub ->
      val folder = File("${WORK_DIR}/${source.split("/").last()}")
      if (!folder.exists())
        folder.mkdirs()
      clearDir(folder.absolutePath)
      val zip = ZipFile(getApkPath(pm, source))
      zip.fileHeaders.forEach { header ->
        val name = header.fileName
        if (name.endsWith(".dex"))
          zip.extractFile(header, folder.absolutePath)
      }
      val jobs = ArrayList<Job>()
      folder.listFiles { _, name -> name.endsWith(".dex") }?.sortedBy { it.name }?.forEach {
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
          Log.d(Constant.DEBUG_TAG, it.name)
          val outDir = File("${folder.absolutePath}/${it.name.split(".").first()}")
          Baksmali.disassembleDexFile(
            DexFileFactory.loadDexFile(it.absolutePath, null),
            outDir,
            2,
            BaksmaliOptions()
          )
          it.delete()
          Log.d(Constant.DEBUG_TAG, "end ${it.name}")
        })
      }
      viewModelScope.launch {
        jobs.forEach { it.join() }
        sub.onSuccess(folder.absolutePath)
      }
    }

  private fun clearDir(path: String, delete: Boolean = false) {
    val dir = File(path)
    if (dir.exists())
      if (delete)
        dir.deleteRecursively()
      else
        dir.listFiles()?.forEach { it.deleteRecursively() }
  }
}
