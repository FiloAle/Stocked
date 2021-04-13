package com.stocked.ui.status

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stocked.LoginActivity
import com.stocked.MainActivity
import com.stocked.R
import com.stocked.ui.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class StatusFragment : Fragment() {

    private lateinit var btnCheck: Button
    private lateinit var btnLogout: Button
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var txtStatus: TextView
    private val ip : String = LoginActivity.ip
    private val port = LoginActivity.port

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_status, container, false)

        loadingDialog = activity?.let { LoadingDialog(it) }!!
        btnCheck = root.findViewById(R.id.btnCheck)
        btnLogout = root.findViewById(R.id.btnLogout)
        txtStatus = root.findViewById(R.id.txtStatus)
        txtStatus.text = getString(R.string.connected) + " " + getString(R.string.to) + " " + ip
        btnCheck.setOnClickListener {
            GlobalScope.launch(Dispatchers.Default)
            {
                try {
                    MainActivity.socket = Socket()
                    GlobalScope.launch(Dispatchers.Main) {
                        loadingDialog.startLoadingDialog()
                    }
                    MainActivity.socket.connect(
                        InetSocketAddress(
                            LoginActivity.ip,
                            LoginActivity.port
                        ), 1000
                    )

                    if (MainActivity.socket.isConnected) {
                        txtStatus.text = getString(R.string.connected) + " " + getString(R.string.to) + " " + ip
                        GlobalScope.launch(Dispatchers.Main)
                        {
                            Toast.makeText(activity, getString(R.string.connected) + " " + getString(R.string.to) + " " + ip, Toast.LENGTH_SHORT).show()
                            loadingDialog.dismissDialog()
                        }
                    }
                } catch (ex: Exception) {
                    GlobalScope.launch(Dispatchers.Main)
                    {
                        Toast.makeText(
                            activity,
                                getString(R.string.not_connected),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    try {
                        try {
                            if (ip != "" && port != 0) {
                                val serverIP = InetAddress.getByName(ip)
                                MainActivity.socket = Socket()
                                MainActivity.socket.connect(InetSocketAddress(serverIP, port), 1000)

                                if (MainActivity.socket.isConnected) {
                                    GlobalScope.launch(Dispatchers.Main)
                                    {
                                        Toast.makeText(activity, getString(R.string.connected) + " " + getString(R.string.to) + " " + ip, Toast.LENGTH_SHORT).show()
                                        loadingDialog.dismissDialog()
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            GlobalScope.launch(Dispatchers.Main)
                            {
                                Toast.makeText(
                                    activity,
                                        getString(R.string.dest_unreachable),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            (activity as MainActivity).startLoginActivity()
                        }
                    } catch (ex: Exception) {
                        GlobalScope.launch(Dispatchers.Main)
                        {
                            Toast.makeText(activity, getString(R.string.dest_unreachable), Toast.LENGTH_SHORT).show()
                            (activity as MainActivity).startLoginActivity()
                        }
                    }
                }
            }
        }

        btnLogout.setOnClickListener {
            (activity as MainActivity).startLoginActivity()
            (activity as MainActivity).finish()
        }

        return root
    }
}