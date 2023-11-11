package com.example.googlelogintemplate

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
//import com.example.googlelogintemplate.MainActivity.Companion.EXTRA_EMAIL
//import com.example.googlelogintemplate.MainActivity.Companion.EXTRA_IMAGE
//import com.example.googlelogintemplate.MainActivity.Companion.EXTRA_NAME
import com.example.googlelogintemplate.databinding.ActivityGoogleSignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import java.util.concurrent.Executors


class GoogleSignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoogleSignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textDisplayName.text = intent.getStringExtra("EXTRA_NAME")
        binding.textDisplayEmail.text = intent.getStringExtra("EXTRA_EMAIL")

        // Declaring executor to parse the URL
        val executor = Executors.newSingleThreadExecutor()

        // Once the executor parses the URL and receives the image, handler will load it in the ImageView
        val handler = Handler(Looper.getMainLooper())

        // Initializing the image
        var image: Bitmap? = null

        // Only for Background process (can take time depending on the Internet speed)
        executor.execute {

            // Image URL
            val imageURL = intent.getStringExtra("EXTRA_IMAGE")

            // Tries to get the image and post it in the ImageView with the help of Handler
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)

                // Only for making changes in UI
                handler.post {
                    binding.userImageView.setImageBitmap(image)
                }
            }

            // If the URL doesnot point to image or any other kind of failure
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.logout.setOnClickListener {
            val authStateListener = AuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser == null) {
                    //Do anything here which needs to be done after sign-out is complete
                    signOutComplete()
                } else {
                    //do nothing
                }
            }
//            //Init and attach
            val firebaseAuth = FirebaseAuth.getInstance()
            val googleSignInClient = GoogleSignIn.getClient(applicationContext, GoogleSignInOptions.DEFAULT_SIGN_IN )
            firebaseAuth.addAuthStateListener(authStateListener)
//            //Call signOut()
            firebaseAuth.signOut()
            googleSignInClient.signOut()

        }


    }

    private fun signOutComplete(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

}