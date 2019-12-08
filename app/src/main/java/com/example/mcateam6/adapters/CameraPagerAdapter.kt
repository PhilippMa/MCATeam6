package com.example.mcateam6.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mcateam6.fragments.BarcodeFragment
import com.example.mcateam6.fragments.ImageRecognizerFragment


class CameraPagerAdapter(fa: FragmentActivity, val arrayFragment: List<Fragment>): FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return arrayFragment.size
    }

    override fun createFragment(position: Int): Fragment {
        return arrayFragment[position]
    }


}