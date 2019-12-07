package com.example.mcateam6.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mcateam6.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddPhotoBottomDialogFragment(
    private val onSelectCamera: () -> Unit = {},
    private val onSelectGallery: () -> Unit = {},
    private val onSelectRemove: () -> Unit = {}
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_add_photo_bottom_dialog, container, false)

        v.findViewById<TextView>(R.id.tv_btn_add_photo_camera).setOnClickListener {
            Handler().postDelayed({
                onSelectCamera()
                Handler().postDelayed({
                    dismiss()
                }, 200)
            }, 150)
        }
        v.findViewById<TextView>(R.id.tv_btn_add_photo_gallery).setOnClickListener {
            Handler().postDelayed({
                onSelectGallery()
                dismiss()
            }, 150)
        }
        v.findViewById<TextView>(R.id.tv_btn_remove_photo).setOnClickListener {
            Handler().postDelayed({
                dismiss()
                onSelectRemove()
            }, 150)
        }

        return v
    }
}