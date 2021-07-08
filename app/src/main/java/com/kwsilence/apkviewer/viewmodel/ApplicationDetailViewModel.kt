package com.kwsilence.apkviewer.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import com.kwsilence.apkviewer.model.ApplicationDetail
import com.kwsilence.apkviewer.util.PackageUtils
import io.reactivex.rxjava3.core.Single

class ApplicationDetailViewModel(private val pm: PackageManager) : ViewModel() {

  fun oAppDetail(source: String): Single<ApplicationDetail?> =
    Single.create { sub -> sub.onSuccess(PackageUtils.sourceToApplicationDetail(pm, source)) }

  fun oAppManifest(source: String): Single<String?> =
    Single.create { sub -> sub.onSuccess(PackageUtils.sourceToManifest(pm, source)) }

  fun oAppResource(source: String): Single<ArrayList<String>?> =
    Single.create { sub -> sub.onSuccess(PackageUtils.sourceToResource(pm, source)) }
}
