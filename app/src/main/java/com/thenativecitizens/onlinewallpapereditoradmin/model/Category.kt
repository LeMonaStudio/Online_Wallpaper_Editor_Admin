package com.thenativecitizens.onlinewallpapereditoradmin.model

data class Category(
    var categoryID: String = "",
    var categoryName: String = "",
    var subCategoryList: MutableList<String> = mutableListOf(),
    var categoryImagePlaceholderUrl: String = "",
)