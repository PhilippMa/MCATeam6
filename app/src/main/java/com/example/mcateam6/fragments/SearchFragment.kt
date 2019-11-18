package com.example.mcateam6.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mcateam6.R
import com.example.mcateam6.adapters.SearchItemAdapter
import com.example.mcateam6.database.RemoteDatabase
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Tag
import com.google.android.gms.tasks.Task
import com.mancj.materialsearchbar.MaterialSearchBar
import com.yalantis.filter.adapter.FilterAdapter
import com.yalantis.filter.widget.FilterItem
import kotlinx.android.synthetic.main.fragment_search.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mcateam6.activities.MainActivity
import com.yalantis.filter.animator.FiltersListItemAnimator
import com.yalantis.filter.listener.FilterListener
import com.yalantis.filter.widget.Filter
import java.util.ArrayList


class SearchFragment : Fragment(), PopupMenu.OnMenuItemClickListener, MaterialSearchBar.OnSearchActionListener, FilterListener<Tag> {
    override fun onFilterDeselected(item: Tag) {
    }

    override fun onFilterSelected(item: Tag) {
    }

    override fun onFiltersSelected(filters: ArrayList<Tag>) {
        var newItemList = mutableListOf<RemoteDatabase.FirebaseProduct>()
        mSearchedResult?.forEach {
            for (tag in filters) {
                val b = it.attributes.get(tag.getText())?.toBoolean() ?: false
                if (b) {
                    newItemList.add(it)
                }
            }
        }
        itemAdapter.updateItemList(newItemList)
    }

    override fun onNothingSelected() {
        itemAdapter.updateItemList(mSearchedResult)
    }

    val db = RemoteDatabase()
    private val ITEM_ALL = 0
    private val ITEM_KOREAN = 1
    private val ITEM_ENGLISH = 2

    var itemList: MutableList<RemoteDatabase.FirebaseProduct>? = mutableListOf()
    lateinit var itemAdapter: SearchItemAdapter

    lateinit var recyclerView: RecyclerView
    lateinit var tvNoItem: TextView
    lateinit var chipGroup: ChipGroup
    lateinit var mFilter: Filter<Tag>

    var mSearchedResult: List<RemoteDatabase.FirebaseProduct>? = listOf()

    var mTitles: MutableList<String>  = mutableListOf()
    var mColors: IntArray = intArrayOf()

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
        recyclerView.layoutManager = GridLayoutManager(activity, 1)

        tvNoItem = v.findViewById(R.id.tv_no_item)

        mFilter = v.findViewById(R.id.filter)
        initialFilter()
        return v
    }
    fun initialFilter() {
        mColors = resources.getIntArray(R.array.filter_color)
        var idx = 0
        var tags: MutableList<Tag> = mutableListOf()
        Attribute.values().forEach {
            mTitles.add(it.name)
            tags.add(Tag(it.name, mColors[idx++]))
        }

        mFilter.adapter = Adapter(tags)
        mFilter.listener = this

        mFilter.build()
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
                        mSearchedResult = it.result
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
    inner class Adapter(items: List<Tag>): FilterAdapter<Tag>(items) {
        override fun createView(position: Int, item: Tag): FilterItem {
            val context = activity as MainActivity
            val filterItem = FilterItem(context)

            filterItem.strokeColor = android.R.color.black
            filterItem.textColor = android.R.color.black
            filterItem.cornerRadius = 14F
            filterItem.checkedTextColor =
                ContextCompat.getColor(context, android.R.color.white)
            filterItem.color = ContextCompat.getColor(context, android.R.color.white)
            filterItem.checkedColor = mColors[position]
            filterItem.text = item.getText()
            filterItem.deselect()

            return filterItem
        }

    }
}