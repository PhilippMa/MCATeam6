package com.example.mcateam6.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mcateam6.database.RemoteDatabase
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import com.example.mcateam6.R
import com.example.mcateam6.activities.ProductInfoActivity


class SearchItemAdapter constructor(val context: Context?, var itemList: MutableList<RemoteDatabase.FirebaseProduct>?) : RecyclerView.Adapter<SearchItemAdapter.ViewHolder>() {
    companion object {
        val VEGAN = "VEGAN"
        val VEGETARIAN = "VEGETARIAN"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context

        val view = LayoutInflater.from(context).inflate(R.layout.search_listview_item, null)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList?.get(position)

        holder.tvNameKorean.text = item?.name_korean
        holder.tvNameEnglish.text = item?.name_english

        // TODO()
        val attribute = item?.attributes
        val vegan:Boolean = attribute?.get(VEGAN)?.toBoolean() ?: false
        val vegetarian: Boolean = attribute?.get(VEGETARIAN)?.toBoolean() ?: false
        if (vegan) {
            holder.ivAttribute.setBackgroundColor(Color.parseColor("#006400"))
        } else if (vegetarian) {
            holder.ivAttribute.setBackgroundColor(Color.parseColor("#90ee90"))
        } else {
            holder.ivAttribute.setBackgroundColor(Color.parseColor("#d3d3d3"))
        }
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                val pos = adapterPosition
                val item = itemList?.get(pos)

                val intent = Intent(context, ProductInfoActivity::class.java).apply {
                    putExtra("englishName", item?.name_english)
                    putExtra("koreanName", item?.name_korean)
                    putExtra("description", item?.description)
                }
                context?.startActivity(intent)
            }
        }
        var tvNameKorean: TextView = view.findViewById(R.id.tv_name_korean)
        var tvNameEnglish: TextView = view.findViewById(R.id.tv_name_english)
        var ivAttribute: ImageView = view.findViewById(R.id.iv_attribute)
    }

    class ItemDiffCallback(var oldItemList: List<RemoteDatabase.FirebaseProduct>?, var newItemList: List<RemoteDatabase.FirebaseProduct>?): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItemList?.get(oldItemPosition)?.id.equals(newItemList?.get(newItemPosition)?.id)
        }

        override fun getOldListSize(): Int {
            return oldItemList?.size ?: 0
        }

        override fun getNewListSize(): Int {
            return newItemList?.size ?: 0
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItemList?.get(oldItemPosition)?.name_korean.equals(newItemList?.get(newItemPosition)?.name_korean) &&
                    oldItemList?.get(oldItemPosition)?.name_english.equals(newItemList?.get(newItemPosition)?.name_english)
        }

    }
    fun updateItemList (newList: List<RemoteDatabase.FirebaseProduct>?) {
        val diffCallback = ItemDiffCallback(this.itemList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.itemList?.clear()
        this.itemList?.addAll(newList as Iterable<RemoteDatabase.FirebaseProduct>)
        diffResult.dispatchUpdatesTo(this)
    }
    fun updateWholeData(newList: List<RemoteDatabase.FirebaseProduct>?) {
        itemList?.clear()
        itemList?.addAll(newList as MutableList)

        this.notifyDataSetChanged()
    }

}