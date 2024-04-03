package cs446.dbc.views


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import cs446.dbc.components.BusinessCardMultiSelect
import cs446.dbc.components.CreateDialog
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
    val userId by appViewModel.userId.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageUriFront by remember { mutableStateOf<Uri?>(null) }
    var imageUriBack by remember { mutableStateOf<Uri?>(null) }
    var reloadRequest by remember { mutableStateOf(0) }

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
    var hasFullName by remember {
        mutableStateOf(false)
    }
    var company by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var hasCompany by remember {
        mutableStateOf(false)
    }
    var role by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var mobilePhone by remember {
        mutableStateOf("")
    }
    var companyPhone by remember {
        mutableStateOf("")
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

            front = TextFieldValue(currCard.front)
            back = TextFieldValue(currCard.back)
            favorite = currCard.favorite
            currCard.fields.forEach { field ->
                when (field.name) {
                    "Full Name" -> {
                        fullName = TextFieldValue(field.value)
                        hasFullName = true
                    }
                    "Company/Institution" -> {
                        company = TextFieldValue(field.value)
                        hasCompany = true
                    }
                    "Role" -> role = TextFieldValue(field.value)
                    "Mobile Phone" -> mobilePhone = field.value
                    "Company Phone" -> companyPhone = field.value
                    "Email" -> email = TextFieldValue(field.value)
                    "Address" -> address = TextFieldValue(field.value)
                    "Website" -> website = TextFieldValue(field.value)
                    "LinkedIn ID" -> linkedin = TextFieldValue(field.value)
                    "Github Username" -> github = TextFieldValue(field.value)
                }
            }
            template = currCard.template
            cardType = currCard.cardType
        }
        else {
            createEditBusinessCard.id = ""
            createEditBusinessCard.front = front.text
            createEditBusinessCard.back = back.text
            createEditBusinessCard.favorite = favorite
            createEditBusinessCard.fields = fields.toMutableList()
            createEditBusinessCard.template = template
            createEditBusinessCard.cardType = cardType
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
            // Full Name
            OutlinedTextField(value = fullName, onValueChange = {
                fullName = it
                hasFullName = if (fullName.text == "") false else true
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
                }, modifier = Modifier.fillMaxSize(),
                isError = !hasFullName
            )

            AnimatedVisibility (!hasFullName) {
                Text(text = "Please enter your full name", color = Color.Red)
            }

            Spacer(modifier = Modifier.padding(4.dp))

            // Company
            OutlinedTextField(value = company, onValueChange = {
                company = it
                hasCompany = if (company.text == "") false else true
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
                }, modifier = Modifier.fillMaxSize(),
                isError = !hasCompany
            )

            AnimatedVisibility (!hasCompany) {
                Text(text = "Please enter your company name", color = Color.Red)
            }

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

            // Mobile Phone
            // TODO: Check if mobile phone has 10 digits
            OutlinedTextField(value = mobilePhone, onValueChange = {
                if (it.length <= 10) {
                    mobilePhone = it.takeWhile { it.isDigit() }
                }

                val mobilePhoneField = createEditBusinessCard.fields.find {
                        field -> field.name == "Mobile Phone" }
                // If field doesn't exist yet, add it
                if (mobilePhoneField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Mobile Phone",
                        value = mobilePhone,
                        type = FieldType.PHONE_NUMBER
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (mobilePhone == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Mobile Phone"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Mobile Phone", mobilePhone)
                }
            }, label = { Text(text = "Mobile Phone") },
                placeholder = {
                    Text(text = "e.g. +1 (123) 456-7890")
                }, modifier = Modifier.fillMaxSize(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = remember { phoneNumberTransformation }
            )

            Spacer(modifier = Modifier.padding(4.dp))

            // Company Phone
            // TODO: Check if company phone has 10 digits
            OutlinedTextField(value = companyPhone, onValueChange = {
                if (it.length <= 10) {
                    companyPhone = it.takeWhile { it.isDigit() }
                }

                val companyPhoneField = createEditBusinessCard.fields.find {
                        field -> field.name == "Company Phone" }
                // If field doesn't exist yet, add it
                if (companyPhoneField == null) {
                    createEditBusinessCard.fields.add(Field(
                        name = "Company Phone",
                        value = companyPhone,
                        type = FieldType.PHONE_NUMBER
                    ))
                }
                // if it does, but user removed text from textfield, remove field from list
                else if (companyPhone == "") {
                    createEditBusinessCard.fields.removeIf { field -> field.name == "Company Phone"}
                }
                // otherwise update field with new value
                else {
                    createEditViewModel.updateField("Company Phone", companyPhone)
                }
            }, label = { Text(text = "Company Phone") },
                placeholder = {
                    Text(text = "e.g. +1 (123) 456-7890")
                }, modifier = Modifier.fillMaxSize(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = remember { phoneNumberTransformation }
            )

            Spacer(modifier = Modifier.padding(4.dp))

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

            Spacer(modifier = Modifier.padding(4.dp))

            // Front background upload
            val directory = context.getExternalFilesDir(null)
            val galleryLauncherFront = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    coroutineScope.launch(Dispatchers.IO) {
                        saveImageToStorage(context, it, userId, cardId, isFront = true) { savedFile ->
                            // Update the state to display the image in the front
                            imageUriFront = Uri.fromFile(savedFile)
                        }
                        reloadRequest++
                    }
                }
            }

            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(onClick = { galleryLauncherFront.launch("image/*") }) {
                    Text("Choose a front background for your card")
                }
                // Below is how you display an image
                if (cardId != "") imageUriFront =
                    Uri.fromFile(File(directory, "user_${userId}_card_${cardId}_image_front.jpg"))
                imageUriFront?.let { uri ->
                    Spacer(modifier = Modifier.padding(4.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uri)
                            .build(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    front = TextFieldValue(uri.toString())
                }

                Spacer(modifier = Modifier.padding(4.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .setParameter("reload", reloadRequest)
                        .build(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                front = TextFieldValue(uri.toString())
            }

            Spacer(modifier = Modifier.padding(4.dp))

            // Back background upload
            val galleryLauncherBack = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    coroutineScope.launch(Dispatchers.IO) {
                        saveImageToStorage(context, it, userId, cardId, isFront = false) { savedFile ->
                            // Update the state to display the image in the back
                            imageUriBack = Uri.fromFile(savedFile)

                // Front background upload
                val galleryLauncherBack =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                        uri?.let {
                            coroutineScope.launch(Dispatchers.IO) {
                                saveImageToStorage(
                                    context,
                                    it,
                                    userId,
                                    cardId,
                                    isFront = false
                                ) { savedFile ->
                                    // Update the state to display the image in the back
                                    imageUriBack = Uri.fromFile(savedFile)
                                }
                            }
                        }
                        reloadRequest++
                    }


                OutlinedButton(onClick = { galleryLauncherBack.launch("image/*") }) {
                    Text("Choose a back background for your card")
                }
                // Below is how you display an image
                if (cardId != "") imageUriBack =
                    Uri.fromFile(File(directory, "user_${userId}_card_${cardId}_image_back.jpg"))
                imageUriBack?.let { uri ->
                    Spacer(modifier = Modifier.padding(4.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uri)
                            .build(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    back = TextFieldValue(uri.toString())
                }
            }
        }
    }
}

// Phone number transformation
private val phoneNumberTransformation = VisualTransformation { text ->
    val trimmed = if (text.text.length > 10) text.take(10) else text
    var result = "+1 "
    for (i in trimmed.indices) {
        if (i == 0) result += "("
        result += trimmed[i]
        result += when (i) {
            2 -> ") "
            5 -> "-"
            else -> ""
        }
    }

    val mapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset == 0) return 3
            if (offset <= 2) return offset + 4
            if (offset <= 5) return offset + 6
            return offset + 7

        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 3) return 0
            if (offset <= 7) return offset -4
            if (offset <= 12) return offset -6
            return offset - 7
        }
    }
    TransformedText(AnnotatedString(result), mapping)
}

private fun saveImageToStorage(context: Context, imageUri: Uri, userId: String, cardId: String, isFront: Boolean, onSaved: (File) -> Unit) {
    // This function saves images to local storage for later use
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    val directory = context.getExternalFilesDir(null) ?: return
    // Use the following file name convention: user_$userId_card_$cardId_image_$cardSide
    // If in add card phase, the following file name convention is used: user_$userId__image_$cardSide
    val side = if (isFront) "front" else "back"
    val file = File(directory, "user_${userId}_${if (cardId != "") "card_${cardId}" else ""}_image_${side}.jpg")
    inputStream?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
        onSaved(file)
    }
}
