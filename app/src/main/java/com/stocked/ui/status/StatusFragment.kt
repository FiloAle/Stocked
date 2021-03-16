package com.stocked.ui.status

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stocked.LoginActivity
import com.stocked.MainActivity
import com.stocked.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.properties.Delegates


class StatusFragment : Fragment() {

    private lateinit var btnCheck: Button
    private lateinit var txtIP  : EditText
    private lateinit var txtPort : EditText
    private lateinit var ip : String
    private var port by Delegates.notNull<Int>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_status, container, false)

        btnCheck = root.findViewById(R.id.btnCheck)
        btnCheck.setOnClickListener(View.OnClickListener {
            GlobalScope.launch(Dispatchers.Default)
            {
                try {
                    if (MainActivity.socket.getInputStream().read() != -1) {
                        GlobalScope.launch(Dispatchers.Main)
                        {
                            Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (ex: Exception) {
                    GlobalScope.launch(Dispatchers.Main)
                    {
                        Toast.makeText(activity, "Not connected, reconnecting...", Toast.LENGTH_SHORT).show()
                    }
                    try {
                        //val txtuser: EditText = root.findViewById(R.id.txtUser)
                        //val txtpsswd: EditText = root.findViewById(R.id.txtPassword)
                        //var user: String = txtuser.text.toString()
                        //var psswd: String = txtpsswd.text.toString()
                        ip = LoginActivity.ip
                        port = LoginActivity.port

                        try {
                            if (ip != "" && port != 0) {
                                val serverIP = InetAddress.getByName(ip)
                                MainActivity.socket = Socket()
                                MainActivity.socket.connect(InetSocketAddress(serverIP, port), 1000)

                                if (MainActivity.socket.isConnected) {
                                    GlobalScope.launch(Dispatchers.Main)
                                    {
                                        Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            port = 0
                            GlobalScope.launch(Dispatchers.Main)
                            {
                                Toast.makeText(activity, "Destination unreachable", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (ex: Exception) {
                        GlobalScope.launch(Dispatchers.Main)
                        {
                            Toast.makeText(activity, "Destination unreachable", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
        return root
    }
}