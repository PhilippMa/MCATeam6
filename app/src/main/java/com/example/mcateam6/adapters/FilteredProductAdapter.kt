package com.example.mcateam6.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mcateam6.R
import com.example.mcateam6.database.RemoteDatabase
import com.example.mcateam6.datatypes.Product
import java.util.*
import kotlin.collections.ArrayList

class FilteredProductAdapter(
    val context: Context,
    var productList: List<RemoteDatabase.FirebaseProduct>,
    val listener: FilteredProductAdapterListener
) : RecyclerView.Adapter<FilteredProductAdapter.ProductViewHolder>(), Filterable {

    var productListFiltered = productList

    inner class ProductViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.en_kr_name_text)
        val brandText: TextView = view.findViewById(R.id.brand_text)

        init {
            view.setOnClickListener {
                listener.onProductSelected(productListFiltered[adapterPosition].toProduct())
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.product_row_item, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return productListFiltered.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productListFiltered[position]

        holder.nameText.text = context.resources.getString(
            R.string.english_and_korean_name,
            product.name_english,
            product.name_korean
        )
        holder.brandText.text = product.brand
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val constraintString = constraint.toString()
                val lowerCaseConstraintString = constraintString.toLowerCase(Locale.getDefault())

                val filteredList = if (constraintString.isBlank()) {
                    productList
                } else {
                    productList.filter { product ->
                        val enNameContains =
                            product.name_english.toLowerCase(Locale.getDefault()).contains(
                                lowerCaseConstraintString
                            )

                        val krNameContains = product.name_korean.contains(constraintString)

                        val brandContains = product.brand.toLowerCase(Locale.getDefault()).contains(
                            lowerCaseConstraintString
                        )

                        enNameContains || krNameContains || brandContains
                    }
                }

                return FilterResults().apply {
                    values = filteredList
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                productListFiltered = results?.values as ArrayList<RemoteDatabase.FirebaseProduct>
                notifyDataSetChanged()
            }
        }
    }

}

interface FilteredProductAdapterListener {
    fun onProductSelected(product: Product)
}