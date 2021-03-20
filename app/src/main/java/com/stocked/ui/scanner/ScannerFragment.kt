package com.stocked.ui.scanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.stocked.MainActivity
import com.stocked.R
import org.json.JSONException
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.DataOutputStream

const val AC_REMOVE = "remove"
const val AC_ADD = "add"
const val AC_CHECK = "check"

class ScannerFragment : Fragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    lateinit var scannerView : View
    var selectedProduct : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        scannerView = inflater.inflate(R.layout.fragment_scanner, container, false)
        cameraTask()
        scannerView.findViewById<Button>(R.id.btnSend).setOnClickListener { view ->
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

        // **DA FARE** Invio dati con modifica, numero indica in più/in meno
        // "*varuser|*varpass|" + actionCode + "|*varcode|*varnumber"

        Toast.makeText(requireContext(), actionCode + " quantità " + amount, Toast.LENGTH_SHORT).show()
        // **DA FARE** attendere risposta del server

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

    private fun verifyCode(varCode : String){
        // **DA FARE** Invio del codice da verificare
        /*val dOut = DataOutputStream(MainActivity.socket.getOutputStream())
        dOut.writeByte(1)
        dOut.writeUTF("*varuser|*varpass|select|" + varCode)
        dOut.flush()*/

        // **DA FARE** Ricezione codici risposta
        // 005 prodotto non presente nel database
        // 002 comunica codice, nome e quantità
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents == null){
                Toast.makeText(activity, getString(R.string.canceled_scan), Toast.LENGTH_SHORT).show()
            }else{
                try{
                    Toast.makeText(activity, "Successful", Toast.LENGTH_SHORT).show()

                    // Da fare: check della presenza del codice nel db, se non esiste annullo operazione
                    verifyCode(result.contents)

                    scannerView.findViewById<TextView>(R.id.txtCode).text = result.contents
                    scannerView.findViewById<RadioButton>(R.id.rdbAdd).isChecked = true

                }catch (exception: JSONException){
                    Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
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
