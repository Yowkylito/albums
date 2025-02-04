package com.bployaltyapp.compose.screen.onboarding

import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bployaltyapp.compose.R
import com.bployaltyapp.compose.component.AuthScreenDescription
import com.bployaltyapp.compose.component.AuthTitleText
import com.bployaltyapp.compose.component.CheckBox
import com.bployaltyapp.compose.component.Label
import com.bployaltyapp.compose.component.SetPasswordTextField
import com.bployaltyapp.compose.model.register.RegisterIntent
import com.bployaltyapp.compose.model.register.RegisterState
import com.bployaltyapp.compose.navigation.RegistrationRoute
import com.bployaltyapp.compose.navigation.SettingUpRoute
import com.bployaltyapp.compose.utils.StatusBarConfig
import com.bployaltyapp.compose.utils.toTitleCase
import com.bployaltyapp.compose.values.AuthErrorMessageStyle
import com.bployaltyapp.compose.values.ButtonEnabledFontStyle
import com.bployaltyapp.compose.values.PasswordValidationStyle
import com.bployaltyapp.compose.values.TextFieldHintStyle
import com.bployaltyapp.compose.values.action_400
import com.bployaltyapp.compose.values.action_50
import com.bployaltyapp.compose.values.bingo_plus_action_200
import com.bployaltyapp.compose.values.neutral_0
import com.bployaltyapp.compose.values.transparent
import com.bployaltyapp.compose.viewmodel.RegistrationViewModel
import com.ldd.core.ui.components.LddVerticalSpacer
import com.ldd.core.ui.components.lddbutton.LddButton
import com.ldd.core.ui.components.lddtext.LddText

private fun String.toTitleCases(): String {
    return split(" ").joinToString(" ") { it.toTitleCase() }
}

