package com.kwsilence.apkviewer.helper

import android.content.UriMatcher

object ProviderHelper {
  const val CONTENT_PATH = "content://"
  const val PROVIDER_AUTHORITY = "com.kwsilence.apkviewer.provider"
  const val PROVIDER_PATH = "${CONTENT_PATH}${PROVIDER_AUTHORITY}"

  const val PATH_GET_ALL_APPLICATIONS = "/application"
  const val PATH_GET_ALL_DISK_APK_FILES = "/disk"
  const val PATH_GET_USER_APPLICATIONS = "/application/user"
  const val PATH_GET_SYSTEM_APPLICATIONS = "/application/system"

  //in detail use selectionArgs to set package or apk file
  const val PATH_GET_DETAIL_INFO = "/detail/info"
  const val PATH_GET_DETAIL_MANIFEST = "/detail/manifest"
  const val PATH_GET_DETAIL_RESOURCE = "/detail/resource"

  const val URI_ALL_APPLICATIONS = 1
  const val URI_USER_APPLICATIONS = 2
  const val URI_SYSTEM_APPLICATIONS = 3
  const val URI_DISK_APKS = 4
  const val URI_DETAIL_INFO = 5
  const val URI_DETAIL_MANIFEST = 6
  const val URI_DETAIL_RESOURCE = 7

  val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
    addURI(PROVIDER_AUTHORITY, PATH_GET_ALL_APPLICATIONS, URI_ALL_APPLICATIONS)
    addURI(PROVIDER_AUTHORITY, PATH_GET_USER_APPLICATIONS, URI_USER_APPLICATIONS)
    addURI(PROVIDER_AUTHORITY, PATH_GET_SYSTEM_APPLICATIONS, URI_SYSTEM_APPLICATIONS)
    addURI(PROVIDER_AUTHORITY, PATH_GET_ALL_DISK_APK_FILES, URI_DISK_APKS)
    addURI(PROVIDER_AUTHORITY, PATH_GET_DETAIL_INFO, URI_DETAIL_INFO)
    addURI(PROVIDER_AUTHORITY, PATH_GET_DETAIL_MANIFEST, URI_DETAIL_MANIFEST)
    addURI(PROVIDER_AUTHORITY, PATH_GET_DETAIL_RESOURCE, URI_DETAIL_RESOURCE)
  }

  const val FIELD_NAME = "name"
  const val FIELD_SOURCE = "source"
  val COLUMNS_APPLICATION = arrayOf(FIELD_NAME, FIELD_SOURCE, "icon")

  val COLUMNS_DETAIL_INFO = arrayOf(
    "package_name",
    "version",
    "apk_file",
    "data_path",
    "install_date",
    "update_date",
    "apk_size",
    "certificate"
  )

  const val FIELD_MANIFEST = "manifest"
  const val FIELD_RESOURCE = "resource"
}
