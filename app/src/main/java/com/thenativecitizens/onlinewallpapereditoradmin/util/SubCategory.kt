package com.thenativecitizens.onlinewallpapereditoradmin.util

data class SubCategory(
    var subCategoryID: String = "",
    var subCategoryName: String = "",
    var categoryName: String = "",
    var imageUrlList: MutableList<String> = mutableListOf(),
    var subCategoryPlaceholderImageUrl: String = ""
)