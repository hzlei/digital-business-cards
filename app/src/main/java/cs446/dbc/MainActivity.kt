package cs446.dbc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.KeyEventDispatcher.Component
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

interface NavigationHost {
    fun navigateTo(fragment: Fragment, addToBackstack: Boolean)
}
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    AppScaffold()
                }
            }
        )
    }

    @Preview(showSystemUi = true, showBackground = true)
    @Composable
    private fun AppScaffold() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("DBC")
                    })
            },
        ) {
            innerPadding -> Column(modifier = Modifier.padding(innerPadding)) {}
        }
    }
}
