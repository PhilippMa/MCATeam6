package com.example.mcateam6.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mcateam6.R
import com.example.mcateam6.fragments.BarcodeFragment
import com.example.mcateam6.fragments.ProductInfoFragment
import com.example.mcateam6.fragments.SearchFragment
import com.example.mcateam6.fragments.SettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var fragmentList = mutableListOf<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_nav.menu?.get(1).setChecked(true)
        val fm = supportFragmentManager

        val productInfoFragment = ProductInfoFragment()
        val barcodeFragment = BarcodeFragment()
        val searchFragment = SearchFragment()
        val settingFragment = SettingFragment()

        fragmentList.add(productInfoFragment)
        fragmentList.add(barcodeFragment)
        fragmentList.add(searchFragment)
        fragmentList.add(settingFragment)

        var fragmentTransaction = fm.beginTransaction()
        fragmentTransaction.add(R.id.layout_root, barcodeFragment)
        fragmentTransaction.commit()

        bottom_nav.setOnNavigationItemSelectedListener(object: BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                fragmentTransaction = fm.beginTransaction()
                when (item.itemId) {
                    R.id.menu_1 -> {
                        fragmentTransaction.replace(R.id.layout_root, fragmentList[0])
                    }
                    R.id.menu_2 -> {
                        fragmentTransaction.replace(R.id.layout_root, fragmentList[1])
                    }
                    R.id.menu_3 -> {
                        fragmentTransaction.replace(R.id.layout_root, fragmentList[2])
                    }
                    R.id.menu_4 -> {
                        fragmentTransaction.replace(R.id.layout_root, fragmentList[3])
                    }
                }
                fragmentTransaction.commit()
                return true
            }

        })
    }
}
