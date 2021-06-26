package com.thenativecitizens.onlinewallpapereditoradmin.ui.upload

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thenativecitizens.onlinewallpapereditoradmin.data.FirebaseRepo
import com.thenativecitizens.onlinewallpapereditoradmin.util.Category
import com.thenativecitizens.onlinewallpapereditoradmin.util.SubCategory
import com.thenativecitizens.onlinewallpapereditoradmin.util.UploadedImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject


@HiltViewModel
class UploadViewModel @Inject constructor(application: Application, private val repository: FirebaseRepo)
    : AndroidViewModel(application) {

    //CoroutineScope and Job
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _uploadedImageList = MutableLiveData<MutableList<UploadedImage>>()
    val uploadedImageList: LiveData<MutableList<UploadedImage>> get() = _uploadedImageList

    private lateinit var currentCategory: Category
    private lateinit var currentSubCategory: SubCategory

    //a list of images to be uploaded
    private var localUploadList: MutableList<UploadedImage> = mutableListOf()

    //A count of images that has successfully been uploaded
    private var count = 0



    init {
        _uploadedImageList.value = mutableListOf()
    }

    //Gets the current Category where the Images is been uploaded to
    //for future updating of the Image placeholder Url
    fun getCurrentCategory(categoryName: String){
        uiScope.launch {
            repository.fetchCategories {
                it.map { indexedCategory ->
                    if(indexedCategory.categoryName == categoryName)
                        currentCategory = indexedCategory
                }
            }
        }
    }


    //Gets the current subCategory where the Images is been uploaded to
    //for future updating of the ImageUrl list
    fun getCurrentSubCategory(categoryName: String, subCategoryName: String){
        uiScope.launch {
            repository.fetchSubCategoryByName(categoryName, subCategoryName){
                currentSubCategory = it
            }
        }
    }

    //updates the UI with the list of selected Images for upload
    fun updateUploadList(list: MutableList<UploadedImage>){
        _uploadedImageList.value = list
        localUploadList = list
    }

    //called to begin uploading to the Firebase storage
    fun beginUpload(categoryName: String, subCategoryName: String){
        localUploadList.forEachIndexed{ index, uploadedImage ->
            uiScope.launch {
                uploadToStorage(index, uploadedImage, categoryName, subCategoryName)
            }
        }
    }

    private suspend fun uploadToStorage(index: Int, uploadedImage: UploadedImage, categoryName: String,
                                           subCategoryName: String){
        withContext(Dispatchers.IO){
            repository.uploadToStorage(index, uploadedImage, categoryName, subCategoryName,
                { currentProgress ->
                uploadedImage.imageUploadProgress = currentProgress
                localUploadList[index] = uploadedImage
                _uploadedImageList.value = localUploadList },
                {imageUrl ->
                updateSubCategory(imageUrl) }
            )
        }
    }


    //updates the current Subcategory in the database, with the new sets of image urls
    private fun updateSubCategory(imageUrl: String){
       uiScope.launch {
           count++ //increase the count of successfully uploaded images
           val list = currentSubCategory.imageUrlList
           if (!list.contains(imageUrl)){
               //Add the Image url if the list does not
               //contain it to avoid repetition
               list.add(imageUrl)
               currentSubCategory.imageUrlList = list
           }
           if(count == localUploadList.size){
               //Shuffle for a new subcategory image placeholder
               currentSubCategory.subCategoryPlaceholderImageUrl = list.shuffled()[0]
               //Shuffle for a new category image placeholder
               currentCategory.categoryImagePlaceholderUrl = list.shuffled()[0]
               //Update category with list of image urls
               repository.onUploadCompleted(currentCategory, currentSubCategory, count)
           }
       }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}