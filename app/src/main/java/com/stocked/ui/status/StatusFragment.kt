package com.stocked.ui.status

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.stocked.MainActivity
import com.stocked.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.Socket
import kotlin.properties.Delegates

class StatusFragment : Fragment() {

    private lateinit var connectButton: Button
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
        connectButton = root.findViewById(R.id.buttonConnect)
        connectButton.setOnClickListener {
            txtIP = root.findViewById(R.id.editTextServerIP)
            txtPort = root.findViewById(R.id.editTextServerPort)
            ip = txtIP.text.toString()
            port = 0

            try {
                port = txtPort.text.toString().toInt()
            } catch (e: Exception) {
                port = 0
            }

            GlobalScope.launch(Dispatchers.Default) {
                try {
                    if (ip != "" && port != 0) {
                        val serverIP = InetAddress.getByName(ip)
                        MainActivity.socket.soTimeout = 100
                        MainActivity.socket = Socket(serverIP, port)

                        delay(100L)

                        if (MainActivity.socket.isConnected) {
                            GlobalScope.launch(Dispatchers.Main) {
                                connectButton.setBackgroundColor(Color.GREEN)
                                connectButton.text = "Connected"
                            }
                        } else {
                            GlobalScope.launch(Dispatchers.Main) {
                                connectButton.setBackgroundColor(Color.RED)
                                connectButton.text = "Not Connected"
                            }
                        }
                    }
                } catch (e: Exception) {
                    GlobalScope.launch(Dispatchers.Main) {
                        connectButton.setBackgroundColor(Color.RED)
                        connectButton.text = "Not Connected"
                    }
                }
            }
        }
        return root
    }
}