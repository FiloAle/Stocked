package com.stocked.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.stocked.R

class StatusFragment : Fragment() {

    private lateinit var statusViewModel: StatusViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        statusViewModel =
                ViewModelProvider(this).get(StatusViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_status, container, false)
        val textView: TextView = root.findViewById(R.id.text_status)
        statusViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}