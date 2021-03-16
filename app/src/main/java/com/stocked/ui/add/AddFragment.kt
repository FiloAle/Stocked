package com.stocked.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stocked.R

class AddFragment : Fragment() {

    private lateinit var addView : View
    private lateinit var addViewModel: AddViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        addViewModel =
                ViewModelProvider(this).get(AddViewModel::class.java)
        addView = inflater.inflate(R.layout.fragment_add, container, false)

        // Da rivedere
        // Step1: inserimento manuale del nuovo codice prodotto (quello da cui generare il barcode)
        // Come scoprire qual è l'ultimo codice..


        // Controllo dell'esistenza del codice nel db

        // Inserimento nome quantità

        // Invio nuovo prodotto


        return addView
    }
}