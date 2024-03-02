package cs446.dbc

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import cs446.dbc.views.shareMenu

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }


    @Preview(showSystemUi = true, showBackground = true)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun App() {
        val navController = rememberNavController()
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {}
                        )
                    },
                    bottomBar = {
                        BottomAppBar(navController)
                    }
                ) { _ ->
                    NavHost(navController, startDestination = Screen.MyCards.route) {
                        composable(Screen.MyCards.route) {}
                        composable(Screen.SharedCards.route) {}
                        composable(Screen.Settings.route) {}
                        composable(Screen.ShareCards.route) { shareMenu() }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomAppBar(navController: NavHostController) {
        androidx.compose.material3.BottomAppBar(
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                onClick = { navController.navigate(Screen.MyCards.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Person, "My Cards")
            }
            IconButton(
                onClick = { navController.navigate(Screen.SharedCards.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.FolderShared, "Shared Cards")
            }
            IconButton(
                onClick = { navController.navigate(Screen.ShareCards.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Share, "Share Cards")
            }
            IconButton(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Settings, "Settings")
            }
        }
    }

    sealed class Screen(val route: String) {
        object MyCards : Screen("my-cards")
        object SharedCards : Screen("shared-cards")
        object ShareCards : Screen("share-menu")
        object Settings : Screen("settings")
    }
}

