package com.stocked.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.stocked.R

class StatusFragment : Fragment() {

    private lateinit var statusViewModel: StatusViewModel
    private lateinit var button : Button

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

        button = root.findViewById(R.id.btnButton)
        button.setOnClickListener(View.OnClickListener {
            Toast.makeText(activity, "maledizione", Toast.LENGTH_SHORT).show()
        })
        return root
    }
}