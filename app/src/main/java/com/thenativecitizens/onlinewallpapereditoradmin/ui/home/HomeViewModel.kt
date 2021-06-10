package com.thenativecitizens.onlinewallpapereditoradmin.ui.home

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.thenativecitizens.onlinewallpapereditoradmin.util.Category
import com.thenativecitizens.onlinewallpapereditoradmin.util.SubCategory

class HomeViewModel(application: Application) : AndroidViewModel(application){

    //Firebase Database references
    private var categoryFirebaseDatabaseRef: DatabaseReference = Firebase.database.reference.child("categories")
    private var sbCategoryFirebaseDatabaseRef: DatabaseReference = Firebase.database.reference.child("subcategories")


    //This field will hold the list of categories
    private var _categoryList = MutableLiveData<MutableList<Category>>()
    val categoryList: LiveData<MutableList<Category>> get() = _categoryList

    //Holds the data snapshot keys to be able to update the list of categories on childChanged
    private var snapShotKeys: MutableList<String> = mutableListOf()

    //Observed to show a loading dialog to the user
    private var _showLoadingDialog = MutableLiveData<String>()
    val showLoadingDialog: LiveData<String> get() = _showLoadingDialog

    //the current category been deleted or whose subCategory is been deleted
    private lateinit var affectedCategory: Category

    init {
        _categoryList.value = mutableListOf()
        _showLoadingDialog.value = ""
        fetchCategories()
    }

    //Called to fetch the list of category Ids
    private fun fetchCategories() {
        categoryFirebaseDatabaseRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val list = _categoryList.value!!
                    _categoryList.value = mutableListOf()
                    snapshot.getValue<Category>()?.let {
                        list.add(it)
                        _categoryList.value = list
                        snapShotKeys.add(snapshot.key!!)
                    }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val list = _categoryList.value!!
                    _categoryList.value = mutableListOf()
                    val modifiedCategory = snapshot.getValue<Category>()
                    val key = snapshot.key

                    modifiedCategory?.let {newCat ->
                        val alteredIndex = snapShotKeys.indexOf(key)
                        list[alteredIndex] = newCat
                        _categoryList.value = list
                    }
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val list = _categoryList.value!!
                    _categoryList.value = mutableListOf()
                    val removedCategory = snapshot.getValue<Category>()
                    val deletedKey = snapshot.key

                    removedCategory?.let {
                        val index = snapShotKeys.indexOf(deletedKey)
                        list.removeAt(index)
                        _categoryList.value = list
                        snapShotKeys.removeAt(index)
                    }
                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }



