package com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.DialogDeleteBinding


class DeleteDialog : DialogFragment() {
    private lateinit var binding: DialogDeleteBinding
    private var keyDeleteDialog = "DELETE_DIALOG"
    private var categoryName: String = ""
    private var subCategoryList: MutableList<String> = mutableListOf()
    private val bundle = Bundle()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_delete, null, false)

        arguments?.let {
            categoryName = it.getString("CategoryName", "")
            binding.categoryName.text = categoryName

            subCategoryList = it.getString("SubCategories")!!.toLst()

            if (subCategoryList.isEmpty()) binding.deleteCategoryBtn.apply {
                this.alpha = 1.0f
                this.isEnabled = true
                binding.subCategoryListTitle.visibility = View.GONE
                binding.subCategoryList.visibility = View.GONE
                this.setOnClickListener {
                    //Tell the HomeFragment to delete
                    bundle.putInt("DeleteOption", 1) // 1 for Delete Category, 2 for Delete SubCategory
                    parentFragmentManager.setFragmentResult(keyDeleteDialog, bundle)
                    dismiss()
                }
            }

            val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            val adapter = SubCategoryListAdapter(SubCategoryListListener { subCategoryName ->
                //Tell the HomeFragment to delete
                bundle.putInt("DeleteOption", 2) // 1 for Delete Category, 2 for Delete SubCategory
                bundle.putString("SubCategoryName", subCategoryName)
                parentFragmentManager.setFragmentResult(keyDeleteDialog, bundle)
                dismiss()
            })
            binding.subCategoryList.apply {
                this.layoutManager = layoutManager
                this.adapter = adapter
                adapter.data = subCategoryList
            }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }


    //Utility function to convert String to a list
    private fun String.toLst(): MutableList<String>{
        val list: MutableList<String> = mutableListOf()
        if(this.isNotEmpty()){
            val tempList: List<String> = this.split(",").map { it }.toList()
            for (i in tempList.indices){
                list.add(tempList[i])
            }
        }
        return list
    }
}