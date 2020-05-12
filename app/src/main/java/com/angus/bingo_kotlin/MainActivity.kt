package com.angus.bingo_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
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
        }?:signUp()
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
