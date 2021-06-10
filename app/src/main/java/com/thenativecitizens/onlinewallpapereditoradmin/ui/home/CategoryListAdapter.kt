package com.thenativecitizens.onlinewallpapereditoradmin.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.ListCategoriesViewBinding
import com.thenativecitizens.onlinewallpapereditoradmin.util.Category

class CategoryListAdapter(private val listener: CategoryListListener): RecyclerView.Adapter<CategoryListAdapter.ViewHolder>(){

    var data = listOf<Category>()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ctx = holder.itemView.context
        val category = data[position]
        holder.bind(category, listener, ctx)
    }


    class ViewHolder private constructor(private val binding: ListCategoriesViewBinding)
        : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListCategoriesViewBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(category: Category, listener: CategoryListListener, ctx: Context) {
            binding.categoryName.text = category.categoryName
            binding.subCategoryList.text = ctx.getString(R.string.sub_category, category.subCategoryList.toStr())
            //binding.imageCount.text = category.totalImageCount.toString()
            binding.category = category
            binding.listener = listener
            binding.executePendingBindings()
        }

        private fun MutableList<String>.toStr(): String{
            var str = ""
            if(this.isNotEmpty()){
                for (i in 0 until this.size){
                    str += this[i]
                    if (i != this.size -1)
                        str += ","
                }
            }
            return str
        }
    }
}


//OnClickListener for the RecyclerView
class CategoryListListener(val clickListener: (category: Category) -> Unit){
    fun onClick(category: Category) = clickListener(category)
}