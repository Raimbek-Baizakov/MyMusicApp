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
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Проверка подключения к API
        testApiConnection()

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

    private fun testApiConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Вариант 1: Проверка соединения
                val testResponse = RetrofitClient.instance.testConnection()

                // Вариант 2: Или сразу получаем пользователей
                // val response = RetrofitClient.instance.getUsers()

                withContext(Dispatchers.Main) {
                    if (testResponse.isSuccessful) {
                        Log.d("API_TEST", "Сервер доступен")
                        Toast.makeText(
                            this@MainActivity,
                            "Соединение с сервером установлено",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Дополнительно можно загрузить пользователей
                        loadUsers()
                    } else {
                        showError("Ошибка сервера: ${testResponse.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Сетевая ошибка: ${e.message}")
                }
            }
        }
    }

    private fun loadUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getUsers()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val users = response.body()
                        Log.d("API_TEST", "Получено ${users?.size ?: 0} пользователей")
                        // Здесь можно обновить UI с полученными данными
                    } else {
                        showError("Ошибка при загрузке пользователей")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка загрузки: ${e.message}")
                }
            }
        }
    }

    private fun showError(message: String) {
        Log.e("API_TEST", message)
        Toast.makeText(
            this@MainActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
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