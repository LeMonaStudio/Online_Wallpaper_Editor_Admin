package com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.DialogAddSubCategoryBinding


class AddSubCategoryDialog : DialogFragment() {
    private lateinit var binding: DialogAddSubCategoryBinding
    private var keyAddSubCategoryDialog = "ADD_SUB_CATEGORY_DIALOG"
    private var selectedCategoryIndex = -1
    private var subCategoryName: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_sub_category, null, false)

        //Get the existing list of category names
        val listOfCategoryNames: MutableList<String>? = arguments?.let {
            it.getString("CategoryNames")?.toLst()
        }
        //pass the list to the category names spinner
        listOfCategoryNames?.let {
            //Array Adapter for the Category Names Spinner
            ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, listOfCategoryNames).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                //pass Adapter to the Spinner
                binding.categorySelectionSpinner.adapter = adapter
            }
        }
        //Observe the spinner's item selection
        binding.categorySelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryIndex = position
                enableOrDisableBtn()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryIndex = -1
                enableOrDisableBtn()
            }
        }

        //Add TextWatcher to the SubCategoryEditText
        binding.subCategoryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                subCategoryName = s.toString()
                enableOrDisableBtn()
            }
            override fun afterTextChanged(s: Editable?) {}

        })

        //When the Add button is clicked
        binding.addSubCategoryBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.apply {
                putInt("CategoryIndex", selectedCategoryIndex)
                putString("SubCategoryName", subCategoryName)
            }
            parentFragmentManager.setFragmentResult(keyAddSubCategoryDialog, bundle)
            dismiss()
        }

        //Creating the Dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }


    //Called to enable or disable the Add Button
    private fun enableOrDisableBtn(){
        binding.addSubCategoryBtn.isEnabled = (subCategoryName.isNotEmpty() && selectedCategoryIndex >= 0)
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