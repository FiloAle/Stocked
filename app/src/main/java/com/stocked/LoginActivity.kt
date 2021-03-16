package com.stocked

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.Socket

class LoginActivity : AppCompatActivity() {

    private fun startMainActivity(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        var ip = ""
        var port = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val connectButton : Button = findViewById(R.id.btnLogin)
        val txtip: EditText = findViewById(R.id.txtServerIP)
        val txtport: EditText = findViewById(R.id.txtServerPort)
        val txtuser : EditText = findViewById(R.id.txtUser)
        val txtpsswd : EditText = findViewById(R.id.txtPassword)

        connectButton.setOnClickListener{
            try {
                var user:String = txtuser.text.toString()
                var psswd:String = txtpsswd.text.toString()
                ip = txtip.text.toString()
                port = 0

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
