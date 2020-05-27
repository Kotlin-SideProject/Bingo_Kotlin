package com.angus.bingo_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_bingo.*

class BingoActivity : AppCompatActivity() {
    companion object{
        val TAG = BingoActivity::class.java.simpleName
    }

    private var isCreator: Boolean = false
    lateinit var roomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bingo)
         roomId = intent.getStringExtra("ROOM_ID")
         isCreator = intent.getBooleanExtra("IS_CREATOR", false)
//        Log.d(TAG, ": ${roomId + isCreator}");
        for (i in 1..25){
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("numbers")
                .child(i.toString())
                .setValue(false)
        }
        val buttons = mutableListOf<NumberButton>()
        for (i in 0..24){
            val  button = NumberButton(this@BingoActivity)
            button.number = i+1
            buttons.add(button)
        }
        buttons.shuffle()
        //RecyclerView
        bingo_recycler.setHasFixedSize(true)
        bingo_recycler.layoutManager = GridLayoutManager(this@BingoActivity, 5)

    }
}
