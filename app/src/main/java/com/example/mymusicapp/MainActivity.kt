package com.example.mymusicapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonProfile = findViewById<Button>(R.id.buttonNextProfile)
        buttonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val buttonSettings = findViewById<Button>(R.id.buttonNextSettings)
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

//        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
//        val isRegistered = sharedPreferences.getBoolean("isRegistered", false)
//
//        if (!isRegistered) {
//            startActivity(Intent(this, SignUpActivity::class.java))
//            finish()
//            return
//        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Устанавливаем слушатель для навигации между фрагментами
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_playlists -> {
                    replaceFragment(PlaylistsFragment())
                    true
                }
                R.id.nav_favorites -> {
                    replaceFragment(FavoritesMusicListFragment())
                    true
                }
                R.id.nav_downloads -> {
                    replaceFragment(DownloadsMusicListFragment())
                    true
                }
                R.id.nav_categories -> {
                    replaceFragment(CategoryFragment())
                    true
                }
                else -> false
            }
        }

        // Устанавливаем начальный фрагмент
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }



    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun hideBottomNavigation() {
        bottomNavigationView.visibility = View.GONE
    }

    fun showBottomNavigation() {
        bottomNavigationView.visibility = View.VISIBLE
    }
}