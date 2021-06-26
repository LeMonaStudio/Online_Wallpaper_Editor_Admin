package com.thenativecitizens.onlinewallpapereditoradmin.ui.upload

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.FragmentUploadBinding
import com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs.UploadImageDialog
import com.thenativecitizens.onlinewallpapereditoradmin.util.UploadedImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val keyUploadImageDialog = "UPLOAD_IMAGE_DIALOG"


@AndroidEntryPoint
class UploadFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentUploadBinding
    private lateinit var uploadViewModel: UploadViewModel
    //Result Launcher to pick image(s) from the user's device
    private lateinit var pickImageContract: ActivityResultLauncher<String>

    //Fields for UI to determine if uploading is done
    private var uploadProgress = 0
    private var uploadTarget = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_upload, container, false)

        uploadViewModel = ViewModelProvider(this).get(UploadViewModel::class.java)

        //Override onBackPressed for the back button
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner){
            if (uploadProgress == uploadTarget)
                findNavController().popBackStack()
            else
                Snackbar.make(binding.root, "INFO: Do not exit! Uploading is still in progress.", Snackbar.LENGTH_LONG).show()
        }

        //Get the Arguments Bundle
        val args = UploadFragmentArgs.fromBundle(requireArguments())

        //Args to UI and ViewModel
        val text = "to:${args.categoryName}/${args.subCategoryName}"
        binding.uploadDestination.text = text
        uploadViewModel.getCurrentCategory(args.categoryName)
        uploadViewModel.getCurrentSubCategory(args.categoryName, args.subCategoryName)


        //Immediately this fragment is loaded, launch the image picker
        pickImageContract = registerForActivityResult(ActivityResultContracts.GetMultipleContents()){
            //hide the Progress Container
            binding.progressContainer.visibility = View.GONE
            if(it.isNotEmpty()){
                //List of Images to Upload
                val listOfUploadImages: MutableList<UploadedImage> = mutableListOf()
                it.mapIndexed { _, uri ->
                    with(uri){
                        requireContext().contentResolver.query(this,null,
                                null, null, null)?.use { cursor ->
                            cursor.moveToFirst()
                            val imageName: String = (cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)))
                            val uploadedImage = UploadedImage(imageName = imageName, imageUri = this, imageUploadProgress = 0)
                            listOfUploadImages.add(uploadedImage)
                            cursor.close()
                        }
                    }
                }
                val bundle = Bundle()
                bundle.putInt("ImageSelectedCount", listOfUploadImages.size)
                val dialog = UploadImageDialog()
                dialog.apply {
                    isCancelable = false
                    arguments = bundle
                }
                //pass the list to uploadViewModel to handle
                uploadViewModel.updateUploadList(listOfUploadImages)
                dialog.show(parentFragmentManager, "Upload_Image_Dialog")
            } else{
                //No Image was selected
                findNavController().popBackStack()
            }
        }
        //Move user to pick images to upload
        pickImageContract.launch("image/*")

        //RecyclerView
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ImageUploadListAdapter()

        binding.imageUploadingList.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
        }


        /**
         * Live Data Observations
         */
        //Observe for the list of Images been uploaded
        uploadViewModel.uploadedImageList.observe(viewLifecycleOwner){
            if (it.isNotEmpty()){
                //Pass to ImageUploadList RecyclerView's Adapter
                adapter.data = it
                //Set the count in the UI
                uploadTarget = it.size
                uploadProgress = it.filter { uploadedImage -> uploadedImage.imageUploadProgress == 100 }.size
                binding.progressCountText.text = getString(R.string.progress_count, "$uploadProgress/$uploadTarget")

                if(uploadProgress == uploadTarget){
                    //Uploading is done
                    Snackbar.make(binding.root, "Upload completed!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK"){
                            findNavController().popBackStack()
                        }
                        .show()
                    binding.uploadProgress.visibility = View.GONE
                }
                binding.executePendingBindings()
            }
        }


        /**
         * Fragment result listeners
         */
        //ParentFragment Result Listeners
        parentFragmentManager.setFragmentResultListener(
            keyUploadImageDialog,
            viewLifecycleOwner,
            {requestKey, result ->
                if(requestKey == keyUploadImageDialog){
                    val clickAction = result.getInt("ClickAction")
                    if (clickAction >= 1){
                        uploadViewModel.beginUpload(args.categoryName, args.subCategoryName)
                        binding.uploadProgress.visibility = View.VISIBLE
                    } else {
                        //Use choose to cancel
                        findNavController().popBackStack()
                    }
                }
            }
        )

        return binding.root
    }
}