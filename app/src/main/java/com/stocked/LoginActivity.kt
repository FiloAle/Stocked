package com.stocked

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stocked.ui.LoadingDialog
import kotlinx.coroutines.*
import java.io.*
import java.lang.NumberFormatException
import java.net.Socket
import java.security.MessageDigest
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var loadingDialog: LoadingDialog

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
        this.supportActionBar?.hide()
        this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.window.statusBarColor = getColor(R.color.blue_400)
        loadingDialog = LoadingDialog(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val connectButton : Button = findViewById(R.id.btnLogin)
        val txtip: EditText = findViewById(R.id.txtServerIP)
        val txtport: EditText = findViewById(R.id.txtServerPort)
        val txtuser : EditText = findViewById(R.id.txtUser)
        val txtpsswd : EditText = findViewById(R.id.txtPassword)

        connectButton.setOnClickListener{
            try {
                loadingDialog.startLoadingDialog()
                user = txtuser.text.toString()
                pwdHash = txtpsswd.text.toString().toMD5()
                ip = txtip.text.toString()
                port = txtport.text.toString().toInt()

                GlobalScope.launch(Dispatchers.Default){
                    try {
                        if (ip != "") {
                            val f = async {
                                MainActivity.socket = Socket(ip, port)
                            }
                            withTimeout(2500){ f.await() }

                            var srvReply = "-1" // -1 = not connected
                            val reader = DataInputStream(MainActivity.socket.getInputStream())
                            val writer = DataOutputStream(MainActivity.socket.getOutputStream())
                            writer.write("check".toByteArray())
                            writer.flush()

                            val d = async {
                                val msg = ByteArray(1024)
                                reader.read(msg)
                                srvReply = msg.toString(Charsets.US_ASCII)
                                val rgx = Regex("[^A-Za-z0-9 |[-]]")
                                srvReply = rgx.replace(srvReply, "")
                            }
                            withTimeout(1000) { d.await() }

                            if (srvReply != "-1") {
                                writer.write("$user|$pwdHash|check".toByteArray())
                                writer.flush()

                                val msg = ByteArray(1024)
                                reader.read(msg)
                                srvReply = msg.toString(Charsets.US_ASCII)
                                val rgx = Regex("[^A-Za-z0-9 |]")
                                srvReply = rgx.replace(srvReply, "")

                                if(srvReply == "004") {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        startMainActivity()
                                    }
                                    loadingDialog.dismissDialog()
                                }
                                else if(srvReply == "003")
                                {
                                    loadingDialog.dismissDialog()
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(this@LoginActivity, getString(R.string.wrong_credentials), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                loadingDialog.dismissDialog()
                                GlobalScope.launch(Dispatchers.Main){
                                    Toast.makeText(this@LoginActivity, getString(R.string.cant_connect), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else
                        {
                            loadingDialog.dismissDialog()
                            GlobalScope.launch(Dispatchers.Main){
                                Toast.makeText(this@LoginActivity, getString(R.string.missing_ip), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }catch (ex: Exception){
                        loadingDialog.dismissDialog()
                        GlobalScope.launch(Dispatchers.Main){
                            Toast.makeText(this@LoginActivity, getString(R.string.dest_unreachable), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            catch (ex: NumberFormatException){
                loadingDialog.dismissDialog()
                Toast.makeText(this, getString(R.string.missing_port), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
