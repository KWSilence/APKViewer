package com.kwsilence.apkviewer.helper

object ProviderHelper {
  const val CONTENT_PATH = "content://"
  const val PROVIDER_AUTHORITY = "com.kwsilence.apkviewer.provider"
  const val PROVIDER_PATH = "${CONTENT_PATH}${PROVIDER_AUTHORITY}"

  const val GET_ALL_APPLICATIONS = "/application"
  const val GET_USER_APPLICATIONS = "/application/user"
  const val GET_SYSTEM_APPLICATIONS = "/application/system"

  const val GET_ALL_DISK_APK_FILES = "/disk"

  const val FIELD_NAME = "name"
  const val FIELD_SOURCE = "source"
  const val FIELD_ICON = "icon"
}
