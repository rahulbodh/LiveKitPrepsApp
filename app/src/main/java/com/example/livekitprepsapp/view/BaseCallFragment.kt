package com.example.livekitprepsapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.livekitprepsapp.R
import com.example.livekitprepsapp.model.CallArgs

open class BaseCallFragment : Fragment() {

    private lateinit var args : CallArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_base_call, container, false)

        args = requireArguments().getParcelable("args")
            ?: throw IllegalStateException("CallArgs are missing")

        return view
    }

}