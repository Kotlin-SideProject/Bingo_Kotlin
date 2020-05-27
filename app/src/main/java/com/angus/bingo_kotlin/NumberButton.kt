package com.angus.bingo_kotlin

import android.content.Context
import androidx.appcompat.widget.AppCompatButton
import java.util.jar.Attributes

class NumberButton(context: Context) : AppCompatButton(context){
    constructor(context: Context, attrs:Attributes) : this(context)
    var number = 0
    var picked : Boolean = false
    var position = 0

}