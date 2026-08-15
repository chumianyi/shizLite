package moe.shizuku.manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import moe.shizuku.manager.module.ModuleFragment
import moe.shizuku.manager.nav.HomeFragment
import moe.shizuku.manager.nav.PermissionFragment
import moe.shizuku.manager.nav.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            switchFragment(HomeFragment())
            toolbar.title = getString(R.string.nav_home)
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_modules -> ModuleFragment()
                R.id.nav_permission -> PermissionFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> HomeFragment()
            }
            toolbar.title = item.title
            switchFragment(fragment)
            true
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
