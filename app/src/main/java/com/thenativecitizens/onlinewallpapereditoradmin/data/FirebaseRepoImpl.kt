package com.thenativecitizens.onlinewallpapereditoradmin.data

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thenativecitizens.onlinewallpapereditoradmin.util.Category
import com.thenativecitizens.onlinewallpapereditoradmin.util.SubCategory
import com.thenativecitizens.onlinewallpapereditoradmin.util.UploadedImage
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


class FirebaseRepoImpl @Inject constructor(private val application: Application,
                                           private val databaseReference: DatabaseReference,
                                           private val storageReference: StorageReference): FirebaseRepo {

    private val firebaseFCMAPI = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + "AAAACsmlwik:APA91bEoSXYAEeiSzwAxzY--iCh38WnFYK3xnNndtaZTNgLHJYHC3nmnvKgfdUert0g-FwxUDQ8SSbuSN7pJv92Mvw30-kGkdKV4DEJX76jXPloswNLDH1Bbdo1CMkXBktRzOl5rwuN2"
    private val contentType = "application/json"

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(application)
    }


    private fun updateCategory(category: Category, subCategoryName: String?, operation: Int): Boolean {
        var status = false
        subCategoryName?.let {
            val listOfSubCategory = category.subCategoryList
            if(operation == 1){
                //We adding a new subcategory
                listOfSubCategory.add(subCategoryName)
            } else {
                //We are removing a subcategory
                listOfSubCategory.remove(subCategoryName)
            }
            category.subCategoryList = listOfSubCategory
            databaseReference.child("categories").child(category.categoryName).setValue(category)
                .addOnSuccessListener {
                    if (operation == 1)
                        Toast.makeText(application, "Added Successfully", Toast.LENGTH_SHORT).show()
                    status = true
                }
                .addOnFailureListener {
                    if(operation == 1)
                    //Not Successful
                        Toast.makeText(application, "Failed!", Toast.LENGTH_SHORT).show()
                    status = false
                }
        }
        return status
    }
    //called to get a new placeholder url for the category whose subcategory is been deleted
    private fun updatePlaceHolderImageUrl(category: Category, subCategoryName: String, otherSubCategoryName: String): Boolean {
        var status = false
        databaseReference.child("subcategories").child(category.categoryName).child(otherSubCategoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<SubCategory>()?.let { subCategory ->
                        category.categoryImagePlaceholderUrl =
                            if(subCategory.imageUrlList.isNotEmpty()) subCategory.imageUrlList.shuffled()[0] else ""
                        status = getSubCategoryAndDelete(category, subCategoryName)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        return status
    }

    //Called to get the SubCategory that is to be deleted
    private fun getSubCategoryAndDelete(category: Category, subCategoryName: String): Boolean{
        var status = false
        databaseReference.child("subcategories").child(category.categoryName).child(subCategoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<SubCategory>()?.let { subCategory ->

                        val newSubCategoryList = category.subCategoryList
                        newSubCategoryList.remove(subCategoryName)
                        category.subCategoryList = newSubCategoryList
                        updateCategory(category, subCategoryName, 0)

                        databaseReference.child("subcategories").child(category.categoryName).child(subCategoryName).removeValue()
                            .addOnSuccessListener {
                                var countDelete = 0
                                if (subCategory.imageUrlList.isNotEmpty()){
                                    subCategory.imageUrlList.forEach {
                                        Firebase.storage.getReferenceFromUrl(it).delete()
                                            .addOnSuccessListener {
                                                countDelete++
                                                if (countDelete == subCategory.imageUrlList.size){
                                                    Toast.makeText(application, "Deleted successfully",
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(application, "Error deleting some images from $subCategoryName",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                Toast.makeText(application, "Deleted successfully",
                                    Toast.LENGTH_SHORT).show()
                                status = true
                            }
                            .addOnFailureListener {
                                //Error removing this subcategory
                                Toast.makeText(application, "Error deleting $subCategoryName",
                                    Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        return status
    }


    /**
     * Firebase Database functions
     */
    override fun fetchCategories(callback: (MutableList<Category>) -> Unit) {
        val categoryList: MutableList<Category> = mutableListOf()
        val snapShotKeys: MutableList<String> = mutableListOf()
        databaseReference.child("categories").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue<Category>()?.let {
                    categoryList.add(it)
                    snapShotKeys.add(snapshot.key!!)
                    //Let the view know about the changes
                    callback(categoryList)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val modifiedCategory = snapshot.getValue<Category>()
                val key = snapshot.key

                modifiedCategory?.let {newCat ->
                    val alteredIndex = snapShotKeys.indexOf(key)
                    categoryList[alteredIndex] = newCat
                }
                //Let the view know about the changes
                callback(categoryList)
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedCategory = snapshot.getValue<Category>()
                val deletedKey = snapshot.key

                removedCategory?.let {
                    val index = snapShotKeys.indexOf(deletedKey)
                    categoryList.removeAt(index)
                    snapShotKeys.removeAt(index)
                }
                //Let the view know about the changes
                callback(categoryList)
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun fetchSubCategoryByName(categoryName: String, subCategoryName: String, callback: (SubCategory) -> Unit) {
        databaseReference.child("subcategories").child(categoryName).child(subCategoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<SubCategory>()?.let {
                        callback(it)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun addCategory(category: Category, callback: (Boolean) -> Unit) {
        databaseReference.child("categories").child(category.categoryName).setValue(category)
            .addOnSuccessListener {
                //successfully, show a Toast
                Toast.makeText(application, "Added Successfully", Toast.LENGTH_SHORT).show()
                //Let the view know the operation was successful
                callback(true)
            }
            .addOnFailureListener {
                //Not Successful
                Toast.makeText(application, "Failed!", Toast.LENGTH_SHORT).show()
                //Let the view know the operation failed
                callback(true)
            }
    }

    override fun addSubCategory(subCategory: SubCategory, category: Category, callback: (Boolean) -> Unit) {
        databaseReference.child("subcategories").child(category.categoryName)
            .child(subCategory.subCategoryName).setValue(subCategory)
            .addOnSuccessListener {
                //successfully, update the category
                if (updateCategory(category, subCategory.subCategoryName, 1))
                    //If successfully, show a Toast
                    Toast.makeText(application, "Added Successfully", Toast.LENGTH_SHORT).show()
                    callback(true)
            }
            .addOnFailureListener {
                //Not Successful, let the view know and show a toast
                Toast.makeText(application, "Failed to entry", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    override fun deleteCategory(category: Category, callback: (Boolean) -> Unit) {
        databaseReference.child("categories").child(category.categoryName).removeValue()
            .addOnSuccessListener {
                Toast.makeText(application, "Deleted successfully",
                    Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener {
                Toast.makeText(application, "Error deleting ${category.categoryName}",
                    Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    override fun deleteSubCategory(category: Category, subCategoryName: String, callback: (Boolean) -> Unit) {
        val list = category.subCategoryList
        list.remove(subCategoryName)
        if (list.size >= 1)
            //the Category's subcategory list is not empty so update the category's placeholder ur;
            callback(updatePlaceHolderImageUrl(category, subCategoryName, list.shuffled()[0]))
        else
            //now fetch the subcategory and delete it
            callback(getSubCategoryAndDelete(category, subCategoryName))
    }

    //Update the database when Image upload operation is completed
    override fun onUploadCompleted(currentCategory: Category, currentSubCategory: SubCategory, totalImageUploaded: Int){
        databaseReference.child("subcategories").child(currentSubCategory.categoryName)
            .child(currentSubCategory.subCategoryName).setValue(currentSubCategory)
            .addOnSuccessListener {
                databaseReference.child("categories").child(currentCategory.categoryName).setValue(currentCategory)
                    .addOnFailureListener {
                        //Not Successful
                        Toast.makeText(application,
                            "ERROR! ${currentCategory.categoryName} was not updated: $it", Toast.LENGTH_SHORT).show()
                    }
                prepareNotification(currentCategory.categoryName, currentSubCategory.subCategoryName, totalImageUploaded)
            }
            .addOnFailureListener {
                //Show failure toast
                Toast.makeText(application,
                    "ERROR! ${currentSubCategory.subCategoryName} was not updated: $it",Toast.LENGTH_LONG).show()
            }
    }


    /**
     * Firebase Storage functions
     */
    override fun uploadToStorage(index: Int, uploadedImage: UploadedImage,
                                 categoryName: String, subCategoryName: String,
                                 progressCallback: (Int) -> Unit,
                                 onCompletionCallback: (StringImageUrl: String) -> Unit) {

        uploadedImage.imageUri.let {thisUri ->
            val imageStorageRef = storageReference.child("$categoryName/$subCategoryName/${uploadedImage.imageName}")
            imageStorageRef.putFile(thisUri)
                .addOnProgressListener {
                    //Show the Progress
                    val currentProgress = ((100.0*it.bytesTransferred)/it.totalByteCount).toInt()
                    progressCallback(currentProgress)
                }
                .addOnSuccessListener {
                    //Get the download Url from Firebase storage
                    imageStorageRef.downloadUrl.addOnSuccessListener {downloadUri ->
                        //Save the Url to the SubCategory's image url list
                        onCompletionCallback(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(application, "Failed to upload ${uploadedImage.imageName}",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Push Notification functions
     */

    //Prepares the notification to be sent to FCM servers
    //that notifies the users of new wallpapers
    private fun prepareNotification(categoryName: String, subCategoryName: String, totalImagesUploaded: Int) {
        val topic = "/topics/wallpapers" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", "New Wallpapers Added To: $categoryName")
            //Enter your notification message
            notificationBody.put("message", "$totalImagesUploaded new $subCategoryName wallpapers has been added")
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
            },
            Response.ErrorListener {
                Log.i("TAG", "onErrorResponse: Didn't work")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}









