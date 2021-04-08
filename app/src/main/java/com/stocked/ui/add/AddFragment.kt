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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

class AddFragment : Fragment() {

    private lateinit var addView : View
    private lateinit var addViewModel: AddViewModel

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

    private fun btnSendAction(){
        var productCode : String? = addView.findViewById<EditText>(R.id.dttCode).text.toString()
        var productName : String? = addView.findViewById<EditText>(R.id.dttProductName).text.toString()
        var productAmount : String? = addView.findViewById<EditText>(R.id.dttProductQuantity).text.toString()
        var checkSend : Boolean = true
        var message : String = ""

        // Check of digits number
        if(productCode?.length  in arrayOf(5, 8, 13)){
            if(productCode?.toIntOrNull() == null){
                checkSend = false;
                message += "Il codice deve essere numerico\n"
            }
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
            else{
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

            // Invio pacchetto dati di aggiunta
            PrintWriter(MainActivity.socket.outputStream, true).write(LoginActivity.user+"|"+LoginActivity.pwdHash+"|new|"+productCode+"|"+productAmount+"|"+productName)

            // Ricezione risposta dal server
            val reply = BufferedReader(InputStreamReader(MainActivity.socket.getInputStream())).readLine()

            when (reply) {
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
        }
        else {
            message = message.dropLast(1)
            Toast.makeText(requireContext(), "Invio annullato:\n"+message, Toast.LENGTH_LONG).show()
        }
    }
}