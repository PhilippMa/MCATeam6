package com.example.mcateam6.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.mcateam6.R
import com.example.mcateam6.activities.ProductInfoActivity
import com.example.mcateam6.database.RemoteDatabase

class SearchItemListAdapter(val context: Context?, var itemList: List<RemoteDatabase.FirebaseProduct>?): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = itemList?.get(position)
        val view: View
        val holder: ViewHolder

        view = LayoutInflater.from(context).inflate(R.layout.search_listview_item, null)
        view.setOnClickListener {
            val intent = Intent(context, ProductInfoActivity::class.java).apply {
                putExtra("englishName", item?.name_english)
                putExtra("koreanName", item?.name_korean)
                putExtra("description", item?.description)
            }
            context?.startActivity(intent)
        }
        holder = ViewHolder(view)
        holder.tvNameKorean.text = item?.name_korean
        holder.tvNameEnglish.text = item?.name_english
        view.tag = holder
        return view
    }

    override fun getItem(position: Int): Any {
        return itemList?.get(position) as Any
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return itemList?.size ?: 0
    }
    private class ViewHolder(view: View) {
        var tvNameKorean: TextView = view.findViewById(R.id.tv_name_korean)
        var tvNameEnglish: TextView = view.findViewById(R.id.tv_name_english)
        var ivAttribute: ImageView = view.findViewById(R.id.iv_attribute)
    }
    fun updateWholeData(data: List<RemoteDatabase.FirebaseProduct>?) {
        itemList = data
        this.notifyDataSetInvalidated()
    }
}