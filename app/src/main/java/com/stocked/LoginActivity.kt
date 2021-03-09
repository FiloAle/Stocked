package com.stocked

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.InetAddress
import java.net.Socket

class LoginActivity : AppCompatActivity() {

    fun startMainActivity(view: View){
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var connectButton : Button = findViewById(R.id.btnLogin)
        var txtuser : EditText = findViewById(R.id.txtUser)
        var txtpsswd : EditText = findViewById(R.id.txtPassword)
        var txtip : EditText = findViewById(R.id.txtServerIP)
        var txtport : EditText = findViewById(R.id.txtServerPort)

        connectButton.setOnClickListener{
            try {
                var user:String = txtuser.text.toString()
                var psswd:String=txtpsswd.text.toString()
                var ip:String = txtip.text.toString()
                var port:Int=0

                try {
                    port = txtport.text.toString().toInt()
                } catch (e: Exception) {
                    port = 0
                }
                GlobalScope.launch(Dispatchers.Default){
                    try {
                        if (ip != "" && port != 0) {
                            val serverIP = InetAddress.getByName(ip)
                            MainActivity.socket = Socket(serverIP, port)

                            if (MainActivity.socket.isConnected) {
                                GlobalScope.launch(Dispatchers.Main){

                                    startMainActivity(view = View(this@LoginActivity))
                                }
                            } else {
                                GlobalScope.launch(Dispatchers.Main){

                                    Toast.makeText( this@LoginActivity, "Impossibile connettersi al server", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }catch (ex:Exception){

                        GlobalScope.launch(Dispatchers.Main){

                            Toast.makeText(this@LoginActivity, ex.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }



            }
            catch (ex :Exception){
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show()
            }

        }


    }



}
