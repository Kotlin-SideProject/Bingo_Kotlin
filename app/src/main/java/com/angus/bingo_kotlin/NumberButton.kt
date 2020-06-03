package com.angus.bingo_kotlin

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class NumberButton @JvmOverloads constructor(context: Context,
                                             attributeSet: AttributeSet? = null) : AppCompatButton(context, attributeSet){

    var number = 0
    var picked : Boolean = false
    var position = 0

}