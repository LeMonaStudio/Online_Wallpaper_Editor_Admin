package com.thenativecitizens.onlinewallpapereditoradmin.data

import com.thenativecitizens.onlinewallpapereditoradmin.util.Category
import com.thenativecitizens.onlinewallpapereditoradmin.util.SubCategory
import com.thenativecitizens.onlinewallpapereditoradmin.util.UploadedImage

interface FirebaseRepo {
    /**
     * Firebase Database functions
     */
    fun fetchCategories(callback: (MutableList<Category>) -> Unit)
    fun fetchSubCategoryByName(categoryName: String, subCategoryName: String, callback: (SubCategory) -> Unit)
    fun addCategory(category: Category, callback: (Boolean) -> Unit)
    fun addSubCategory(subCategory: SubCategory, category: Category, callback: (Boolean) -> Unit)
    fun deleteCategory(category: Category, callback: (Boolean) -> Unit)
    fun deleteSubCategory(category: Category, subCategoryName: String, callback: (Boolean) -> Unit)

    /**
     * Firebase Storage functions
     */
    fun uploadToStorage(index: Int, uploadedImage: UploadedImage,
                        categoryName: String, subCategoryName: String,
                        progressCallback: (Int) -> Unit,
                        onCompletionCallback: (StringImageUrl: String) -> Unit)
    fun onUploadCompleted(currentCategory: Category, currentSubCategory: SubCategory, totalImageUploaded: Int)
}