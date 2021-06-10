package com.thenativecitizens.onlinewallpapereditoradmin.util

data class Category(
    var categoryID: String = "",
    var categoryName: String = "",
    var subCategoryList: MutableList<String> = mutableListOf(),
    var categoryImagePlaceholderUrl: String = "",
)