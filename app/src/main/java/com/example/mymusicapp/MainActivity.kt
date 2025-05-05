package com.example.mymusicapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получаем SharedPreferences
        val sharedPreferences = getSharedPreferences("MyMusicAppPrefs", MODE_PRIVATE)

        // Проверяем, первый ли это запуск после установки
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            // Сбрасываем isVerified в false при первом запуске
            sharedPreferences.edit().putBoolean("isVerified", false).apply()
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }

        val isVerified = sharedPreferences.getBoolean("isVerified", false)

        if (!isVerified) {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

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
