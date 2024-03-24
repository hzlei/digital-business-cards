package cs446.dbc

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Event
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compose.AppTheme
import cs446.dbc.components.ReceiveDialog
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.EventModel
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.models.TemplateType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.EventViewModel
import cs446.dbc.views.CreateEventScreen
import cs446.dbc.views.EventScreen
import cs446.dbc.views.SharedCardsScreen
import cs446.dbc.views.UserCardsScreen
import cs446.dbc.views.EventMenuScreen
import java.util.UUID
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appActivity = this)
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalAnimationApi::class)
//    @OptIn(ExperimentalLifeCycleComposeApi::class)
    @Composable
    private fun App(appActivity: AppCompatActivity) {
        val appViewModel: AppViewModel = viewModel() {
            AppViewModel(savedStateHandle = createSavedStateHandle(), CardType.SHARED)
        }
        val cardViewModel: BusinessCardViewModel = viewModel() {
            BusinessCardViewModel(savedStateHandle = createSavedStateHandle(), CardType.SHARED)
        }
        val eventViewModel: EventViewModel = viewModel() {
            EventViewModel(savedStateHandle = createSavedStateHandle())
        }

        val appContext = LocalContext.current
        val navController = rememberNavController()
        val loadedSharedCards by appViewModel.loadedSharedCards.collectAsStateWithLifecycle()
        val loadedMyCards by appViewModel.loadedMyCards.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = "load_cards") {
            if (!loadedSharedCards) {
                val cardList =
                    appViewModel.loadCardsFromDirectory(
                        appContext,
                        "businessCards",
                        CardType.SHARED
                    )
                cardViewModel.performAction(BusinessCardAction.InsertCards(cardList))
            }
        }

        val homeUiState by appViewModel.uiState.collectAsStateWithLifecycle()
        val snackBarHostState = remember { SnackbarHostState() }

        // TODO: remove after demo, we'll use this to start in the SharedCards Screen
        val sharedCardsList = listOf(
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "A",
                back = "B",
                favorite = false,
                fields = mutableListOf(),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "C",
                back = "D",
                favorite = true,
                fields = mutableListOf(),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "E",
                back = "F",
                favorite = false,
                fields = mutableListOf(
                    Field(
                        "Full Name",
                        "Hanz Zimmer",
                        FieldType.TEXT
                    )
                ),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "G",
                back = "H",
                favorite = false,
                fields = mutableListOf(
                    Field(
                        "Phone Number",
                        "416-111-2222",
                        FieldType.PHONE_NUMBER
                    )
                ),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "I",
                back = "J",
                favorite = false,
                template = TemplateType.TEMPLATE_1,
                fields = mutableListOf(
                    Field(
                        "Full Name",
                        "John Doe",
                        FieldType.TEXT,
                    )
                ),
                cardType = CardType.SHARED
            ),
        )

        sharedCardsList.forEach { card ->
            cardViewModel.performAction(
                BusinessCardAction.PopulateCard(
                    front = card.front,
                    back = card.back,
                    favorite = card.favorite,
                    fields = card.fields,
                    cardType = card.cardType
                )
            )
        }

        appViewModel.updateScreenTitle("Saved Cards")

        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackBarHostState)
                    },
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
                        BottomAppBar(
                            navController,
                            appViewModel,
                            cardViewModel,
                            snackBarHostState,
                            appContext
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController,
                            startDestination = Screen.Home.route,
                            enterTransition = {
                                slideInHorizontally {
                                    if (
                                        order(initialState.destination.route!!)
                                        < order(targetState.destination.route!!)
                                    ) it
                                    else -it
                                }
                            },
                            exitTransition = { slideOutHorizontally {
                                if (
                                    order(initialState.destination.route!!)
                                    < order(targetState.destination.route!!)
                                ) -it
                                else it
                            } },
                        ) {
                            composable(Screen.Home.route) {
                                cardViewModel.performAction(
                                    BusinessCardAction.UpdateCardContext(
                                        CardType.SHARED
                                    )
                                )
                                SharedCardsScreen(
                                    appViewModel,
                                    cardViewModel,
                                    sharedCardsList,
                                    appContext
                                )
                            }
                            composable(Screen.UserCards.route) {
                                cardViewModel.performAction(
                                    BusinessCardAction.UpdateCardContext(
                                        CardType.PERSONAL
                                    )
                                )
                                // TODO: Remove the example list after
                                UserCardsScreen(
                                    appViewModel, cardViewModel, listOf(
                                        BusinessCardModel(
                                            id = UUID.randomUUID().toString(),
                                            front = "A",
                                            back = "B",
                                            favorite = false,
                                            fields = mutableListOf(
                                                Field(
                                                    "Full Name",
                                                    "John Doe",
                                                    FieldType.TEXT,
                                                ),
                                                Field(
                                                    "Email",
                                                    "john@example.com",
                                                    FieldType.TEXT,
                                                ),
                                                Field(
                                                    "Organization",
                                                    "Test Org",
                                                    FieldType.TEXT
                                                )
                                            ),
                                            cardType = CardType.PERSONAL,
                                            template = TemplateType.TEMPLATE_1
                                        ),
                                        BusinessCardModel(
                                            id = UUID.randomUUID().toString(),
                                            front = "C",
                                            back = "D",
                                            favorite = true,
                                            fields = mutableListOf(),
                                            cardType = CardType.PERSONAL
                                        ),
                                    ), appContext
                                )
                            }
                            composable(Screen.Settings.route) {
                                appViewModel.updateScreenTitle("Settings") // TODO: Replace with SettingsScreen
                                // TODO: Remove after, for now just creating a new composable so we
                                //  don't get the home page showing up
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {

                                }
                            }
                            composable(Screen.Events.route
                            ) {
                                EventScreen(eventViewModel, appViewModel, appContext, navController)
                            }
                            composable(Screen.EventMenu.route,
                                arguments = listOf(navArgument("eventId") {}))
                            {
                                val eventId = it.arguments?.getString("eventId")
                                EventMenuScreen(eventViewModel, appViewModel, appContext, navController, eventId)
                            }
                            composable(Screen.EventCreationMenu.route) {
                                CreateEventScreen(eventViewModel, appViewModel, appContext, navController)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomAppBar(
        navController: NavHostController,
        appViewModel: AppViewModel,
        cardViewModel: BusinessCardViewModel,
        snackBarHostState: SnackbarHostState,
        context: Context
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        var showReceiveDialog by rememberSaveable {
            mutableStateOf(false)
        }

        @Composable
        fun NavButton(screen: Screen, icon: ImageVector, description: String) {
            val isCurrentRoute = navBackStackEntry?.destination?.route == screen.route
            IconToggleButton(
                checked = isCurrentRoute,
                onCheckedChange = { if (!isCurrentRoute) navController.navigate(screen.route) },
            ) {
                Icon(icon, description)
            }
        }

        if (showReceiveDialog) {
            ReceiveDialog(snackBarHostState, sharedCardViewModel = cardViewModel) {
                showReceiveDialog = false
            }
        }
        androidx.compose.material3.BottomAppBar(
            modifier = Modifier.fillMaxWidth(),
            actions = {
                NavButton(Screen.Home, Icons.Outlined.People, "Saved Cards")
                NavButton(Screen.UserCards, Icons.Outlined.Person, "My Cards")
                NavButton(Screen.Events, Icons.Outlined.Event, "Events")
            },
            // TODO: Make sure to add parameters that we don't directly make (e.g. CardType)
            floatingActionButton = {
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.UserCards.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = { /*TODO: Go to business card creation screen*/
                            val newCard = BusinessCardModel(
                                id = UUID.randomUUID().toString(),
                                front = "New Front",
                                back = "New Back",
                                favorite = false,
                                fields = mutableListOf(),
                                cardType = CardType.PERSONAL,
                            )

                            appViewModel.addCard(
                                newCard,
                                context,
                                "businessCards",
                                CardType.PERSONAL
                            )
                            cardViewModel.performAction(BusinessCardAction.InsertCard(newCard))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Cards"
                        )
                    }
                }
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.Home.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = {
                            showReceiveDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Receive Card"
                        )
                    }
                }
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.Events.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = {
                            // go to create event screen
                            navController.navigate(route = "create-event")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Event"
                        )
                    }
                }
            }
        )
    }

    private fun order(route: String): Int {
        return when (route) {
            Screen.Settings.route -> 0
            Screen.Home.route -> 1
            Screen.UserCards.route -> 2
            Screen.Events.route -> 3
            else -> Int.MAX_VALUE
        }
    }

    sealed class Screen(val route: String) {
        object UserCards : Screen("my-cards")
        object Home : Screen("saved-cards")
        object Settings : Screen("settings")
        object Events : Screen("events")
        object EventMenu : Screen("event-menu/{eventId}")
        object EventCreationMenu : Screen("create-event")

    }
}