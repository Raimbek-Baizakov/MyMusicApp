package com.example.mymusicapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistSelectionAdapter(
    private val playlists: List<String>,
    private val onPlaylistSelected: (String) -> Unit
) : RecyclerView.Adapter<PlaylistSelectionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.list_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = playlists[position]
        holder.itemView.setOnClickListener {
            onPlaylistSelected(playlists[position])
        }
    }

    override fun getItemCount() = playlists.size
}