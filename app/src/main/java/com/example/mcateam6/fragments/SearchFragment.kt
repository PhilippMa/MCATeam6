package com.example.mcateam6.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.mcateam6.R
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.fragment_search.*


class SearchFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search, container, false)
        val searchBar = v.findViewById<MaterialSearchBar>(R.id.searchBar)
        searchBar.inflateMenu(R.menu.search_item_menu)
        searchBar.menu.setOnMenuItemClickListener(this)
        return v
    }
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.item1 -> {
                searchBar.setHint("Korean")
            }
            R.id.item2 -> {
                searchBar.setHint("English")
            }
        }
        return false
    }
}