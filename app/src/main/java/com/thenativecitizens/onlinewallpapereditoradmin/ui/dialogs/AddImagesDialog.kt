package com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.DialogAddImagesBinding
import com.thenativecitizens.onlinewallpapereditoradmin.model.Category


class AddImagesDialog : DialogFragment() {

    private lateinit var binding: DialogAddImagesBinding
    private var keyAddImagesDialog = "ADD_IMAGES_DIALOG"

    private var selectedCategoryIndex: Int = -1
    private var selectedSubCategoryName: String = ""

    //Firebase Database references
    private var categoryFirebaseDatabaseRef: DatabaseReference = Firebase.database.reference.child("categories")

    //locally saved list of subCategories for the selected category
    private var listOfSubCats: MutableList<String> = mutableListOf()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_images, null, false)

        //disable the SubCategory Spinner
        binding.subCategorySelectionSpinner.isEnabled = false

        //Get the existing list of category names
        val listOfCategoryNames: MutableList<String>? = arguments?.let {
            it.getString("CategoryNames")?.toLst()
        }

        //pass the list to the category names spinner
        listOfCategoryNames?.let {
            //fetch the first item category
            fetchCategory(it[0])

            //Array Adapter for the Category Names Spinner
            ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, listOfCategoryNames).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                //pass Adapter to the Spinner
                binding.categorySelectionSpinner.adapter = adapter
            }
        }

        //Observe the Category spinner's item selection
        binding.categorySelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryIndex = position
                //make the progress bar visible
                binding.progress.visibility = View.VISIBLE
                //disable the subCategory Spinner
                binding.subCategorySelectionSpinner.isEnabled = false
                //call to fetch the selected category
                listOfCategoryNames?.let {
                    fetchCategory(it[position])
                }
                enableOrDisableBtn()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryIndex = -1
                enableOrDisableBtn()
            }
        }

        //Observe the SubCategory spinner's item selection
        binding.subCategorySelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSubCategoryName = listOfSubCats[position]
                enableOrDisableBtn()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryIndex = -1
                enableOrDisableBtn()
            }
        }


        //When the Continue Button is clicked
        binding.continueToUploadBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.apply {
                putInt("CategoryIndex", selectedCategoryIndex)
                putString("SubCategoryName", selectedSubCategoryName)
                parentFragmentManager.setFragmentResult(keyAddImagesDialog, bundle)
                dismiss()
            }

        }



        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }


    //Called to enable or disable the Add Button
    private fun enableOrDisableBtn(){
        binding.continueToUploadBtn.isEnabled = (selectedSubCategoryName.isNotEmpty() && selectedCategoryIndex >= 0)
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

    //fetch a category from database by the Spinner selection
    private fun fetchCategory(categoryName: String){
        categoryFirebaseDatabaseRef.child(categoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<Category>()?.let {
                        listOfSubCats = it.subCategoryList
                        //hide the progress
                        binding.progress.visibility = View.GONE
                        //pass Adapter to the Spinner
                        binding.subCategorySelectionSpinner.isEnabled = true
                        setSubCategorySpinnerAdapter(it)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //Sets the Adapter for the SubCategory Spinner
    private fun setSubCategorySpinnerAdapter(category: Category) {
        //pass the list to the category names spinner
        category.subCategoryList.let {
            //Array Adapter for the Category Names Spinner
            ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, it).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.subCategorySelectionSpinner.adapter = adapter
            }
        }
    }
}