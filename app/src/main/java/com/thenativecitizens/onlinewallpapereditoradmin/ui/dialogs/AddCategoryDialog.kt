package com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.DialogAddCategoryBinding


class AddCategoryDialog : DialogFragment() {

    private lateinit var binding: DialogAddCategoryBinding
    private val keyAddCategoryDialog = "ADD_CATEGORY_DIALOG"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_category, null, false)

        //When the Add button is clicked
        binding.addCategoryBtn.setOnClickListener {
            if (binding.categoryName.text.toString().isNotEmpty()){
                //Send the name of the Category to the HomeFragment to handle
                val bundle = Bundle()
                bundle.putString("CategoryName", binding.categoryName.text.toString())
                parentFragmentManager.setFragmentResult(keyAddCategoryDialog, bundle)
                dismiss()
            }
        }

        //Add TextWatcher to the CategoryName edit text
        binding.categoryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Enable or disable the Add button is the edit text is empty or not
                binding.addCategoryBtn.isEnabled = s.toString().isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}

        })
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }
}