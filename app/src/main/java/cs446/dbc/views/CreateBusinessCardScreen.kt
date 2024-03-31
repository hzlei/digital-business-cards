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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cs446.dbc.components.BusinessCardMultiSelect
import cs446.dbc.components.toFormattedString
import cs446.dbc.models.CardType
import cs446.dbc.models.EventType
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.models.TemplateType
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
fun CreateBusinessCardScreen(createEditViewModel: CreateEditViewModel, cardViewModel: BusinessCardViewModel, appViewModel: AppViewModel, navController: NavHostController, cardId: String = "") {
    val myCards by cardViewModel.myBusinessCards.collectAsStateWithLifecycle()
    val createEditBusinessCard by createEditViewModel.createEditBusinessCard.collectAsStateWithLifecycle()

    appViewModel.updateScreenTitle("${if (cardId != "") "Edit" else "Create"} Card")

    var front by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var back by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var favorite by rememberSaveable {
        mutableStateOf(false)
    }

    var fields = SnapshotStateList<Field>()

    // Mandatory fields
    // TODO: Force add full name, company/institution name, role/title
    //  They can then add information such as mobile phone number, company phone number, email,
    //  address, website, linkedin, github
    var fullName by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var company by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var role by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var mobilePhone by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var companyPhone by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var email by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var address by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var website by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var linkedin by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var github by remember {
        mutableStateOf(TextFieldValue(""))
    }

    var template: TemplateType = TemplateType.TEMPLATE_1
    var cardType: CardType = CardType.PERSONAL
    var eventId: String = ""
    var eventUserId: String = ""

    LaunchedEffect(key1 = "populate_card_info") {
        if (cardId != "") {
            // TODO: send cards to event (pick which ones?)
            val currCard = myCards.find { it.id == cardId }!!
            createEditBusinessCard.id = cardId
            createEditBusinessCard.front = currCard.front
            createEditBusinessCard.back = currCard.back
            createEditBusinessCard.favorite = currCard.favorite
            createEditBusinessCard.fields = currCard.fields
            createEditBusinessCard.template = currCard.template
            createEditBusinessCard.cardType = currCard.cardType
            createEditBusinessCard.eventId = currCard.eventId
            createEditBusinessCard.eventUserId = currCard.eventUserId

            front = TextFieldValue(currCard.front)
            back = TextFieldValue(currCard.back)
            favorite = currCard.favorite
            currCard.fields.forEach { field ->
                val fieldCpy = Field(
                    field.name,
                    field.value,
                    field.type
                )
                fields.add(fieldCpy)
            }
            template = currCard.template
            cardType = currCard.cardType
            eventId = currCard.eventId
            eventUserId = currCard.eventUserId
        }
        else {
            createEditBusinessCard.id = ""
            createEditBusinessCard.front = front.text
            createEditBusinessCard.back = back.text
            createEditBusinessCard.favorite = favorite
            createEditBusinessCard.fields = fields.toMutableList()
            createEditBusinessCard.template = template
            createEditBusinessCard.cardType = cardType
            createEditBusinessCard.eventId = eventId
            createEditBusinessCard.eventUserId = eventUserId
        }
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // TODO: Force add full name, company/institution name, role/title
            //  They can then add information such as mobile phone number, company phone number, email,
            //  address, website, linkedin, github
            OutlinedTextField(value = front, onValueChange = {
                front = it
                createEditBusinessCard.front = front.text
            }, label = { Text(text = "Card Front Text") },
                placeholder = {
                    Text(text = "e.g. Figure out what this and the Back text is for")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(value = back, onValueChange = {
                back = it
                createEditBusinessCard.back = back.text
            }, label = { Text(text = "Card Back Text") },
                placeholder = {
                    Text(text = "e.g. Figure out what this and the Front text is for")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // Full Name
            OutlinedTextField(value = fullName, onValueChange = {
                fullName = it
                val fullNameField = createEditBusinessCard.fields.find {
                    field -> field.name == "Full Name" }
                // If field doesn't exist yet, add it
                if (fullNameField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Full Name",
                        value = fullName.text,
                        type = FieldType.TEXT
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (fullName.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Full Name"}
                }
                // otherwise update field with new value
                else {
                   createEditViewModel.updateField("Full Name", fullName.text)
                }
            }, label = { Text(text = "Full Name") },
                placeholder = {
                    Text(text = "e.g. John Doe")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // Company
            OutlinedTextField(value = company, onValueChange = {
                company = it
                val companyField = createEditBusinessCard.fields.find {
                        field -> field.name == "Company/Institution" }
                // If field doesn't exist yet, add it
                if (companyField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Company/Institution",
                        value = company.text,
                        type = FieldType.TEXT
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (company.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Company/Institution"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Company/Institution", company.text)
                }
            }, label = { Text(text = "Company/Institution Name") },
                placeholder = {
                    Text(text = "e.g. Google")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // Role
            OutlinedTextField(value = role, onValueChange = {
                role = it
                val roleField = createEditBusinessCard.fields.find {
                        field -> field.name == "Role" }
                // If field doesn't exist yet, add it
                if (roleField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Role",
                        value = role.text,
                        type = FieldType.TEXT
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (role.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Role"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Role", role.text)
                }
            }, label = { Text(text = "Role/Title") },
                placeholder = {
                    Text(text = "e.g. Chief Executive Officer")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // TODO: add Mobile Phone
            // TODO: add Company Phone

            // Email
            // TODO: Check email error handling in FAB
            OutlinedTextField(value = email, onValueChange = {
                email = it
                val emailField = createEditBusinessCard.fields.find {
                        field -> field.name == "Email" }
                // If field doesn't exist yet, add it
                if (emailField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Email",
                        value = email.text,
                        type = FieldType.EMAIL
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (email.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Email"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Email", email.text)
                }
            }, label = { Text(text = "Email Address") },
                placeholder = {
                    Text(text = "e.g. john.doe@gmail.com")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // Address
            OutlinedTextField(value = address, onValueChange = {
                address = it
                val addressField = createEditBusinessCard.fields.find {
                        field -> field.name == "Address" }
                // If field doesn't exist yet, add it
                if (addressField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Address",
                        value = address.text,
                        type = FieldType.TEXT
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (address.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Address"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Address", address.text)
                }
            }, label = { Text(text = "Address") },
                placeholder = {
                    Text(text = "e.g. 1600 Amphitheatre Parkway in Mountain View, California")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // Website
            OutlinedTextField(value = website, onValueChange = {
                website = it
                val websiteField = createEditBusinessCard.fields.find {
                        field -> field.name == "Website" }
                // If field doesn't exist yet, add it
                if (websiteField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Website",
                        value = website.text,
                        type = FieldType.URL
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (website.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Website"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Website", website.text)
                }
            }, label = { Text(text = "Website") },
                placeholder = {
                    Text(text = "e.g. www.google.com")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // LinkedIn
            OutlinedTextField(value = linkedin, onValueChange = {
                linkedin = it
                val linkedinField = createEditBusinessCard.fields.find {
                        field -> field.name == "LinkedIn ID" }
                // If field doesn't exist yet, add it
                if (linkedinField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "LinkedIn ID",
                        value = linkedin.text,
                        type = FieldType.LINKEDIN_ID
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (linkedin.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "LinkedIn ID"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("LinkedIn ID", linkedin.text)
                }
            }, label = { Text(text = "LinkedIn ID") },
                placeholder = {
                    Text(text = "e.g. www.linkedin.com/in/john-doe")
                }, modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // Github
            OutlinedTextField(value = github, onValueChange = {
                github = it
                val githubField = createEditBusinessCard.fields.find {
                        field -> field.name == "Github Username" }
                // If field doesn't exist yet, add it
                if (githubField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Github Username",
                        value = github.text,
                        type = FieldType.GITHUB_USERNAME
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (github.text == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Github Username"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Github Username", github.text)
                }
            }, label = { Text(text = "Github Username") },
                placeholder = {
                    Text(text = "e.g. John_Doe")
                }, modifier = Modifier.fillMaxSize()
            )

            //TODO: Allow lazy column to add more fields

            // TODO: Add button to import image for front and back of card
        }
    }
}
