package com.example.mcateam6.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mcateam6.R
import com.example.mcateam6.adapters.SearchItemAdapter
import com.example.mcateam6.database.RemoteDatabase
import com.google.android.gms.tasks.Task
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.fragment_search.*


class SearchFragment : Fragment(), PopupMenu.OnMenuItemClickListener, MaterialSearchBar.OnSearchActionListener {
    val db = RemoteDatabase()
    private val ITEM_ALL = 0
    private val ITEM_KOREAN = 1
    private val ITEM_ENGLISH = 2

    var itemList: MutableList<RemoteDatabase.FirebaseProduct>? = mutableListOf()
    lateinit var itemAdapter: SearchItemAdapter

    lateinit var recyclerView: RecyclerView
    lateinit var tvNoItem: TextView

    var itemIndex = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_search, container, false)
        val searchBar = v.findViewById<MaterialSearchBar>(R.id.searchBar)
        searchBar.setOnSearchActionListener(this)
        searchBar.inflateMenu(R.menu.search_item_menu)
        searchBar.menu.setOnMenuItemClickListener(this)

        recyclerView = v.findViewById(R.id.recycler_view)

        itemAdapter = SearchItemAdapter(context, itemList)
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        tvNoItem = v.findViewById(R.id.tv_no_item)

        return v
    }
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.item_all -> {
                searchBar.setHint("All")
                itemIndex = ITEM_ALL
            }
            R.id.item_korean -> {
                searchBar.setHint("Korean")
                itemIndex = ITEM_KOREAN
            }
            R.id.item_english -> {
                searchBar.setHint("English")
                itemIndex = ITEM_ENGLISH
            }
        }
        return false
    }
    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            MaterialSearchBar.BUTTON_BACK -> {
                searchBar.disableSearch()
            }
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        return
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        var task: Task<List<RemoteDatabase.FirebaseProduct>>? = null
        when (itemIndex) {
            ITEM_ALL -> {
                task = db.searchAll(text.toString().trim())
            }
            ITEM_KOREAN -> {
                task = db.searchKorean(text.toString().trim())
            }
            ITEM_ENGLISH -> {
                task = db.searchEnglish(text.toString().trim())
            }
        }
        task?.addOnCompleteListener{
            if (it.isSuccessful) {
                when (it.result?.isEmpty()) {
                    true -> {
                        setNoItemMode()
                    }
                    false -> {
                        setItemMode()
                        itemAdapter.updateWholeData(it.result)
                    }
                }
            } else {
                Toast.makeText(context, "Fail to load search list", Toast.LENGTH_SHORT)
            }
        }
    }
    private fun setNoItemMode() {
        tvNoItem.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    private fun setItemMode() {
        tvNoItem.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}