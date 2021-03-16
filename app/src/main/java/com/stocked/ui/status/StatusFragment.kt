package com.stocked.ui.status

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.*
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
import java.net.InetAddress
import java.net.SocketAddress

class StatusFragment : Fragment() {

    private lateinit var connectButton: Button
    private lateinit var txtIP  : EditText
    private lateinit var txtPort : EditText
    private lateinit var ip : String
    private var port by Delegates.notNull<Int>()
    private lateinit var myMenu: Menu

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_main_drawer, menu)
        myMenu = menu
    }


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


            GlobalScope.launch(Dispatchers.Default) {
                try {
                    if(ip != "" && port != 0)
                    {

                        var ipaddr = InetAddress.getByName(ip)
                        MainActivity.socket = Socket(ipaddr, port)



                        if(MainActivity.socket.isConnected)
                        {
                            GlobalScope.launch(Dispatchers.Main){
                                connectButton.setBackgroundColor(Color.GREEN)
                                connectButton.text="Connected"
                                var inv : MenuItem = myMenu.getItem(0)
                                //inv.isEnabled = true
                            }

                        }
                        else
                        {
                            GlobalScope.launch(Dispatchers.Main){
                                connectButton.setBackgroundColor(Color.RED)
                                connectButton.text="Not Connected"
                            }
                        }
                    }
                }catch (e:Exception){

                    GlobalScope.launch(Dispatchers.Main){
                        connectButton.setBackgroundColor(Color.RED)
                        connectButton.text="Not Connected"
                    }
                }

            }



        })

        return root
    }
}