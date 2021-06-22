package com.kwsilence.apkviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
    setupActionBarWithNavController(navHostFragment.navController)
  }

  override fun onSupportNavigateUp(): Boolean {
    return findNavController(R.id.nav_host).navigateUp() || super.onNavigateUp()
  }
}
