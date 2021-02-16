package com.stocked.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stocked.R

class StatusFragment : Fragment() {

    private lateinit var button : Button

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_status, container, false)
        button = root.findViewById(R.id.buttonConnect)
        button.setOnClickListener(View.OnClickListener {
            Toast.makeText(activity, "maledizione", Toast.LENGTH_SHORT).show()
        })
        return root
    }
}