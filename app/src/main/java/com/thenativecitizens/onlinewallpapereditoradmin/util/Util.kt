package com.thenativecitizens.onlinewallpapereditoradmin.util


//Converts List to String and String to List
object ListAndStringConverter{
    fun listToString(list: List<Any>): String{
        var str = ""
        if(list.isNotEmpty()){
            for (i in list.indices){
                str += list[i]
                if (i != list.size -1)
                    str += ","
            }
        }
        return str
    }
    //fun stringToList(): MutableList<String>
}