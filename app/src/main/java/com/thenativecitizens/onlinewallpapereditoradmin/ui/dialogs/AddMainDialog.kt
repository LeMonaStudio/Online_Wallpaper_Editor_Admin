package com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.DialogAddMainBinding

class AddMainDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogAddMainBinding
    private val keyAddMainDialog = "ADD_MAIN_DIALOG"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_main, container, false)

        //the bundle to be sent back to the HomeFragment
        val bundle = Bundle()

        /**
         * OnClick actions
         */
        //Add Category Btn
        binding.addCategoryBtn.setOnClickListener {
            //let the HomeFragment know about the button clicked
            bundle.putString("ClickAction", "Category")
            parentFragmentManager.setFragmentResult(keyAddMainDialog, bundle)
            //dismiss the dialog
            dialog?.dismiss()
        }
        //Add SubCategory Btn
        binding.addSubCategoryBtn.setOnClickListener {
            //let the HomeFragment know about the button clicked
            bundle.putString("ClickAction", "SubCategory")
            parentFragmentManager.setFragmentResult(keyAddMainDialog, bundle)
            //dismiss the dialog
            dialog?.dismiss()
        }
        //Add Image Btn
        binding.addImageBtn.setOnClickListener {
            //let the HomeFragment know about the button clicked
            bundle.putString("ClickAction", "Image")
            parentFragmentManager.setFragmentResult(keyAddMainDialog, bundle)
            //dismiss the dialog
            dialog?.dismiss()
        }

        return binding.root
    }
}