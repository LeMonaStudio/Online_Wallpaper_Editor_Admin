package com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.DialogUploadImageBinding


class UploadImageDialog : DialogFragment() {

    private lateinit var binding: DialogUploadImageBinding
    private val keyUploadImageDialog = "UPLOAD_IMAGE_DIALOG"
    private val bundle = Bundle()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_upload_image, null, false)

        arguments?.let {
            binding.selectedImagesMessage.text = getString(R.string.image_selected_count_prompt,
                it.getInt("ImageSelectedCount", 0).toString())
        }

        //OnClickListeners for the Buttons
        binding.startUploadBtn.setOnClickListener {
            bundle.putInt("ClickAction", 1)
            parentFragmentManager.setFragmentResult(keyUploadImageDialog, bundle)
            dismiss()
        }
        binding.cancelBtn.setOnClickListener {
            bundle.putInt("ClickAction", 0)
            parentFragmentManager.setFragmentResult(keyUploadImageDialog, bundle)
            dismiss()
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }
}