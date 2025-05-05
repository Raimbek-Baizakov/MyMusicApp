package com.example.mymusicapp

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mymusicapp.utils.ImageLoader
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), TrackPlayerService.OnPlayerEventListener {


    // Метод для перехода в настройки
    fun onSettingsClick(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun onSearchClick(view: View) {
        val intent = Intent(this, SearchMusicListActivity::class.java)
        startActivity(intent)

        // Опционально: добавить анимацию
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // Метод для обработки клика на профиле
    fun onAccountClick(view: View) {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var currentFragmentText: TextView

    // Player UI elements
    private lateinit var playerView: View
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var songTitle: TextView
    private lateinit var songArtist: TextView
    private lateinit var songImage: ImageView
    private var isPlayerVisible: Boolean = false


    // Player service and ViewModel
    private lateinit var playerService: TrackPlayerService
    private lateinit var viewModel: TrackViewModel
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val REQUEST_PERMISSION_CODE = 101
    }

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

        // Инициализируем элементы
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        currentFragmentText = findViewById(R.id.currentFragmentText)

        // Инициализируем плеер
        setupPlayer()

        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this)[TrackViewModel::class.java]

        // Инициализируем сервис плеера
        playerService = TrackPlayerService()

        // Наблюдаем за состоянием плеера
        observePlayerState()

        // Устанавливаем слушатель для навигации между фрагментами
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment(), "Home")
                    true
                }
                R.id.nav_playlists -> {
                    replaceFragment(PlaylistsFragment(), "Playlists")
                    true
                }
                R.id.nav_favorites -> {
                    replaceFragment(FavoritesMusicListFragment(), "Favorites")
                    true
                }
                R.id.nav_downloads -> {
                    replaceFragment(DownloadsMusicListFragment(), "Downloads")
                    true
                }
                R.id.nav_categories -> {
                    replaceFragment(CategoryFragment(), "Category")
                    true
                }
                else -> false
            }
        }

        // Устанавливаем начальный фрагмент
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), "Home")
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }

    private fun setupPlayer() {
        // Ищем ViewGroup, в который будем добавлять плеер
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        // Размещаем плеер программно
        playerView = LayoutInflater.from(this).inflate(R.layout.player_controls, rootView, false)

        // Делаем плеер невидимым по умолчанию
        playerView.visibility = View.GONE

        // Добавляем плеер в rootView
        rootView.addView(playerView)

        // Инициализируем элементы плеера
        btnPlayPause = playerView.findViewById(R.id.btn_play_pause)
        btnNext = playerView.findViewById(R.id.btn_next)
        btnPrevious = playerView.findViewById(R.id.btn_previous)
        songTitle = playerView.findViewById(R.id.song_title)
        songArtist = playerView.findViewById(R.id.song_artist)
        songImage = playerView.findViewById(R.id.song_image)

        // Настройка слушателей
        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnNext.setOnClickListener { playNext() }
        btnPrevious.setOnClickListener { playPrevious() }
    }


    private fun observePlayerState() {
        // Наблюдаем за текущим треком
        viewModel.currentTrack.observe(this) { track ->
            track?.let { updatePlayerUI(it) }
        }

        // Наблюдаем за состоянием воспроизведения
        viewModel.isPlaying.observe(this) { isPlaying ->
            updatePlayPauseButton(isPlaying)
        }

        // Наблюдаем за состоянием воспроизведения из сервиса
        playerService.isPlaying.observe(this) { isPlaying ->
            viewModel.setIsPlaying(isPlaying)
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        // Обновляем текст заголовка
        updateFragmentTitle(title)
    }

    private fun updateFragmentTitle(title: String) {
        currentFragmentText.text = title
    }

    fun hideBottomNavigation() {
        bottomNavigationView.visibility = View.GONE
    }

    fun showBottomNavigation() {
        bottomNavigationView.visibility = View.VISIBLE
    }

    // Функции для работы с плеером
    fun playTrack(track: Track, source: String = viewModel.playlistSource.value ?: "home") {
        if (!checkAndRequestPermissions()) {
            return
        }

        viewModel.setCurrentTrack(track)

        // Устанавливаем источник плейлиста, если изменился
        if (source != viewModel.playlistSource.value) {
            viewModel.loadTracks(source)
        }

        playerService.playTrack(this, track, this)
    }

    private fun updatePlayerUI(track: Track) {
        songTitle.text = track.title
        songArtist.text = track.author
        ImageLoader.loadTrackImage(songImage, track.imagePath, this) // ← Вот так!
        showPlayer()
    }

    private fun togglePlayPause() {
        playerService.togglePlayPause()
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun playNext() {
        viewModel.getNextTrack()?.let { nextTrack ->
            playTrack(nextTrack)
        }
    }

    private fun playPrevious() {
        viewModel.getPreviousTrack()?.let { prevTrack ->
            playTrack(prevTrack)
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), REQUEST_PERMISSION_CODE)
                false
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
                false
            }
        }
    }

    private fun showPlayer() {
        if (!isPlayerVisible) {
            playerView.visibility = View.VISIBLE
            isPlayerVisible = true
        }
    }

    private fun hidePlayer() {
        if (isPlayerVisible) {
            playerView.visibility = View.GONE
            isPlayerVisible = false
        }
    }

    // Callbacks от TrackPlayerService
    override fun onPrepared() {
        viewModel.setIsPlaying(true)
    }

    override fun onCompletion() {
        viewModel.setIsPlaying(false)
        playNext()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerService.release()
    }
}