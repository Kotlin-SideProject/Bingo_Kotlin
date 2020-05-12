package com.angus.bingo_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    companion object{
        var TAG = MainActivity::class.java.simpleName
        var RC_SIGN_IN = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        auth.currentUser?.also {
            Log.d(TAG, ": ${it.email}/${it.uid}")
            it.displayName?.run {
                FirebaseDatabase.getInstance().getReference("users")
                        .child(it.uid)
                        .child("displayName")
                        .setValue(this)
                        .addOnCompleteListener { Log.d(TAG, ": done"); }
            }
            FirebaseDatabase.getInstance().getReference("users")
                .child(it.uid)
                .child("nickName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.value?.also { nick ->
                            Log.d(TAG, "nickname: ${nick}");
                        }?:showNickDialog(it)
                    }
                })
        }?:signUp()
    }

    private fun showNickDialog(user: FirebaseUser) {
        val nickEdit =  EditText(this)
        nickEdit.setText(user.displayName)
        AlertDialog.Builder(this)
            .setTitle("Your nickname?")
            .setMessage("Please enter your nickname")
            .setView(nickEdit)
            .setPositiveButton("OK") { dialog, which ->
                FirebaseDatabase.getInstance().getReference("users")
                    .child(user.uid)
                    .child("nickName")
                    .setValue(nickEdit.text.toString())
            }.show()
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
}
