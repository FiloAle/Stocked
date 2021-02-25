package com.stocked.ui.scanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import com.stocked.R
import org.json.JSONException
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class ScannerFragment : Fragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    lateinit var scannerView : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scannerView = inflater.inflate(R.layout.fragment_scanner, container, false)
        cameraTask()
        return scannerView
    }

    fun cameraTask(){

        if(hasCameraAccess()){

            var qrScanner = IntentIntegrator(activity)
            qrScanner.setPrompt(getString(R.string.qr_msg))
            qrScanner.setCameraId(0)
            qrScanner.setOrientationLocked(true)
            qrScanner.setBeepEnabled(false)
            qrScanner.captureActivity = CaptureActivity::class.java
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents == null){

                Toast.makeText(activity, getString(R.string.canceled_scan), Toast.LENGTH_SHORT).show()

            }else{
                try{
                    Toast.makeText(activity, "Successful", Toast.LENGTH_SHORT).show()
                    scannerView.findViewById<TextView>(R.id.txtCode).text = result.contents
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