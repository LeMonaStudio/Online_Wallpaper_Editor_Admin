package com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.ListSubcategoriesDeleteViewBinding


class SubCategoryListAdapter(private val listener: SubCategoryListListener): RecyclerView.Adapter<SubCategoryListAdapter.ViewHolder>(){

    var data = listOf<String>()
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
        //val ctx = holder.itemView.context
        val category = data[position]
        holder.bind(category, listener)
    }


    class ViewHolder private constructor(private val binding: ListSubcategoriesDeleteViewBinding)
        : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListSubcategoriesDeleteViewBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(subCategoryName: String, listener: SubCategoryListListener) {
            binding.subCategoryNameVar = subCategoryName
            binding.subCategoryName.text = subCategoryName
            binding.listener = listener
            binding.executePendingBindings()
        }
    }
}


//OnClickListener for the RecyclerView
class SubCategoryListListener(val clickListener: (subCategoryName: String) -> Unit){
    fun onClick(subCategoryName: String) = clickListener(subCategoryName)
}