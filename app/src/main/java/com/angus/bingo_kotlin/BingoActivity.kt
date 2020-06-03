package com.angus.bingo_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_bingo.*
import kotlinx.android.synthetic.main.single_button.view.*

class BingoActivity : AppCompatActivity() {
    companion object{
        val TAG = BingoActivity::class.java.simpleName
        val STATUS_INIT = 0
        val STATUS_CREATED = 1
        val STATUS_JOINED = 2
        val STATUS_CREATOR_TURN = 3
        val STATUS_JOINER_TURN = 4
        val STATUS_CREATOR_BINGO = 5
        val STATUS_JOINER_BINGO = 6
    }

    lateinit var adapter: FirebaseRecyclerAdapter<Boolean, BingoActivity.numberHolder>
    private var isCreator: Boolean = false
    lateinit var roomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bingo)
         roomId = intent.getStringExtra("ROOM_ID")
         isCreator = intent.getBooleanExtra("IS_CREATOR", false)
//        Log.d(TAG, ": ${roomId + isCreator}");
        val buttons = mutableListOf<NumberButton>()
        if(isCreator){
            for (i in 1..25){
                FirebaseDatabase.getInstance().getReference("rooms")
                    .child(roomId)
                    .child("numbers")
                    .child(i.toString())
                    .setValue(false)
            }
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("status")
                .setValue(STATUS_CREATED)
        }else{
            FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("status")
                .setValue(STATUS_JOINED)
        }
        for (i in 0..24){
            val  button = NumberButton(this@BingoActivity)
            button.number = i+1
            buttons.add(button)
        }
        buttons.shuffle()
        //RecyclerView
        bingo_recycler.setHasFixedSize(true)
        bingo_recycler.layoutManager = GridLayoutManager(this@BingoActivity, 5)

        //Query  from FirebaseDatabase
        val query = FirebaseDatabase.getInstance().getReference("rooms")
            .child(roomId)
            .child("numbers")
            .orderByKey()
        val options = FirebaseRecyclerOptions.Builder<Boolean>()
            .setQuery(query, Boolean::class.java)
            .build()
         adapter = object : FirebaseRecyclerAdapter<Boolean, numberHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): numberHolder {
                val view = LayoutInflater.from(this@BingoActivity)
                    .inflate(R.layout.single_button, parent, false)
                return numberHolder(view)
            }

            override fun onBindViewHolder(holder: numberHolder, position: Int, model: Boolean) {
                holder.button.text = buttons[position].number.toString()
                holder.button.isEnabled = !model
            }
        }
        bingo_recycler.adapter = adapter

    }
    class numberHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        lateinit var button : NumberButton
        init {
            button = itemView.button
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
