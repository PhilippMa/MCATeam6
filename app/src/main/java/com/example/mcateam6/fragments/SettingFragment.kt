package com.example.mcateam6.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.mcateam6.R

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences2, rootKey)
    }

}