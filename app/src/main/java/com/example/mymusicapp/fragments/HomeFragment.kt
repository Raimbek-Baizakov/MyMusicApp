package com.example.mymusicapp

import SongAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter // Убрали generic параметр

    private val musicList = mutableListOf(
        Song(
            1,
            "Song 1",
            "Author 1",
            "3:30",
            "song1.png",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            2,
            "Song 2",
            "Author 2",
            "4:00",
            "song2.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            3,
            "Song 3",
            "Author 3",
            "2:50",
            "song3.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            4,
            "Song 4",
            "Author 4",
            "3:10",
            "song4.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            5,
            "Song 5",
            "Author 5",
            "4:20",
            "song5.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            6,
            "Song 6",
            "Author 6",
            "3:45",
            "song6.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            7,
            "Song 7",
            "Author 7",
            "2:55",
            "song7.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            8,
            "Song 8",
            "Author 8",
            "4:05",
            "song8.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            9,
            "Song 9",
            "Author 9",
            "3:25",
            "song9.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        ),
        Song(
            10,
            "Song 10",
            "Author 10",
            "3:35",
            "song10.jpg",
            downloaded = false,
            favorite = false,
            playlistName = null
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = SongAdapter(
            songList = musicList,
            onFavoriteClick = { position ->
                val updatedSong = musicList[position].copy(
                    favorite = !musicList[position].favorite
                )
                musicList[position] = updatedSong // Обновляем элемент в списке
                adapter.notifyItemChanged(position) // Уведомляем адаптер об изменении
            },
            onDownloadClick = { position ->
                val updatedSong = musicList[position].copy(
                    downloaded = !musicList[position].downloaded
                )
                musicList[position] = updatedSong // Обновляем элемент в списке
                adapter.notifyItemChanged(position) // Уведомляем адаптер об изменении
            }
        )

        recyclerView.adapter = adapter
        return view
    }
}