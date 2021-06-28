package com.kwsilence.apkviewer

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.kwsilence.apkviewer.constant.Constant
import java.io.File

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
    setupActionBarWithNavController(navHostFragment.navController)

    val filesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.parent
    Constant.TMP_DIR = "${filesDir}${File.separator}Tmp"
  }

  override fun onSupportNavigateUp(): Boolean {
    return findNavController(R.id.nav_host).navigateUp() || super.onNavigateUp()
  }
}
