package cs446.dbc

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import cs446.dbc.bluetooth.Bluetooth

interface NavigationHost {
    fun navigateTo(fragment: Fragment, addToBackstack: Boolean)
}
class MainActivity : AppCompatActivity(), NavigationHost {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dbc_main_activity)
        
        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, MainMenuFragment())
            .commit()

        val bluetooth = Bluetooth(this)
    }

    override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }
}
