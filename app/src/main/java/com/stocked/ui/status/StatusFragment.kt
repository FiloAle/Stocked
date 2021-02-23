package com.stocked.ui.status

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.stocked.MainActivity
import com.stocked.R
import java.lang.Exception
import java.net.Socket
import java.util.function.ToIntFunction
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import kotlinx.coroutines.*
import java.net.SocketAddress

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
        connectButton.setOnClickListener(View.OnClickListener {
            txtIP= root.findViewById(R.id.editTextServerIP)
            txtPort = root.findViewById(R.id.editTextServerPort)
            ip = txtIP.text.toString()
            port = 0
            try {

                port =  txtPort.text.toString().toInt()
            }catch(e:Exception) {
                port = 0
            }


            GlobalScope.launch(Dispatchers.Main) {
                try {
                    if(ip != "" && port != 0)
                    {

                        MainActivity.socket.soTimeout = 1000
                        MainActivity.socket = Socket(ip, port)


                        if(MainActivity.socket.isConnected)
                        {

                            connectButton.setBackgroundColor(Color.GREEN)
                            connectButton.text="Connected"
                        }
                    }
                }catch (e:Exception){
                    connectButton.setBackgroundColor(Color.RED)
                    connectButton.text="Not Connected"

                }

            }



        })

        return root
    }
}