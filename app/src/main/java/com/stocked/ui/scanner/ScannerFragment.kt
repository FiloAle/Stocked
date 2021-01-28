package com.stocked.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.stocked.R

class ScannerFragment : Fragment() {

    private lateinit var scannerViewModel: ScannerViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        scannerViewModel =
                ViewModelProvider(this).get(ScannerViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_scanner, container, false)
        val textView: TextView = root.findViewById(R.id.text_scanner)
        scannerViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}