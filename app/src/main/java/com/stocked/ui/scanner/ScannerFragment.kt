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
import kotlinx.coroutines.*
import org.json.JSONException
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.*

const val AC_REMOVE = "remove"
const val AC_ADD = "add"
const val AC_CHECK = "check"
const val AC_SELECT = "select"

class ScannerFragment : Fragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    lateinit var scannerView : View
    var selectedProduct : String = ""
    private lateinit var replyCommunication : String

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

    private fun checkAndSend(){
        lateinit var rdbChecked : RadioButton
        var actionCode : String? = null
        var amount : Int? = 0
        amount = scannerView.findViewById<EditText>(R.id.dttAmount).text.toString().toIntOrNull()
        if(amount == null || amount <= 0){
            Toast.makeText(requireContext(), "Invio annullato: inserire una quantità valida.", Toast.LENGTH_SHORT).show()
            return
        }
        if(scannerView.findViewById<RadioButton>(R.id.rdbRemove).isChecked){
            actionCode = AC_REMOVE
        }else
            actionCode = AC_ADD

        // Invio dati con modifica, numero indica in più/in meno
        var format : String = LoginActivity.user + "|" + LoginActivity.pwdHash + "|" + actionCode + "|" + selectedProduct + "|" + amount

        try{
            GlobalScope.launch(Dispatchers.Default){
                interactWithSocket(format)
            }

            Thread.sleep(100)

            val messageFields = replyCommunication.split("|")
            val response : String = messageFields[0] // Codice intero ricevuto

            when (response){
                "002" ->{
                    Toast.makeText(requireContext(), "Operazione eseguita", Toast.LENGTH_SHORT).show()
                    cameraTask()
                }

                else ->{
                    Toast.makeText(requireContext(), "Errore sconosciuto: operazione annullata", Toast.LENGTH_SHORT).show()
                    cameraTask()
                }
            }
        }catch(ex:Exception){
            Toast.makeText(activity, ex.localizedMessage, Toast.LENGTH_SHORT).show()
        }

    }

    private fun cameraTask(){
        if(hasCameraAccess()){
            IntentIntegrator.forSupportFragment(this).initiateScan();
        }else{
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

    private fun verifyCode(varCode : String){

        // Invio pacchetto dati di aggiunta
        var format : String = LoginActivity.user + "|" + LoginActivity.pwdHash + "|" + AC_SELECT + "|" + varCode


        GlobalScope.launch(Dispatchers.Default){
            interactWithSocket(format)
        }

        Thread.sleep(100)

        val messageFields = replyCommunication.split("|")
        val response : String = messageFields[0] // Codice intero ricevuto


        when (response) {
            "002" -> {
                // Valorizzazione dei textview
                selectedProduct = messageFields[1]
                scannerView.findViewById<TextView>(R.id.txtCode).text = messageFields[1]
                scannerView.findViewById<TextView>(R.id.txtProductName).text = messageFields[2]
                scannerView.findViewById<TextView>(R.id.txtAvailableProducts).text = messageFields[3]
            }
            "005" -> {
                // Prodotto non presente
                // Transizione all'add fragment ????????
                Toast.makeText(requireContext(), "non trovato", Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents == null){
                Toast.makeText(activity, getString(R.string.canceled_scan), Toast.LENGTH_SHORT).show()
            }else{
                try{
                    Toast.makeText(activity, "Successful", Toast.LENGTH_SHORT).show()

                    verifyCode(result.contents)

                    scannerView.findViewById<TextView>(R.id.txtCode).text = result.contents
                    scannerView.findViewById<RadioButton>(R.id.rdbAdd).isChecked = true

                }catch (exception: JSONException){
                    Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                }catch(ex: Exception){
                    Toast.makeText(activity, ex.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

        if(requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
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
