package com.thenativecitizens.onlinewallpapereditoradmin.ui.upload

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thenativecitizens.onlinewallpapereditoradmin.util.Category
import com.thenativecitizens.onlinewallpapereditoradmin.util.SubCategory
import com.thenativecitizens.onlinewallpapereditoradmin.util.UploadedImage
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    //Firebase Database references
    private var categoryFirebaseDatabaseRef: DatabaseReference = Firebase.database.reference.child("categories")
    private var sbCategoryFirebaseDatabaseRef: DatabaseReference = Firebase.database.reference.child("subcategories")

    private val firebaseFCMAPI = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + "AAAACsmlwik:APA91bEoSXYAEeiSzwAxzY--iCh38WnFYK3xnNndtaZTNgLHJYHC3nmnvKgfdUert0g-FwxUDQ8SSbuSN7pJv92Mvw30-kGkdKV4DEJX76jXPloswNLDH1Bbdo1CMkXBktRzOl5rwuN2"
    private val contentType = "application/json"


    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(getApplication())
    }


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
        categoryFirebaseDatabaseRef.child(categoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<Category>()?.let {
                        currentCategory = it
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }


    //Gets the current subCategory where the Images is been uploaded to
    //for future updating of the ImageUrl list
    fun getCurrentSubCategory(categoryName: String, subCategoryName: String){
        sbCategoryFirebaseDatabaseRef.child(categoryName).child(subCategoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<SubCategory>()?.let {
                        currentSubCategory = it
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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


    private suspend fun uploadToStorage(index: Int, uploadedImage: UploadedImage, categoryName: String, subCategoryName: String){
        withContext(Dispatchers.IO){
            //Get an instance of the firebase storage
            val storageRef: StorageReference = Firebase.storage.reference
            uploadedImage.imageUri.let {thisUri ->
                val imageStorageRef = storageRef.child("$categoryName/$subCategoryName/${uploadedImage.imageName}")
                imageStorageRef.putFile(thisUri)
                    .addOnProgressListener {
                        //Show the Progress
                        val currentProgress = ((100.0*it.bytesTransferred)/it.totalByteCount).toInt()
                        uploadedImage.imageUploadProgress = currentProgress
                        localUploadList[index] = uploadedImage
                        _uploadedImageList.value = localUploadList
                    }
                    .addOnSuccessListener {
                        //Get the download Url from Firebase storage
                        imageStorageRef.downloadUrl.addOnSuccessListener {downloadUri ->
                            //Save the Url to the SubCategory's image url list
                            updateSubCategory(downloadUri.toString(), categoryName, subCategoryName)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(getApplication(), "Failed to upload ${uploadedImage.imageName}",
                            Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    //updates the current Subcategory in the database, with the new sets of image urls
    private fun updateSubCategory(imageUrl: String, categoryName: String, subCategoryName: String){
        count++ //increase the count of successfully uploaded images
        val list = currentSubCategory.imageUrlList
        if (!list.contains(imageUrl)){
            //Add the Image url if the list does not
            //contain it to avoid repetition
            list.add(imageUrl)
            currentSubCategory.imageUrlList = list
            //Shuffle for a new subcategory image placeholder
            currentSubCategory.subCategoryPlaceholderImageUrl = list.shuffled()[0]
        }
        if(count == localUploadList.size){
            //Shuffle for a new category image placeholder
            currentCategory.categoryImagePlaceholderUrl = list.shuffled()[0]
            //Update category with list of image urls
            onUploadCompleted(categoryName, subCategoryName, count)
        }
    }

    //Update the database
    private fun onUploadCompleted(categoryName: String, subCategoryName: String, count: Int){
        sbCategoryFirebaseDatabaseRef.child(categoryName).child(subCategoryName).setValue(currentSubCategory)
            .addOnSuccessListener {
                categoryFirebaseDatabaseRef.child(categoryName).setValue(currentCategory)
                    .addOnFailureListener {
                        //Not Successful
                        Toast.makeText(getApplication(), "Failed! $categoryName was not updated: $it", Toast.LENGTH_SHORT).show()
                    }
                prepareNotification(categoryName, subCategoryName, count)
            }
            .addOnFailureListener {
                //Show failure toast
                Toast.makeText(getApplication(), "Failed: $it", Toast.LENGTH_LONG).show()
            }
    }


    //Prepares the notification to be sent to FCM servers
    //that notifies the users of new wallpapers
    private fun prepareNotification(categoryName: String, subCategoryName: String, count: Int) {
        val topic = "/topics/wallpapers" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", "New Wallpapers Added To: $categoryName")
            //Enter your notification message
            notificationBody.put("message", "$count new $subCategoryName wallpapers has been added")
            notification.put("to", topic)
            notification.put("data", notificationBody)
            Log.i("TAG", "try")
        } catch (e: JSONException) {
            Log.i("TAG", "onCreate: " + e.message)
        }
        //Send the notification to FCM server
        sendNotification(notification)
    }

    //Sends the prepared notification to FCM servers
    private fun sendNotification(notification: JSONObject) {
        Log.i("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(firebaseFCMAPI, notification,
            Response.Listener { response ->
                Log.i("TAG", "onResponse: $response")
                Toast.makeText(getApplication(), "Successfully sent notification", Toast.LENGTH_LONG).show()
            },
            Response.ErrorListener {
                Toast.makeText(getApplication(), "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
                Toast.makeText(getApplication(), "onErrorResponse: Didn't work for send notification", Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}