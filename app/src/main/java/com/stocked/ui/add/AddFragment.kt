package com.stocked.ui.add

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
import java.io.DataInputStream
import java.io.DataOutputStream

class AddFragment : Fragment() {

    private lateinit var addView : View
    private var replyCommunication : String = "" // Variabile di passaggio dati tra thread secondario e mainthread

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        addView = inflater.inflate(R.layout.fragment_add, container, false)

        val send : Button = addView.findViewById(R.id.btnAdd)
        send.setOnClickListener{
            btnSendAction()
        }
        return addView
    }

    // Procedura di invio dati al server e ricezione del codice
    // Viene valorizzata una global variable per passare l'informazione al metodo che dovrà utilizzarla
    private fun interactWithSocket(format : String) : Unit{
        val apollo = DataOutputStream(MainActivity.socket.getOutputStream())
        apollo.write(format.toByteArray())
        apollo.flush() // Invio

        // Ricezione risposta dal server
        val reader = DataInputStream(MainActivity.socket.getInputStream())
        val msg = ByteArray(1024)
        reader.read(msg)
        var reply = msg.toString(Charsets.US_ASCII)
        val rgx = Regex("[^A-Za-z0-9 |]")
        reply = rgx.replace(reply, "")
        replyCommunication = reply
        return
    }

    private fun btnSendAction(){

        // Prende i dati inseriti dall'utente
        val productCode : String = addView.findViewById<EditText>(R.id.dttCode).text.toString()
        val productName : String = addView.findViewById<EditText>(R.id.dttProductName).text.toString()
        val productAmount : String = addView.findViewById<EditText>(R.id.dttProductQuantity).text.toString()

        if (productCode == ""){
            Toast.makeText(requireContext(), "codice prodotto mancante", Toast.LENGTH_SHORT).show()
            return
        } else if (productName == "") {
            Toast.makeText(requireContext(), getString(R.string.missing_prd_name), Toast.LENGTH_SHORT).show()
            return
        } else {
            if (productAmount.length.compareTo(0) == 1) {
                if(productAmount.toIntOrNull() != null && productAmount.toInt() < 0) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show()
                    return
                }
                else if (productAmount.toIntOrNull() == null) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show()
                    return
                }
            }
            else {
                Toast.makeText(requireContext(), getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show()
                return
            }
        }

        try {
            // Invio pacchetto dati di aggiunta
            val format : String = LoginActivity.user + "|" + LoginActivity.pwdHash + "|new|$productCode|$productAmount|$productName"

            // Utilizzo un altro thread per utilizzare le funzionalità di rete in quanto non possono essere eseguite
            // nel main thread
            GlobalScope.launch (Dispatchers.Default) {
                interactWithSocket(format)
            }

            // Sleep necessario per far terminare le operazioni di rete
            Thread.sleep(400)

            when (replyCommunication) {
                "002" -> {
                    Toast.makeText(requireContext(), getString(R.string.executed_operation), Toast.LENGTH_SHORT).show()
                }
                "006" -> {
                    Toast.makeText(requireContext(), getString(R.string.already_existing_prd_name), Toast.LENGTH_SHORT).show()
                }
                "007" -> {
                    Toast.makeText(requireContext(), getString(R.string.already_existing_prd_code), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireContext(), getString(R.string.rejected_operation), Toast.LENGTH_SHORT).show()
                }
            }
        }catch (ex: Exception) {
            Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show()
        }

        replyCommunication = ""
    }
}