package com.example.mcateam6.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mcateam6.R
import com.example.mcateam6.activities.AddProductActivity
import com.example.mcateam6.activities.MainActivity
import com.example.mcateam6.adapters.CameraPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CameraFragment: Fragment() {
    private lateinit var mViewPager: ViewPager2
    private lateinit var mAdapter: CameraPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_camera, container, false)
        mViewPager = v.findViewById(R.id.view_pager)
        val arrayFragment = listOf<Fragment>(BarcodeFragment(), ImageRecognizerFragment())
        mAdapter = CameraPagerAdapter(activity as MainActivity, arrayFragment)

        mViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mViewPager.adapter = mAdapter

        v.findViewById<FloatingActionButton>(R.id.add_product_button).setOnClickListener {
            val intent = Intent().setClass(context!!, AddProductActivity::class.java)
            startActivity(intent)
        }
        return v
    }
}