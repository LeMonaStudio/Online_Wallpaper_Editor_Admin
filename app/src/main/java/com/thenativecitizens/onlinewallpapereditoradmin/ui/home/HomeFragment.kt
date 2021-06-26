package com.thenativecitizens.onlinewallpapereditoradmin.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.thenativecitizens.onlinewallpapereditoradmin.R
import com.thenativecitizens.onlinewallpapereditoradmin.databinding.FragmentHomeBinding
import com.thenativecitizens.onlinewallpapereditoradmin.ui.dialogs.*
import com.thenativecitizens.onlinewallpapereditoradmin.util.Category
import com.thenativecitizens.onlinewallpapereditoradmin.util.ListAndStringConverter
import com.thenativecitizens.onlinewallpapereditoradmin.util.SubCategory
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


const val keyAddMainDialog = "ADD_MAIN_DIALOG"
const val keyAddCategoryDialog = "ADD_CATEGORY_DIALOG"
const val keyAddSubCategoryDialog = "ADD_SUB_CATEGORY_DIALOG"
const val keyAddImagesDialog = "ADD_IMAGES_DIALOG"
const val keyDeleteDialog = "DELETE_DIALOG"


@AndroidEntryPoint
class HomeFragment @Inject constructor(): Fragment(){

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeViewModel: HomeViewModel