    //Called by the HomeFragment to add a new category to the database
    fun addCategory(category: Category){
        _showLoadingDialog.value = "Adding ${category.categoryName}"
        categoryFirebaseDatabaseRef.child(category.categoryName).setValue(category)
            .addOnSuccessListener {
                //If successfully, show a Toast
                Toast.makeText(getApplication(), "Added Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                //Not Successful
                Toast.makeText(getApplication(), "Failed!", Toast.LENGTH_SHORT).show()
            }
        _showLoadingDialog.value = ""
    }

    //Called to update an existing category with the new subcategory
    private fun updateCategory(category: Category, subCategoryName: String?, operation: Int){
        subCategoryName?.let {
            val listOfSubCategory = category.subCategoryList
            if(operation == 1){
                //We adding a new subcategory
                listOfSubCategory.add(subCategoryName)
                _showLoadingDialog.value = "Updating Category with: $subCategoryName"
            } else {
                //We are removing a subcategory
                listOfSubCategory.remove(subCategoryName)
            }
            category.subCategoryList = listOfSubCategory
            categoryFirebaseDatabaseRef.child(category.categoryName).setValue(category)
                    .addOnSuccessListener {
                        if (operation == 1)
                            Toast.makeText(getApplication(), "Added Successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        if(operation == 1)
                            //Not Successful
                            Toast.makeText(getApplication(), "Failed!", Toast.LENGTH_SHORT).show()
                    }
            _showLoadingDialog.value = ""
        }
    }

    //called by the add a new subcategory to the subcategories table
    fun addSubCategory(subCategory: SubCategory, category: Category){
        _showLoadingDialog.value = "Adding Subcategory: ${subCategory.subCategoryName}"
        sbCategoryFirebaseDatabaseRef.child(category.categoryName)
            .child(subCategory.subCategoryName).setValue(subCategory)
            .addOnSuccessListener {
                //If successfully, show a Toast
                updateCategory(category, subCategory.subCategoryName, 1)
            }
            .addOnFailureListener {
                //Not Successful
                Toast.makeText(getApplication(), "Failed!", Toast.LENGTH_SHORT).show()
            }
        _showLoadingDialog.value = ""
    }

    //Called to remove or delete an existing category
    fun onDeleteCategory(category: Category){
        affectedCategory = category
        _showLoadingDialog.value = "Deleting ${affectedCategory.categoryName}"
        categoryFirebaseDatabaseRef.child(affectedCategory.categoryName).removeValue()
            .addOnSuccessListener {
                Toast.makeText(getApplication(), "Deleted successfully",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(getApplication(), "Error deleting ${affectedCategory.categoryName}",
                    Toast.LENGTH_SHORT).show()
            }
        _showLoadingDialog.value = ""
    }


    //Called to delete subCategory
    fun onDeleteSubCategory(category: Category, subCategoryName: String){
        affectedCategory = category
        val list = affectedCategory.subCategoryList
        list.remove(subCategoryName)
        if (list.size >= 1)
        //the Category's subcategory list is not empty so fetch new image url for placeholder
            getNewCategoryPlaceHolderImageUrl(list.shuffled()[0])
        //now fetch the subcategory and delete it
        getSubCategoryAndDelete(subCategoryName)
    }


    //called to get a new placeholder url for the category whose subcategory is been deleted
    private fun getNewCategoryPlaceHolderImageUrl(otherSubCategoryName: String) {
        sbCategoryFirebaseDatabaseRef.child(affectedCategory.categoryName).child(otherSubCategoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<SubCategory>()?.let { subCategory ->
                        _showLoadingDialog.value = "Fetching new placeholder url"
                        affectedCategory.categoryImagePlaceholderUrl =
                            subCategory.imageUrlList.shuffled()[0]
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }


    //Called to get the SubCategory that is to be deleted
    private fun getSubCategoryAndDelete(subCategoryName: String){
        _showLoadingDialog.value = "Fetching subcategory for delete"
        sbCategoryFirebaseDatabaseRef.child(affectedCategory.categoryName).child(subCategoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<SubCategory>()?.let { subCategory ->
                        _showLoadingDialog.value = "Now deleting $subCategoryName"

                        val newSubCategoryList = affectedCategory.subCategoryList
                        newSubCategoryList.remove(subCategoryName)
                        affectedCategory.subCategoryList = newSubCategoryList
                        updateCategory(affectedCategory, subCategoryName, 0)

                        _showLoadingDialog.value = "Deleting $subCategoryName images}"

                        sbCategoryFirebaseDatabaseRef.child(affectedCategory.categoryName).child(subCategoryName).removeValue()
                            .addOnSuccessListener {
                                var countDelete = 0
                                if (subCategory.imageUrlList.isNotEmpty()){
                                    subCategory.imageUrlList.forEach {
                                        Firebase.storage.getReferenceFromUrl(it).delete()
                                            .addOnSuccessListener {
                                                countDelete++
                                                if (countDelete == subCategory.imageUrlList.size){
                                                    Toast.makeText(getApplication(), "Deleted successfully",
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(getApplication(), "Error deleting some images from $subCategoryName",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                //Error removing this subcategory
                                Toast.makeText(getApplication(), "Error deleting $subCategoryName",
                                    Toast.LENGTH_SHORT).show()
                            }
                        _showLoadingDialog.value = ""
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}