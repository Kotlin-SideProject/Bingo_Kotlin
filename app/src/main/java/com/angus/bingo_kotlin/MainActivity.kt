package com.angus.bingo_kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.room_row.view.*
import java.util.*

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, View.OnClickListener {

    companion object{
        var TAG = MainActivity::class.java.simpleName
        var RC_SIGN_IN = 100
    }

    private lateinit var adapter: FirebaseRecyclerAdapter<GameRoom, GameHolder>
    var member: Member? = null
    var avatarIds = intArrayOf(
        R.drawable.avatar_0,
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nickname.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.let {user ->
                showNickDialog(user.uid, nickname.text.toString())
            }
        }
        group_avatars.visibility = View.GONE
        avatar.setOnClickListener {
            group_avatars.visibility = if (group_avatars.visibility == View.GONE)
                View.VISIBLE else View.GONE
        }
        avatar_0.setOnClickListener(this)
        avatar_1.setOnClickListener(this)
        avatar_2.setOnClickListener(this)
        avatar_3.setOnClickListener(this)
        avatar_4.setOnClickListener(this)
        avatar_5.setOnClickListener(this)
        avatar_6.setOnClickListener(this)
        fab.setOnClickListener {
            val roomEdit =  EditText(this)
            roomEdit.setText("wellcome")
            AlertDialog.Builder(this)
                .setTitle("Game Room")
                .setMessage("Please enter your room title")
                .setView(roomEdit)
                .setPositiveButton("OK") { dialog, which ->
                    FirebaseDatabase.getInstance().getReference("users")
                    var room = GameRoom(roomEdit.text.toString(), member)
                    FirebaseDatabase.getInstance().getReference("rooms")
                        .push()
                        .setValue(room, object : DatabaseReference.CompletionListener {
                            override fun onComplete(error: DatabaseError?, databaseReference: DatabaseReference) {
                                val roomId = databaseReference.key
                                val bingoIntent = Intent(this@MainActivity, BingoActivity::class.java)
                                bingoIntent.putExtra("ROOM_ID", roomId)
                                bingoIntent.putExtra("IS_CREATOR", true)
                                startActivity(bingoIntent)
                            }

                        })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        //recyclerView setting
        main_recycler.setHasFixedSize(true)
        main_recycler.layoutManager = LinearLayoutManager(this@MainActivity)
        var query = FirebaseDatabase.getInstance().getReference("rooms").limitToLast(30)
        val options = FirebaseRecyclerOptions.Builder<GameRoom>()
            .setQuery(query, GameRoom::class.java)
            .build()
         adapter = object : FirebaseRecyclerAdapter<GameRoom, GameHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHolder {
             val view = LayoutInflater.from(this@MainActivity)
                 .inflate(R.layout.room_row, parent, false)
                return GameHolder(view)
            }

            override fun onBindViewHolder(holder: GameHolder, position: Int, model: GameRoom) {
                holder.image.setImageResource(avatarIds[model.init!!.avatarId])
                holder.roomTitle.setText(model.title)
            }

        }
        main_recycler.adapter = adapter
    }
    class GameHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val image:ImageView = itemView.room_image
        val roomTitle:TextView = itemView.room_text
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(this)
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
        adapter.stopListening()
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        auth.currentUser?.also {
            Log.d(TAG, ": ${it.email}/${it.uid}")
            it.displayName?.run {
                FirebaseDatabase.getInstance().getReference("users")
                        .child(it.uid)
                        .child("displayName")
                        .setValue(this)
                        .addOnCompleteListener { Log.d(TAG, ": done") }

                FirebaseDatabase.getInstance().getReference("users")
                    .child(it.uid)
                    .child("uid")
                    .setValue(it.uid)
            }
            FirebaseDatabase.getInstance().getReference("users")
                .child(it.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                         member = dataSnapshot.getValue(Member::class.java)
                        member?.nickName?.also { nick ->
                            nickname.setText(nick)
                        }?:showNickDialog(it)
                        member?.avatarId?.let { avatarId ->
                            avatar.setImageResource(avatarIds[member!!.avatarId])
                        }
                    }
                })

        }?:signUp()
    }


    private fun showNickDialog(uid : String, nickname : String){
        val nickEdit =  EditText(this)
        nickEdit.setText(nickname)
        AlertDialog.Builder(this)
            .setTitle("Your nickname?")
            .setMessage("Please enter your nickname")
            .setView(nickEdit)
            .setPositiveButton("OK") { dialog, which ->
                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("nickName")
                    .setValue(nickEdit.text.toString())
            }.show()
    }
    private fun showNickDialog(user: FirebaseUser) {
        val uid = user.uid
        val nickname = user.displayName
        showNickDialog(uid, nickname!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_menu_signout){
            AuthUI.getInstance().signOut(this)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signUp() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build()))
//                        .setIsSmartLockEnabled(false)
                        .build()
                , RC_SIGN_IN)
    }

    override fun onClick(v: View?) {
        var selected = when (v?.id){
            R.id.avatar_0 -> 0
            R.id.avatar_1 -> 1
            R.id.avatar_2 -> 2
            R.id.avatar_3 -> 3
            R.id.avatar_4 -> 4
            R.id.avatar_5 -> 5
            R.id.avatar_6 -> 6
            else -> 0
        }
        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .child("avatarId")
            .setValue(selected)
        group_avatars.visibility = View.GONE
    }
}
