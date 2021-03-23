package com.stocked

import android.content.Intent
import android.os.Bundle
import android.system.Os.socket
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket
import java.security.MessageDigest


class LoginActivity : AppCompatActivity() {

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        var ip = ""
        var port = 0
        lateinit var user : String
        lateinit var pwdHash : String
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.toHex()
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
                user = txtuser.text.toString()
                pwdHash = txtpsswd.text.toString().toMD5()
                ip = txtip.text.toString()
                port = txtport.text.toString().toInt()

                GlobalScope.launch(Dispatchers.Default){
                    try {
                        if (ip != "") {
                            MainActivity.socket = Socket(ip, port)
                            var srvReply = "-1" // -1 = not connected
                            PrintWriter(MainActivity.socket.outputStream, true).write("check")
                            val d = async {
                                srvReply = BufferedReader(InputStreamReader(MainActivity.socket.inputStream)).readLine()
                            }
                            withTimeout(1000) { d.await() }

                            if (srvReply != "-1") {
                                PrintWriter(MainActivity.socket.outputStream, true).write("$user|$pwdHash|check")
                                withTimeout(1000) { d.await() }

                                if(srvReply == "004") {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        startMainActivity()
                                    }
                                }
                                else if(srvReply == "003")
                                {
                                    Toast.makeText(this@LoginActivity, "User e/o password errati", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                GlobalScope.launch(Dispatchers.Main){
                                    Toast.makeText(this@LoginActivity, "Impossibile connettersi al server", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else
                        {
                            // l'utente non ha inserito l'ip
                        }
                    }catch (ex: Exception){
                        GlobalScope.launch(Dispatchers.Main){
                            Toast.makeText(this@LoginActivity, ex.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            catch (ex: Exception){
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
