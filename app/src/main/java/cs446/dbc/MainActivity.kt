package cs446.dbc

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.views.UserCardsScreen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appActivity = this)
        }
    }


    @OptIn(ExperimentalAnimationApi::class)
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
                                AnimatedContent(
                                    targetState = homeUiState.screenTitle,
                                    label = "TopBarTitle",
                                    transitionSpec = {
                                        fadeIn() togetherWith fadeOut()
                                    }
                                ) {
                                    Text(it)
                                }
                            },
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
                        NavHost(
                            navController,
                            startDestination = Screen.Home.route,
                        ) {
                            composable(Screen.Home.route) {
                                appViewModel.updateScreenTitle("Saved Cards") // TODO: Replace with SavedCardsScreen
                            }
                            composable(Screen.UserCards.route) {
                                UserCardsScreen(appViewModel, listOf(
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "A",
                                        back = "B",
                                        favorite = false,
                                        fields = mutableListOf()
                                    ),
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "C",
                                        back = "D",
                                        favorite = true,
                                        fields = mutableListOf()
                                    ),
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "E",
                                        back = "F",
                                        favorite = false,
                                        fields = mutableListOf(
                                            Field(
                                                "Full Name",
                                                "Hanz Zimmer",
                                                FieldType.TEXT
                                            )
                                        )
                                    ),
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "G",
                                        back = "H",
                                        favorite = false,
                                        fields = mutableListOf(
                                            Field(
                                                "Phone Number",
                                                "416-111-2222",
                                                FieldType.PHONE_NUMBER
                                            )
                                        )
                                    )
                                ))
                            }
                            composable(Screen.Settings.route) {
                                appViewModel.updateScreenTitle("Settings") // TODO: Replace with SettingsScreen
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomAppBar(navController: NavHostController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        @Composable
        fun NavButton(screen: Screen, icon: ImageVector, description: String) {
            val routeSelected = navBackStackEntry?.destination?.route == screen.route
//            val colors = if (navBackStackEntry?.destination?.route == screen.route)
//                IconButtonDefaults.i
//                else
            IconToggleButton(
                checked = routeSelected,
                onCheckedChange = { navController.navigate(screen.route) },
            ) {
                Icon(icon, description)
            }
        }
        androidx.compose.material3.BottomAppBar(
            modifier = Modifier.fillMaxWidth(),
            actions = {
                NavButton(Screen.Home, Icons.Outlined.People,  "Saved Cards")
                NavButton(Screen.UserCards, Icons.Outlined.Person, "My Cards")
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.UserCards.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = { /*TODO: Go to business card creation screen*/ }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Cards"
                        )
                    }
                }
            }
        )
    }

    sealed class Screen(val route: String) {
        object UserCards : Screen("my-cards")
        object Home : Screen("saved-cards")
        object Settings : Screen("settings")
    }
}