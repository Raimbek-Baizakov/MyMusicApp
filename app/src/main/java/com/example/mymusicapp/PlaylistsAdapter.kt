package com.example.mymusicapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

/**
 * Адаптер для отображения списка плейлистов и кнопки добавления.
 * Поддерживает два типа элементов:
 * 1. Плейлист (TYPE_PLAYLIST)
 * 2. Кнопка добавления (TYPE_ADD_BUTTON)
 */
class PlaylistsAdapter(
    private val playlists: MutableList<String>,
    private val onPlaylistClick: (String) -> Unit,
    private val onAddButtonClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PLAYLIST = 0
        private const val TYPE_ADD_BUTTON = 1

        // Цвета для разных плейлистов (можно вынести в ресурсы)
        private val playlistColors = mapOf(
            "My playlist 1" to Color.parseColor("#FF6F61"), // Коралловый
            "My playlist 2" to Color.parseColor("#FFD700"), // Золотой
            "My playlist 3" to Color.parseColor("#4CAF50"), // Зеленый
            "My playlist 4" to Color.parseColor("#1E88E5"), // Синий
            "My playlist 5" to Color.parseColor("#8E24AA"), // Фиолетовый
            "My playlist 6" to Color.parseColor("#FFAB40")  // Оранжевый
        )
    }

    /**
     * Определяем тип элемента по позиции
     */
    override fun getItemViewType(position: Int): Int {
        return if (position < playlists.size) TYPE_PLAYLIST else TYPE_ADD_BUTTON
    }

    /**
     * Создание ViewHolder'а в зависимости от типа
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PLAYLIST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_playlist, parent, false)
                PlaylistViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_button, parent, false)
                AddButtonViewHolder(view)
            }
        }
    }

    /**
     * Привязка данных к ViewHolder'у
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlaylistViewHolder -> {
                val playlist = playlists[position]
                holder.bind(playlist)
                holder.cardView.setOnClickListener { onPlaylistClick(playlist) }
            }
            is AddButtonViewHolder -> {
                holder.bind()
                holder.cardView.setOnClickListener { onAddButtonClick() }
            }
        }
    }

    /**
     * Общее количество элементов (+1 для кнопки добавления)
     */
    override fun getItemCount(): Int = playlists.size + 1

    /**
     * Добавление нового плейлиста
     */
    fun addPlaylist(name: String) {
        playlists.add(name)
        notifyItemInserted(playlists.size - 1) // Оптимизация вместо notifyDataSetChanged()
    }

    /**
     * ViewHolder для элемента плейлиста
     */
    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardPlaylist)
        private val tvPlaylistName: TextView = itemView.findViewById(R.id.tvPlaylistName)

        fun bind(playlistName: String) {
            tvPlaylistName.text = playlistName
            cardView.setCardBackgroundColor(
                playlistColors[playlistName] ?: Color.GRAY // Серый по умолчанию
            )
        }
    }

    /**
     * ViewHolder для кнопки добавления
     */
    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardAddPlaylist)
        private val btnAdd: TextView = itemView.findViewById(R.id.btnAddPlaylist)

        fun bind() {
            btnAdd.text = "+" // Можно заменить на иконку
        }
    }
}