    private var listOfCategory: List<Category> = listOf()
    private var listOfCategoryNames: MutableList<String> = mutableListOf()
    //This is the category selected at any point by the User
    private lateinit var userSelectedCategory: Category


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        //ViewModel
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        //Override onBackPressed for the back button
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner){
            requireActivity().finish()
        }

        //CategoryList RecyclerView
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = CategoryListAdapter(CategoryListListener { category ->
            //set the user selected category value
            userSelectedCategory = category
            //launch the delete dialog
            val bundle = Bundle()
            bundle.putString("CategoryName", category.categoryName)
            bundle.putString("SubCategories", ListAndStringConverter.listToString(category.subCategoryList))
            val dialog = DeleteDialog()
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "DELETE_DIALOG")
        })

        binding.categoryList.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
        }

        /**
         * OnClick Actions
         */
        binding.fab.setOnClickListener {
            //Launch AddMainDialog
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddMainDialog())
        }

        /**
         * ViewModel Observations
         */
        homeViewModel.categoryList.observe(viewLifecycleOwner){
            if (it.isNotEmpty()){
                //binding.progressContainer.visibility = View.GONE
                adapter.data = it
                //Save the categories for later use
                listOfCategory = it
                //Save the list of Category Names locally for reuse
                val list: MutableList<String> = mutableListOf()
                listOfCategory.mapIndexed { _, category ->
                    list.add(category.categoryName)
                }
                listOfCategoryNames = list

                adapter.notifyDataSetChanged()
                binding.executePendingBindings()
            } else {
                //  binding.progressContainer.visibility = View.VISIBLE
                adapter.data = mutableListOf<Category>()
                adapter.notifyDataSetChanged()
                binding.executePendingBindings()
            }
        }

        //Observed to show a loading dialog for background operations
        homeViewModel.showLoadingDialog.observe(viewLifecycleOwner){
            it?.let {
                if (it.isNotEmpty()){
                    binding.loading.visibility = View.VISIBLE
                    binding.loadingOperationName.text = it
                    binding.mainScreen.apply {
                        alpha = 0.6f
                        isEnabled = false
                    }
                    binding.categoryList.apply {
                        isEnabled = false
                    }
                    binding.fab.isEnabled = false
                } else {
                    binding.loading.visibility = View.GONE
                    binding.loadingOperationName.text = ""
                    binding.mainScreen.apply {
                        alpha = 1.0f
                        isEnabled = true
                    }
                    binding.categoryList.apply {
                        isClickable = true
                        isEnabled = true
                    }
                    binding.fab.isEnabled = true
                }
            }
        }

        /**
         * fragmentResult listeners
         */
        //AddMainDialog
        parentFragmentManager.setFragmentResultListener(
            keyAddMainDialog,
            viewLifecycleOwner,
            {requestKey, result ->
                if(requestKey == keyAddMainDialog){
                    //Use the ClickAction to determine what to do with the action
                    when(result.getString("ClickAction")){
                        "Category" -> {
                            //User wants to add a new category
                            //Launch the AddCategoryDialog
                            val dialog = AddCategoryDialog()
                            dialog.show(parentFragmentManager, "ADD_CATEGORY_DIALOG")
                        }
                        "SubCategory" -> {
                            if(listOfCategoryNames.isNotEmpty()){
                                //User wants to add a new category
                                //Launch the AddSubCategoryDialog
                                val dialog = AddSubCategoryDialog()
                                val bundle = Bundle()
                                bundle.putString("CategoryNames", ListAndStringConverter.listToString(listOfCategoryNames))
                                dialog.arguments = bundle
                                dialog.show(parentFragmentManager, "ADD_SUB_CATEGORY_DIALOG")
                            } else{
                                Snackbar.make(binding.root, "To create a subcategory create a category", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        "Image" -> {
                            if(listOfCategoryNames.isNotEmpty()){
                                //User wants to add images
                                //Launch the AddImagesDialog
                                val bundle = Bundle()
                                bundle.putString("CategoryNames", ListAndStringConverter.listToString(listOfCategoryNames))
                                val dialog = AddImagesDialog()
                                dialog.arguments = bundle
                                dialog.show(parentFragmentManager, "ADD_IMAGES_DIALOG")
                            } else{
                                Snackbar.make(binding.root, "To add images create a subcategory first", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        )

        //AddCategoryDialog
        parentFragmentManager.setFragmentResultListener(
            keyAddCategoryDialog,
            viewLifecycleOwner,
            {requestKey, result ->
                if(requestKey == keyAddCategoryDialog){
                    result.getString("CategoryName")?.let {
                        val categoryID: String = UUID.randomUUID().toString()
                        val category = Category(categoryID = categoryID,
                            categoryName =  it, subCategoryList = mutableListOf(), categoryImagePlaceholderUrl = "")
                        homeViewModel.addCategory(category)
                    }
                }
            }
        )

        //AddSubCategoryDialog
        parentFragmentManager.setFragmentResultListener(
            keyAddSubCategoryDialog,
            viewLifecycleOwner,
            {requestKey, result ->
                if(requestKey == keyAddSubCategoryDialog){
                    if(result.getInt("CategoryIndex") >= 0){
                        val category = listOfCategory[result.getInt("CategoryIndex")]
                        val subCategoryID: String = UUID.randomUUID().toString()
                        val subCategory= SubCategory(subCategoryID = subCategoryID,
                                subCategoryName =  result.getString("SubCategoryName", ""),
                                categoryName = category.categoryName,
                                imageUrlList = mutableListOf(),
                                subCategoryPlaceholderImageUrl = "")
                        homeViewModel.addSubCategory(subCategory, category)
                    }
                }
            }
        )

        //AddImagesDialog
        parentFragmentManager.setFragmentResultListener(
            keyAddImagesDialog,
            viewLifecycleOwner,
            {requestKey, result ->
                if(requestKey == keyAddImagesDialog){
                    val category = listOfCategory[result.getInt("CategoryIndex")]
                    val subCategoryName = result.getString("SubCategoryName", category.subCategoryList[0])
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToUploadFragment(category.categoryName, subCategoryName))
                }
            }
        )

        //DeleteDialog
        parentFragmentManager.setFragmentResultListener(
            keyDeleteDialog,
            viewLifecycleOwner,
            {requestKey, result ->
                if (requestKey == keyDeleteDialog){
                    when(result.getInt("DeleteOption")){
                        1 -> {
                            homeViewModel.deleteCategory(userSelectedCategory)
                        }
                        2 -> {
                            homeViewModel.deleteSubCategory(userSelectedCategory,
                                result.getString("SubCategoryName", ""))
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Nothing to Delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )

        return binding.root
    }
}