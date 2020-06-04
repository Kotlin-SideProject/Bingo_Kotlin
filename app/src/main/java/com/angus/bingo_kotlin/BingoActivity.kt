package com.angus.bingo_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_bingo.*
import kotlinx.android.synthetic.main.single_button.view.*

class BingoActivity : AppCompatActivity(), View.OnClickListener {
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

        val numberMap = mutableMapOf<Int, Int>()
        for(i in 0..24){
            numberMap.put(buttons.get(i).number, i)
        }
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
//                holder.button.isEnabled = !model
                holder.button.number = buttons.get(position).number
                holder.button.isEnabled = !buttons.get(position).picked
                holder.button.setOnClickListener(this@BingoActivity)
            }

             override fun onChildChanged(
                 type: ChangeEventType,
                 snapshot: DataSnapshot,
                 newIndex: Int,
                 oldIndex: Int
             ) {
                 super.onChildChanged(type, snapshot, newIndex, oldIndex)
                 if(type == ChangeEventType.CHANGED){
                     val number = snapshot.key?.toInt()
                     Log.d(TAG, "number: ${number}")
                     val position = numberMap.get(number)
                     Log.d(TAG, "position: ${position}");
                     val picked = snapshot.value as Boolean
                     Log.d(TAG, "picked:${picked} ");
                     buttons.get(position!!).picked = picked
                     val holder : numberHolder = bingo_recycler.findViewHolderForAdapterPosition(position!!) as numberHolder
                     holder.button.isEnabled = !picked
                 }
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

    override fun onClick(v: View?) {
        val number = (v as NumberButton).number
        FirebaseDatabase.getInstance().getReference("rooms")
            .child(roomId)
            .child("numbers")
            .child(number.toString())
            .setValue(true)
    }
}
