package com.thenativecitizens.onlinewallpapereditoradmin.util

import android.net.Uri

data class UploadedImage(
    var imageName: String = "",
    var imageUri: Uri,
    var imageUploadProgress: Int = 0,
)