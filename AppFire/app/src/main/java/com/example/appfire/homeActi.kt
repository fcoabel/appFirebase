package com.example.appfire

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*

enum class ProviderType{
    BASIC, GOOGLE, ANONIMO
}

class homeActi : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Guardar sesión

        val bundle = intent.extras
        val email = bundle?.getString("email")

        confHome(email?: "")

        val sesion = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        sesion.putString("email", email)
        sesion.apply()


    }

    private fun confHome(email: String){
        title = "INICIO"
        EmailText.text = email

        salirButton.setOnClickListener{
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }
}