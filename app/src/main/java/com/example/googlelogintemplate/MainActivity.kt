package com.example.googlelogintemplate

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.googlelogintemplate.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val REQUEST_CODE_SIGN_IN = 0


class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityMainBinding

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        //Configure Google Sign-in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.REQUEST_ID_TOKEN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInButton.setOnClickListener {
            signIn()
        }

        binding.registerAccountButton.setOnClickListener {
            registerUser()
        }

        binding.logInButton.setOnClickListener {
            loginUser()
        }

        binding.alreadyHaveTextView.setOnClickListener {
            binding.registerAccountButton.visibility = View.INVISIBLE
            binding.logInButton.visibility = View.VISIBLE
            binding.alreadyHaveTextView.visibility = View.INVISIBLE
            binding.createAccountTextView.visibility = View.VISIBLE

        }

        binding.createAccountTextView.setOnClickListener {
            binding.registerAccountButton.visibility = View.VISIBLE
            binding.logInButton.visibility = View.INVISIBLE
            binding.alreadyHaveTextView.visibility = View.VISIBLE
            binding.createAccountTextView.visibility = View.INVISIBLE
        }




    }

    private fun registerUser() {
        val email = binding.editTextTextEmailAddress.text.toString()
        val password = binding.editTextTextPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loginUser() {
        val email = binding.editTextTextEmailAddress.text.toString()
        val password = binding.editTextTextPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun checkLoggedInState() {
        if (firebaseAuth.currentUser == null) { // not logged in
            Toast.makeText(this@MainActivity, "You are not logged in", Toast.LENGTH_LONG).show()
        } else {
           updateUI(firebaseAuth.currentUser)
        }
    }


    private fun signIn(){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if(requestCode == REQUEST_CODE_SIGN_IN){
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try{
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.e(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException){
                    // Google Sign In failed, ui UI appropriately
                    Log.e(TAG, "Google Sign in Failed:", e)

                }
            }
        }
        private fun firebaseAuthWithGoogle(idToken: String) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = firebaseAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithCredential:failure", task.exception)
                        updateUI(null)
                    }
                }
        }

        private fun updateUI(user: FirebaseUser?) {
            if (user != null) {
                startIntent(user)
            }
        }

    private fun startIntent(user: FirebaseUser?){
        val intent = Intent(applicationContext, GoogleSignInActivity::class.java)
        intent.putExtra("EXTRA_NAME", user?.displayName)
        intent.putExtra("EXTRA_EMAIL", user?.email)
        intent.putExtra("EXTRA_IMAGE", user?.photoUrl.toString())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
        val user = firebaseAuth.currentUser
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
           startIntent(user)
        }
    }


}