@Composable
fun SetPasswordScreen(
    navController: NavController,
    isDarkMode: Boolean = false,
    state: State<RegisterState>,
    event: (RegisterIntent) -> Unit,
) {
    StatusBarConfig(isLightModeIcons = isDarkMode)
    event(RegisterIntent.GetPhoneNumber(state.value.mobileNumber))
    val context = LocalContext.current
    val isGestureNavigation =
        remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val navigationMode =
                    Settings.Secure.getInt(
                        context.contentResolver,
                        "navigation_mode",
                        0,
                    )
                navigationMode == 2
            } else {
                false
            }
        }
    val buttonState =
        when {
            state.value.validationState.isFirstNameValid &&
                state.value.validationState.isLastNameValid &&
                state.value.passwordState.isPasswordValid &&
                (state.value.passwordState.passwordError == null) &&
                (state.value.passwordState.confirmPassword.isNotEmpty()) &&
                (state.value.passwordState.password == state.value.passwordState.confirmPassword) -> {
                true
            }
            else -> {
                false
            }
        }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(if (isDarkMode) transparent else Color.White),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 16.dp),
        ) {
            AuthTitleText(
                text = stringResource(R.string.set_password),
                fontSize = 32.sp,
            )

            LddVerticalSpacer(12.dp)

            AuthScreenDescription(
                text = "You're almost there! Please set up the password you will use for this account.",
            )

            LddVerticalSpacer(16.dp)

            Label(text = stringResource(R.string.first_name))

            LddVerticalSpacer(12.dp)

            Row(
                modifier =
                    Modifier
                        .shadow(
                            elevation = if (!state.value.uiState.isFirstnameFocus) 0.5.dp else 2.5.dp,
                            clip = true,
                            shape = RoundedCornerShape(8.dp),
                            ambientColor = transparent,
                            spotColor = transparent,
                        )
                        .height(55.dp)
                        .fillMaxWidth()
                        .border(
                            width = if (!state.value.uiState.isFirstnameFocus) 0.5.dp else 1.5.dp,
                            color = state.value.uiState.firstNameBorder,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .background(neutral_0),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = state.value.firstName.toTitleCases(),
                    onValueChange = {
                        if (it.all { char -> !char.isDigit() }) {
                            event(RegisterIntent.EnterFirstName(it))
                        }
                    },
                    modifier =
                        Modifier
                            .background(color = transparent)
                            .padding(start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .onFocusChanged {
                                event(RegisterIntent.TextFieldFocus("firstname", it.isFocused))
                            },
                    maxLines = 1,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (state.value.firstName.isEmpty()) {
                            Text(
                                text = "Enter First Name",
                                style = TextFieldHintStyle,
                            )
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                )
            }
            if (state.value.validationState.firstNameError != null) {
                Text(
                    text = state.value.validationState.firstNameError!!,
                    style = AuthErrorMessageStyle,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            LddVerticalSpacer(16.dp)

            Label(text = stringResource(R.string.last_name))

            LddVerticalSpacer(12.dp)

            Row(
                modifier =
                    Modifier
                        .shadow(
                            elevation = if (!state.value.uiState.isLastnameFocus) 0.5.dp else 2.5.dp,
                            clip = true,
                            shape = RoundedCornerShape(8.dp),
                            ambientColor = transparent,
                            spotColor = transparent,
                        )
                        .height(55.dp)
                        .fillMaxWidth()
                        .border(
                            width = if (!state.value.uiState.isLastnameFocus) 0.5.dp else 1.5.dp,
                            color = state.value.uiState.lastNameBorder,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .background(neutral_0),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = state.value.lastName.toTitleCases(),
                    onValueChange = {
                        if (it.all { char -> !char.isDigit() }) {
                            event(RegisterIntent.EnterLastName(it))
                        }
                    },
                    modifier =
                        Modifier
                            .background(color = transparent)
                            .padding(start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .onFocusChanged { event(RegisterIntent.TextFieldFocus("lastname", it.isFocused)) },
                    maxLines = 1,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (state.value.lastName.isEmpty()) {
                            LddText(
                                text = "Enter Last Name",
                                style = TextFieldHintStyle,
                            )
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                )
            }
            if (state.value.validationState.lastNameError != null) {
                Text(
                    text = state.value.validationState.lastNameError!!,
                    style = AuthErrorMessageStyle,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            LddVerticalSpacer(12.dp)

            Label(text = stringResource(R.string.new_password))


                SetPasswordTextField(
                    accountPassword = state.value.passwordState.password,
                    onPasswordChange = { newPassword ->
                        event(RegisterIntent.EnterPassword(newPassword))
                    },
                    placeHolderText = stringResource(R.string.enter_new_password),
                    state = state,
                    borderState = state.value.passwordState.passwordBorder,
                )


            LddVerticalSpacer(12.dp)

            Label(text = stringResource(stringlib.R.string.confirm_password_title))

            SetPasswordTextField(
                accountPassword = state.value.passwordState.confirmPassword,
                onPasswordChange = { confirmPassword -> event(RegisterIntent.EnterConfirmPassword(confirmPassword)) },
                placeHolderText = stringResource(stringlib.R.string.retype_password),
                state = state,
                borderState = state.value.passwordState.passwordBorder,
            )

            PasswordMessageSection(state = state, event = event)
        }

        LddButton(
            onClick = { navController.navigate(SettingUpRoute) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (!isGestureNavigation)50.dp else 20.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(12.dp),
            enabled = buttonState,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = action_400,
                    contentColor = bingo_plus_action_200,
                    disabledContainerColor = action_50,
                    disabledContentColor = bingo_plus_action_200,
                ),
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = stringResource(R.string.create_password).uppercase(),
                style = ButtonEnabledFontStyle,
            )
        }
    }
    BackHandler { navController.navigate(RegistrationRoute) }
}

@Composable
fun PasswordMessageSection(
    state: State<RegisterState>,
    event: (RegisterIntent) -> Unit,
) {
    LddVerticalSpacer(16.dp)

    Column {
        LddText(
            text = "Password must contain:",
            style = PasswordValidationStyle,
        )

        LddVerticalSpacer(12.dp)
        val setMinimumCharCheckedState = remember { mutableStateOf(false) }

        PasswordValidationMessage(
            text = "Minimum of 6 characters",
            isChecked = state.value.passwordState.isPassSixCharChecked,
            onCheckedChange = { setMinimumCharCheckedState.value = it },
        )
        LddVerticalSpacer(12.dp)

        val setUpperCaseChecked = remember { mutableStateOf(false) }
        PasswordValidationMessage(
            text = "At least one upper case letter",
            isChecked = state.value.passwordState.isPassUpperCaseChecked,
            onCheckedChange = { setUpperCaseChecked.value = it },
        )
        val setLowerCaseChecked = remember { mutableStateOf(false) }

        LddVerticalSpacer(12.dp)
        PasswordValidationMessage(
            text = "At least one lower case letter",
            isChecked = state.value.passwordState.isPassLowerCaseChecked,
            onCheckedChange = { setLowerCaseChecked.value = it },
        )

        LddVerticalSpacer(12.dp)

        val setOneNumberChecked = remember { mutableStateOf(false) }
        PasswordValidationMessage(
            text = "At least one number",
            isChecked = state.value.passwordState.isPassNumberChecked,
            onCheckedChange = { setOneNumberChecked.value = it },
        )

        LddVerticalSpacer(12.dp)

        val setOneSpecialCharChecked = remember { mutableStateOf(false) }
        PasswordValidationMessage(
            text = "At least one special character (!@\$#%*)",
            isChecked = state.value.passwordState.isPassSpecialChecked,
            onCheckedChange = { isChecked ->
                setOneSpecialCharChecked.value = isChecked
            },
        )
    }
}

@Composable
fun PasswordValidationMessage(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isClickable: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckBox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            borderDp = 2.dp,
            isClickable = isClickable,
        )
        LddText(
            modifier = Modifier.padding(start = 6.dp),
            text = text,
            style = PasswordValidationStyle,
        )
    }
}

@Preview
@Composable
private fun SetPasswordScreenPreview() {
    val registrationViewModel: RegistrationViewModel = hiltViewModel()
    val state = registrationViewModel.registerState.collectAsState()
    SetPasswordScreen(rememberNavController(), state = state, event = {})
}
