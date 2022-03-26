package com.thenativecitizens.onlinewallpapereditoradmin.model

import android.net.Uri

data class UploadedImage(
    var imageName: String = "",
    var imageUri: Uri,
    var imageUploadProgress: Int = 0,
)