package com.stocked.ui.scanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.stocked.LoginActivity
import com.stocked.MainActivity
import com.stocked.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.DataInputStream
import java.io.DataOutputStream

const val AC_REMOVE = "remove"
const val AC_ADD = "add"
const val AC_SELECT = "select"

class ScannerFragment : Fragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    lateinit var scannerView : View
    var selectedProduct : String = ""
    private var replyCommunication : String = ""
    private var found : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        scannerView = inflater.inflate(R.layout.fragment_scanner, container, false)
        cameraTask()
        scannerView.findViewById<Button>(R.id.btnSend).setOnClickListener {
            checkAndSend()
        }
        return scannerView
    }

    private fun checkAndSend() {
        val actionCode : String?
        val amount : Int? = scannerView.findViewById<EditText>(R.id.dttAmount).text.toString().toIntOrNull()
        if(!found){
            Toast.makeText(requireContext(), getString(R.string.not_found), Toast.LENGTH_LONG).show()
            return
        }

        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show()
            return
        }

        if (scannerView.findViewById<RadioButton>(R.id.rdbRemove).isChecked) {
            actionCode = AC_REMOVE

            // Controlla che non si tenti di rimuovere una quantità maggiore rispetto a quella disponibile
            if (scannerView.findViewById<TextView>(R.id.txtAvailableProducts).text.toString().toInt() < amount) {
                Toast.makeText(requireContext(), getString(R.string.exceeding_quantity_removal), Toast.LENGTH_LONG).show()
                return
            }
        } else
            actionCode = AC_ADD

        // Invio dati con modifica, numero indica in più/in meno
        val format : String = LoginActivity.user + "|" + LoginActivity.pwdHash + "|" + actionCode + "|" + selectedProduct + "|" + amount

        try {
            if (selectedProduct != "") {
                GlobalScope.launch(Dispatchers.Default){
                    interactWithSocket(format)
                }

                // Tempo di attesa in modo da permettere alle funzionalità di rete di terminare lo scambio
                // Evita che la variabile replyCommunication risulti non inizializzata
                Thread.sleep(400)

                if (replyCommunication != "") {
                    val messageFields = replyCommunication.split("|")

                    when (messageFields[0]) {
                        "002" -> {
                            Toast.makeText(requireContext(), getString(R.string.executed_operation), Toast.LENGTH_SHORT).show()
                            cameraTask()
                        }
                        else -> {
                            Toast.makeText(requireContext(), getString(R.string.unknown_err), Toast.LENGTH_SHORT).show()
                            cameraTask()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.no_server_response), Toast.LENGTH_SHORT).show()
                    cameraTask()
                }
            }
            selectedProduct = ""
            replyCommunication = ""
        } catch (ex:Exception) {
            Toast.makeText(activity, ex.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun cameraTask(){
        if (hasCameraAccess()) {
            IntentIntegrator.forSupportFragment(this).initiateScan();
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.cam_perm),
                123,
                android.Manifest.permission.CAMERA
            )
        }
    }

    private fun hasCameraAccess() : Boolean{
        return EasyPermissions.hasPermissions(requireActivity(), android.Manifest.permission.CAMERA)
    }

    private fun interactWithSocket(format : String) : Unit{
        val writer = DataOutputStream(MainActivity.socket.getOutputStream())
        writer.write(format.toByteArray())
        writer.flush()

        // Ricezione risposta dal server
        var reply : String
        val reader = DataInputStream(MainActivity.socket.getInputStream())
        val msg = ByteArray(1024)
        reader.read(msg)
        reply = msg.toString(Charsets.US_ASCII)
        val rgx = Regex("[^A-Za-z0-9 |]")
        reply = rgx.replace(reply, "")
        replyCommunication = reply

        return
    }

    private fun verifyCode(varCode : String){
        // Invio pacchetto dati di aggiunta
        val format : String = LoginActivity.user + "|" + LoginActivity.pwdHash + "|" + AC_SELECT + "|" + varCode

        // Utilizzo un dispatcher per utilizzare le funzionalità di rete
        GlobalScope.launch(Dispatchers.Default){
            interactWithSocket(format)
        }

        Thread.sleep(400)

        if(replyCommunication != ""){
            val messageFields = replyCommunication.split("|")
            selectedProduct = messageFields[1] // Importante, non eliminare
            when (messageFields[0]) {
                "002" -> {
                    // Valorizzazione dei textview
                    // Gli sleep sono necessari per dare il tempo al sistema di aggiornare correttamente l'interfaccia
                    scannerView.findViewById<TextView>(R.id.txtCode).text = messageFields[1]
                    scannerView.findViewById<TextView>(R.id.txtProductName).text = messageFields[2]
                    scannerView.findViewById<TextView>(R.id.txtAvailableProducts).text = messageFields[3]
                    scannerView.findViewById<EditText>(R.id.dttAmount).text.clear()
                    found = true
                    Thread.sleep(100)
                }
                "005" -> {
                    // Prodotto non presente
                    scannerView.findViewById<TextView>(R.id.txtCode).text = selectedProduct
                    scannerView.findViewById<TextView>(R.id.txtProductName).text = getString(R.string.not_found)
                    scannerView.findViewById<TextView>(R.id.txtAvailableProducts).text = ""
                    scannerView.findViewById<EditText>(R.id.dttAmount).text.clear()
                    found = false
                    Thread.sleep(100)
                }
            }
        }
        else {
            Toast.makeText(requireContext(), getString(R.string.no_server_response), Toast.LENGTH_SHORT).show()
            cameraTask()
        }

        replyCommunication = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(activity, getString(R.string.canceled_scan), Toast.LENGTH_SHORT).show()
                scannerView.findViewById<TextView>(R.id.txtCode).text = getString(R.string.prd_code)
                scannerView.findViewById<TextView>(R.id.txtProductName).text = getString(R.string.prd_name)
                scannerView.findViewById<TextView>(R.id.txtAvailableProducts).text = getString(R.string.prd_quantity)
                scannerView.findViewById<EditText>(R.id.dttAmount).text.clear()
                Thread.sleep(100)
            } else {
                try {
                    // Verifica del codice scannerizzato e ricezione della risposta dal server
                    verifyCode(result.contents)
                    scannerView.findViewById<RadioButton>(R.id.rdbAdd).isChecked = true
                } catch (exception: JSONException) {
                    Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) {
                    Toast.makeText(activity, ex.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(activity, getString(R.string.cam_perm_granted), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        Thread.sleep(100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        cameraTask()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
    }

    override fun onRationaleDenied(requestCode: Int) {
    }
}
