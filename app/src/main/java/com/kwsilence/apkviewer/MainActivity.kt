package com.kwsilence.apkviewer

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.kwsilence.apkviewer.constant.Constant

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
    setupActionBarWithNavController(navHostFragment.navController)

    Constant.WORK_DIR = "${Environment.getExternalStorageDirectory().absolutePath}/APKViewer"
  }

  override fun onSupportNavigateUp(): Boolean {
    return findNavController(R.id.nav_host).navigateUp() || super.onNavigateUp()
  }
}
