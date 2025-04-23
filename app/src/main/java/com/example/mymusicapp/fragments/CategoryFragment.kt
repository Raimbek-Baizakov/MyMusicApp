package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class CategoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonNextCategoriesMusicList = view.findViewById<Button>(R.id.buttonNextCategoryMusicList)
        buttonNextCategoriesMusicList.setOnClickListener {
            val intent = Intent(requireActivity(), CategoriesMusicListActivity::class.java)
            startActivity(intent)
        }
    }
}