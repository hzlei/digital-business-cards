package cs446.dbc.views

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cs446.dbc.components.BusinessCardMultiSelect
import cs446.dbc.components.toFormattedString
import cs446.dbc.models.EventType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.CreateEditViewModel
import cs446.dbc.viewmodels.EventViewModel
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(createEditViewModel: CreateEditViewModel, eventViewModel: EventViewModel, appViewModel: AppViewModel, cardViewModel: BusinessCardViewModel, navController: NavHostController, eventId: String = "") {
    val events by eventViewModel.events.collectAsStateWithLifecycle()
    val myCards by cardViewModel.myBusinessCards.collectAsStateWithLifecycle()
    val createEditEvent by createEditViewModel.createEditEvent.collectAsStateWithLifecycle()

    // TODO: ensure eventId gets set back to null afterwards
    //  even if the user leaves the page and doesn't save their changes
    // TODO: There seems to be an issue where after we get back to the events screen
    //  then come back here, our data is messed up, and it seems we go back to the eventMenu or EventScreen for some reason
    //  and rerun the code to clear our current view....
    appViewModel.updateScreenTitle("${if (eventId != "") "Edit" else "Create"} Event${if (eventId != "") ": " + events.find { it.id == eventId }?.name else ""}")

    var name by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var location by remember {
        mutableStateOf(TextFieldValue(""))
    }
    val defaultMaxUsers = 1000
    var maxUsers by remember {
        mutableIntStateOf(defaultMaxUsers)
    }
    var maxUsersSet by remember {
        mutableStateOf(false)
    }
    var startDate by rememberSaveable {
        mutableLongStateOf(if (eventId != "") events.find {
            it.id == eventId }!!.startDate.toLong() else Date().time)
    }
    val threeDays: Long = 1000 * 60 * 60 * 24 * 3
    var endDate by rememberSaveable {
        mutableLongStateOf(if (eventId != "") events.find {
            it.id == eventId }!!.endDate.toLong() else Date().time + threeDays)
    }

    LaunchedEffect(key1 = "populate_event_info") {
        if (eventId != "") {
            // TODO: send cards to event (pick which ones?)
            val currEvent = events.find { it.id == eventId }!!
            createEditEvent.id = eventId
            createEditEvent.name = currEvent.name
            createEditEvent.location = currEvent.location
            createEditEvent.startDate = currEvent.startDate
            createEditEvent.endDate = currEvent.endDate
            createEditEvent.numUsers = currEvent.numUsers
            createEditEvent.maxUsers = currEvent.maxUsers
            createEditEvent.maxUsersSet = currEvent.maxUsersSet
            createEditEvent.eventType = currEvent.eventType

            name = TextFieldValue(currEvent.name)
            location = TextFieldValue(currEvent.location)
            startDate = currEvent.startDate.toLong()
            endDate = currEvent.endDate.toLong()
            maxUsers = currEvent.maxUsers
            maxUsersSet = currEvent.maxUsersSet
        }
        else {
            createEditEvent.id = ""
            createEditEvent.name = name.text
            createEditEvent.location = location.text
            createEditEvent.startDate = startDate.toString()
            createEditEvent.endDate = endDate.toString()
            createEditEvent.numUsers = 0
            createEditEvent.maxUsers = 1000
            createEditEvent.maxUsersSet = false
            createEditEvent.eventType = EventType.HOSTED
        }
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 16.dp)
    ){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = {
                    name = it
                    createEditEvent.name = name.text
                }, label = { Text(text = "Event Name") },
                placeholder = {
                Text(text = "e.g. Deep Learning Summit")
            }, modifier = Modifier.fillMaxSize())

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(value = location, onValueChange = {
                    location = it
                    createEditEvent.location = location.text
                }, label = { Text(text = "Event Location") },
                placeholder = {
                    Text(text = "e.g. Toronto, Ontario")
            }, modifier = Modifier.fillMaxSize())

            Spacer(modifier = Modifier.padding(4.dp))

            // TODO: Don't allow end date to be longer than 3 days from start date
            DateTextField("Start Date", Date(startDate)) {
                startDate = it
                createEditEvent.startDate = startDate.toString()
            }

            Spacer(modifier = Modifier.padding(4.dp))

            DateTextField("End Date", Date(endDate)) {
                endDate = it
                createEditEvent.endDate = endDate.toString()
            }

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(value = maxUsers.toString(), onValueChange = {
                // Ensure value is non-negative
                maxUsers = if ((it.toIntOrNull() ?: 0) < 1) 1
                else it.toIntOrNull() ?: defaultMaxUsers
                maxUsersSet = maxUsers != defaultMaxUsers
                createEditEvent.maxUsers = maxUsers
                createEditEvent.maxUsersSet = maxUsersSet
               },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                label = { Text(text = "Max Number of Participants")})

            // TODO: allow user to pick which cards to be sent to the event (can only be done
            //  the first time an event is created, after which the box goes away
            //  can maybe make a component with checkboxes, and have those correspond to the id's to be sent
            if (eventId == "" && myCards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.weight(0.3f)) {
                    BusinessCardMultiSelect(
                        title = "Select Any Business Cards to Upload to Event",
                        cardViewModel = cardViewModel,
                        createEditViewModel = createEditViewModel
                    )
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterial3Api
@Composable
fun DateTextField(textFieldLabel: String, currDate: Date, date: (Long) -> Unit) {
    var isDatePickerDialogOpen by remember {
        mutableStateOf(false)
    }
//    val currentDate = currDate.toFormattedString()
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    Log.d("selected DATE",currDate.toFormattedString())
    var selectedDate by rememberSaveable { mutableStateOf(currDate.toFormattedString()) }

    Row {
        OutlinedTextField(
            readOnly = true,
            enabled = false,
            value = selectedDate,
            onValueChange = {},
            trailingIcon = { Icons.Default.DateRange },
            //interactionSource = source,
            modifier = Modifier
                .clickable { isDatePickerDialogOpen = true }
                .fillMaxWidth(),
            label = { Text(text = textFieldLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
//        IconButton(onClick = { isDatePickerDialogOpen = true }) {
//            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar")
//        }
    }

    // TODO: we need 2 variables to be altered, one for start date and another for end date
    //if (isPressed) {
//        datePickerDialog()
      if (isDatePickerDialogOpen) {
          CustomDatePickerDialog(initialValue = currDate.time,
              onConfirm = { newDateLong: Long? ->
                  val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                  cal.time = Date(newDateLong!!)
                  val month = cal.get(Calendar.MONTH)
                  selectedDate = "${
                      month.toMonthName()
                  } ${
                      cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                  }, ${cal.get(Calendar.YEAR)}"
                  date(newDateLong)
                  isDatePickerDialogOpen = false
              }
          ) {
              isDatePickerDialogOpen = false
          }
      }
}

@ExperimentalMaterial3Api
@Composable
private fun CustomDatePickerDialog(
    initialValue: Long,
    onConfirm: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialValue)
    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = { onConfirm(datePickerState.selectedDateMillis)}) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    ){
        DatePicker(state = datePickerState)
    }
}

private fun Int.toMonthName(): String {
    return DateFormatSymbols().months[this]
}

private fun Date.toFormattedString(): String {
    val simpleDateFormat = SimpleDateFormat("LLLL dd, yyyy", Locale.getDefault())
    simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return simpleDateFormat.format(this)
}