package cs446.dbc

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.views.UserCardsScreen
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appActivity = this)
        }
    }


   // @Preview(showSystemUi = true, showBackground = true)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//    @OptIn(ExperimentalLifeCycleComposeApi::class)
    @Composable
    private fun App(appViewModel: AppViewModel = viewModel(), appActivity: AppCompatActivity) {
        val navController = rememberNavController()
        val homeUiState by appViewModel.uiState.collectAsStateWithLifecycle()

        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = homeUiState.screenTitle
                                )
                            },
//                            colors = TopAppBarDefaults.topAppBarColors(
//                                containerColor = Color.LightGray
//                            ),
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigate(Screen.Settings.route) },
                                ) {
                                    Icon(Icons.Outlined.Settings, "Settings")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomAppBar(navController)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        NavHost(navController, startDestination = Screen.UserCards.route) {
                            composable(Screen.UserCards.route) {
                                UserCardsScreen(appViewModel, listOf())
                            }
                            //composable(Screen.SharedCards.route) {}
                            composable(Screen.Home.route) {
                                appViewModel.updateScreenTitle("Shared Cards")
                            }
                            // TODO: change to actual settings, for now using to test ShareDialog
                            composable(Screen.Settings.route) { }
                            composable(Screen.SavedCards.route) { }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomAppBar(navController: NavHostController) {
        androidx.compose.material3.BottomAppBar(
            modifier = Modifier.fillMaxWidth(),
//            containerColor = Color(0xff454545)
        ) {
            IconButton(
                onClick = { navController.navigate(Screen.UserCards.route) },
                modifier = Modifier
                    .weight(1f)
            ) {
                Icon(Icons.Outlined.Person, "My Cards")
            }
//            IconButton(
//                onClick = { navController.navigate(Screen.SharedCards.route) },
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(Icons.Outlined.FolderShared, "Shared Cards")
//            }
            IconButton(
                onClick = { navController.navigate(Screen.Home.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Home, "Home")
            }
            IconButton(
                onClick = { navController.navigate(Screen.SavedCards.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Share, "Share Cards")
            }
        }
    }

    sealed class Screen(val route: String) {
        // Stores and displays our own business cards
        object UserCards : Screen("my-cards")
//        object SharedCards : Screen("shared-cards")
        // Stores and displays received cards
        object Home : Screen("home")
        object SavedCards : Screen("saved-cards")
        object Settings : Screen("settings")
    }
}