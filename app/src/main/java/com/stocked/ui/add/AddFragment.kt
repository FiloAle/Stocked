package com.stocked.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stocked.LoginActivity
import com.stocked.MainActivity
import com.stocked.R
import kotlinx.coroutines.*
import java.io.*
import java.lang.Runnable

class AddFragment : Fragment() {

    private lateinit var addView : View
    private lateinit var addViewModel: AddViewModel
    private lateinit var replyCommunication : String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        addViewModel = ViewModelProvider(this).get(AddViewModel::class.java)
        addView = inflater.inflate(R.layout.fragment_add, container, false)

        val send : Button = addView.findViewById(R.id.btnAdd)
        send.setOnClickListener{
            btnSendAction()
        }
        return addView
    }

    private fun interactWithSocket(format : String) : Unit{
        val apollo = DataOutputStream(MainActivity.socket.getOutputStream())
        apollo.write(format.toByteArray())
        apollo.flush()

        // Ricezione risposta dal server
        var reply : String
        val reader = DataInputStream(MainActivity.socket.getInputStream())
        var msg : ByteArray = ByteArray(1024)
        reader.read(msg)
        reply = msg.toString(Charsets.US_ASCII)
        val rgx = Regex("[^A-Za-z0-9 |]")
        reply = rgx.replace(reply, "")
        replyCommunication = reply
        return
    }

    private fun btnSendAction(){
        val productCode : String? = addView.findViewById<EditText>(R.id.dttCode).text.toString()
        val productName : String? = addView.findViewById<EditText>(R.id.dttProductName).text.toString()
        val productAmount : String? = addView.findViewById<EditText>(R.id.dttProductQuantity).text.toString()
        var checkSend : Boolean = true
        var message : String = ""

        // Check of digits number
        if(productCode?.length  in arrayOf(5, 8, 13)){
            /*if(productCode?.toIntOrNull() == null){
                checkSend = false;
                message += "Il codice deve essere numerico\n"
            }*/
        }
        else{
            checkSend = false
            message += "Il codice non ha il numero di cifre richieste (5, 8, 13)\n"
        }

        // Check the name
        if(productName == ""){
            checkSend = false
            message += "Inserire il nome del prodotto\n"
        }

        // Check of quantity
        if(productAmount?.length?.compareTo(0) == 1){
            if(productAmount.toIntOrNull() != null && productAmount.toInt() < 0){
                checkSend = false
                message += "La quantità deve essere positiva\n"
            }
            else if (productAmount.toIntOrNull() == null){
                checkSend = false
                message += "La quantità deve essere un intero\n"
            }
        }
        else{
            checkSend = false
            message += "Inserire una quantità\n"
        }

        if(checkSend){
            message = "Dati corretti"

            try {
                // Invio pacchetto dati di aggiunta
                var format : String = LoginActivity.user + "|" + LoginActivity.pwdHash + "|new|$productCode|$productAmount|$productName"
                var reply : String = ""

                GlobalScope.launch(Dispatchers.Default){
                    interactWithSocket(format)
                }

                Thread.sleep(100)

                when (replyCommunication) {
                    "002" -> {
                        message = "Dati corretti"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                    "006" -> {
                        Toast.makeText(requireContext(), "Nome prodotto già presente", Toast.LENGTH_SHORT).show()
                    }
                    "007" -> {
                        Toast.makeText(requireContext(), "Codice prodotto già presente", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Operazione Rifiutata", Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (ex: Exception){
                var message1 = ex.message
                Toast.makeText(requireContext(), ex.message.toString(), Toast.LENGTH_LONG).show()
            }

        }
        else {
            message = message.dropLast(1)
            Toast.makeText(requireContext(), "Invio annullato:\n"+message, Toast.LENGTH_LONG).show()
        }
    }
}