package com.example.appfire

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        //SplashScreen con el logo de la aplicación
        setTheme(R.style.Theme_AppFire)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Firebase Analytics
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Firebase Integrado")
        analytics.logEvent("InitScreen", bundle)

        //Configurando MainActivity
        configurarMain()
        sesion()

    }
    override fun onStart() {
        super.onStart()
        esp.visibility = View.VISIBLE
    }

    private fun sesion(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email",null)
        val provider = prefs.getString("provider", null)
        if(email != null && provider != null){
            esp.visibility = View.INVISIBLE
            inicio(email, ProviderType.valueOf(provider))
        }
    }
    private fun configurarMain(){
        title = "Iniciar Sesión"

        registrarButton.setOnClickListener{
            if (EmailText.text.isNotEmpty() && PasswordText.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(EmailText.text.toString(), PasswordText.text.toString()).addOnCompleteListener{
                            if (it.isSuccessful){
                                inicio(it.result?.user?.email?:"" ,ProviderType.BASIC)
                            }else{
                                alerta()
                            }
                }
            }
        }

        accederButton.setOnClickListener{
            if (EmailText.text.isNotEmpty() && PasswordText.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(EmailText.text.toString(), PasswordText.text.toString()).addOnCompleteListener{
                    if (it.isSuccessful){
                        inicio(it.result?.user?.email?:"" , ProviderType.BASIC)
                    }else{
                        alerta()
                    }
                }
            }
        }

        googleButton.setOnClickListener{
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
            val googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
        anonimoButton.setOnClickListener{
            FirebaseAuth.getInstance().signInAnonymously()
            inicio("email" ,ProviderType.ANONIMO)
        }
    }

    private fun alerta(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR")
        builder.setMessage("Se ha producido un error al autentificar")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun inicio(email: String, provider: ProviderType){
        val iniciointent = Intent(this, homeActi::class.java).apply {
            putExtra("email", email)
        }
        startActivity(iniciointent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                inicio("email",ProviderType.GOOGLE)
                            } else {
                                alerta()
                            }
                        }
                }
            }catch (e: ApiException){
                alerta()
            }
        }
    }


}
