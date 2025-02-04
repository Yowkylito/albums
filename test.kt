package com.bployaltyapp.compose.viewmodel

import android.annotation.SuppressLint
import android.net.http.HttpException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bployaltyapp.compose.model.register.RegisterIntent
import com.bployaltyapp.compose.model.register.RegisterIntentState
import com.bployaltyapp.compose.model.register.RegisterState
import com.bployaltyapp.compose.utils.toTitleCase
import com.bployaltyapp.compose.values.neutral_100
import com.bployaltyapp.data.repository.otp.OtpRepositoryImpl.Companion.TAG
import com.bployaltyapp.data.repository.registration.RegistrationRepository
import com.bployaltyapp.data.source.remote.request.RegistrationRequest
import com.ldd.foundation.designsystem.secondary_red
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel
    @Inject
    constructor(private val registrationRepository: RegistrationRepository) : ViewModel() {
        private val _registerState = MutableStateFlow(RegisterState())
        val registerState: StateFlow<RegisterState> = _registerState
        private val _state = MutableStateFlow<RegisterIntentState>(RegisterIntentState.Idle)
        val state: StateFlow<RegisterIntentState> get() = _state
        private var currentMobileNumber: String = ""

        fun processRegisterIntent(intent: RegisterIntent) {
            when (intent) {
                is RegisterIntent.EnterMobileNumber -> {
                    numberValidation(intent.mobileNumber)
                }
                is RegisterIntent.RegMobileNumber -> phoneNumberVerification(intent.mobileNumber)
                is RegisterIntent.AgreedToPolicy -> validate(policy = intent.isChecked)
                RegisterIntent.ValidationSuccess -> {
                    successValidation()
                }
                RegisterIntent.ValidationFailed -> {
                    failedValidation()
                }
                RegisterIntent.CredentialsReset -> {
                    resetCredentials()
                }

                // OTP

                is RegisterIntent.EnterOtp -> {
                    currentMobileNumber = registerState.value.mobileNumber
                    _registerState.update { it.copy(otp = intent.otp) }
                    _state.value = RegisterIntentState.OtpInput(otp = intent.otp, mobileNumber = currentMobileNumber)
                }
                is RegisterIntent.ResendOtp -> resendOtp(intent.phoneNumber)
                is RegisterIntent.SubmitOtp -> submitOtp()

                // Set Up Password screen
                is RegisterIntent.EnterConfirmPassword -> confirmPassword(intent.confirmPassword)
                is RegisterIntent.EnterFirstName -> firstName(intent.firstName)
                is RegisterIntent.EnterLastName -> lastName(intent.lastName)
                is RegisterIntent.EnterPassword -> passwordValidation(intent.password)
                is RegisterIntent.TextFieldFocus -> focusHandler(intent.tfName, intent.focused)
                is RegisterIntent.Register -> registration(intent.reg)
                is RegisterIntent.GetPhoneNumber -> getPhoneNumber(intent.phoneNumber)
            }
        }

        private fun String.isMobileNumberValid(): Boolean {
            return length == 10
        }

        private fun numberValidation(mobileNumber: String) {
            _registerState.update { it.copy(mobileNumber = "0$mobileNumber") }
            _registerState.value =
                when {
                    mobileNumber.isEmpty() ->
                        _registerState.value.copy(
                            isMobileNumValid = false,
                        )
                    !mobileNumber.isMobileNumberValid() ->
                        _registerState.value.copy(
                            isMobileNumValid = false,
                            mobileNumberError = "Please enter a valid mobile number",
                        )
                    else ->
                        _registerState.value.copy(
                            mobileNumberError = null,
                            isMobileNumValid = true,
                        )
                }
        }

        private fun validate(policy: Boolean) {
            viewModelScope.launch {
                _registerState.update { it.copy(isPolicyBoxChecked = policy) }
            }
        }

        private fun successValidation() {
            _registerState.update { it.copy(isPolicyBoxChecked = false, isBottomSheetShow = false) }
        }

        private fun resetCredentials() {
            _registerState.update { it.copy(isPolicyBoxChecked = false, isBottomSheetShow = false) }
        }

        private fun failedValidation() {
            viewModelScope.launch {
                _registerState.update { it.copy(isBottomSheetShow = true) }
            }
        }

        private fun phoneNumberVerification(mobileNumber: String) {
            viewModelScope.launch {
                _state.value = RegisterIntentState.Loading
                try {
                    val response = registrationRepository.sendOtp(mobileNumber)
                    Log.i("Yowkey", "vm response $response")
                    if (response.code == 1) {
                        _state.value = RegisterIntentState.OtpSent(response.data)
                        Log.i("Yowkey", "OTP sent successfully.")
                    } else {
                        _state.value = RegisterIntentState.Error("Sending Failed")
                        Log.i("Yowkey", "Error: Sending OTP failed with response code: ${response.code}")

                    }
                } catch (e: Exception) {
                    _state.value = RegisterIntentState.Error(e.message ?: "Unknown Error")
                    Log.i("Yowkey", "Exception: ${e.message}")
                }
            }
        }

        private fun getPhoneNumber(phoneNumber: String) {
            _registerState.update { it.copy(mobileNumber = phoneNumber) }
        }

        // OTP

        private fun submitOtp() {
            viewModelScope.launch {
                val currentState = _state.value
                if (currentState is RegisterIntentState.OtpInput) {
                    _state.value = RegisterIntentState.Loading
                    try {
                        val response = registrationRepository.verifyOtp(_registerState.value.mobileNumber, _registerState.value.otp)
                        if (response.code == 1) {
                            _state.value = RegisterIntentState.OtpVerified(response.data)
                            Log.i(TAG, "OTP verified successfully.")
                        } else {
                            _state.value = RegisterIntentState.Error("Verification Failed")
                            Log.i(TAG, "Error: Verification failed with response code: ${response.code}")
                        }
                    } catch (e: Exception) {
                        _state.value = RegisterIntentState.Error(e.message ?: "Unknown Error")
                        Log.i(TAG, "Exception: ${e.message}")
                    }
                } else {
                    _state.value = RegisterIntentState.Error("Invalid state for submitting OTP")
                    Log.i(TAG, "Error: Invalid state for submitting OTP")
                }
            }
        }

        private fun resendOtp(phoneNumber: String) {
            viewModelScope.launch {
                val currentState = _state.value
                if (currentState is RegisterIntentState.OtpInput) {
                    _state.value = RegisterIntentState.Loading
                    try {
                        val response = registrationRepository.sendOtp(currentState.mobileNumber)
                        delay(1000) // Simulate network delay
                        if (response.code == 1) {
                            _state.value = currentState.copy(showResendSection = true)
                            Log.i(TAG, "OTP resent successfully.")
                        } else {
                            _state.value = RegisterIntentState.Error("Resending Failed")
                            Log.i(TAG, "Error: Resending OTP failed with response code: ${response.code}")
                        }
                    } catch (e: Exception) {
                        _state.value = RegisterIntentState.Error(e.message ?: "Unknown Error")
                        Log.i(TAG, "Exception: ${e.message}")
                    }
                } else {
                    _state.value = RegisterIntentState.Error("Invalid state for resending OTP")
                    Log.i(TAG, "Error: Invalid state for resending OTP")
                }
            }
        }

        private fun firstName(firstName: String) {
            _registerState.update { it.copy(firstName = firstName.toTitleCase()) }
            _registerState.value =
                when {
                    _registerState.value.firstName.isEmpty() ->
                        _registerState.value.copy(
                            isFirstNameValid = false,
                            firstNameBorder = secondary_red,
                            firstNameError = "First name can't be empty.",
                        )
                    else -> {
                        _registerState.value.copy(
                            isFirstNameValid = true,
                            firstNameBorder = neutral_100,
                            firstNameError = null,
                        )
                    }
                }
        }

        private fun lastName(lastName: String) {
            _registerState.update { it.copy(lastName = lastName.toTitleCase()) }
            _registerState.value =
                when {
                    _registerState.value.lastName.isEmpty() ->
                        _registerState.value.copy(
                            isLastNameValid = false,
                            lastNameBorder = secondary_red,
                            lastNameError = "Last name can't be empty.",
                        )
                    else -> {
                        _registerState.value.copy(
                            isLastNameValid = true,
                            lastNameBorder = neutral_100,
                            lastNameError = null,
                        )
                    }
                }
        }

        private fun passwordValidation(password: String) {
            _registerState.update { it.copy(password = password) }

            var isValid = true
            var errorMessage: String? = null

            if (password.isEmpty()) {
                isValid = false
                errorMessage = "Fulfill password requirements"
            } else {
                if (password.length < 6) {
                    isValid = false
                    errorMessage = "Fulfill password requirements"
                    _registerState.update { it.copy(isPassSixCharChecked = false) }
                } else {
                    _registerState.update { it.copy(isPassSixCharChecked = true) }
                }

                if (!password.any { it.isUpperCase() }) {
                    isValid = false
                    errorMessage = "Fulfill password requirements"
                    _registerState.update { it.copy(isPassUpperCaseChecked = false) }
                } else {
                    _registerState.update { it.copy(isPassUpperCaseChecked = true) }
                }

                if (!password.any { it.isLowerCase() }) {
                    isValid = false
                    errorMessage = "Fulfill password requirements"
                    _registerState.update { it.copy(isPassLowerCaseChecked = false) }
                } else {
                    _registerState.update { it.copy(isPassLowerCaseChecked = true) }
                }

                if (!password.any { it.isDigit() }) {
                    isValid = false
                    errorMessage = "Fulfill password requirements"
                    _registerState.update { it.copy(isPassNumberChecked = false) }
                } else {
                    _registerState.update { it.copy(isPassNumberChecked = true) }
                }

                if (!password.hasSpecialCharacter()) {
                    isValid = false
                    errorMessage = "Fulfill password requirements"
                    _registerState.update { it.copy(isPassSpecialChecked = false) }
                } else {
                    _registerState.update { it.copy(isPassSpecialChecked = true) }
                }
            }

            _registerState.update {
                it.copy(
                    isPasswordValid = isValid,
                    passwordError = if (isValid) null else errorMessage,
                    passwordBorder = if (isValid && (password == _registerState.value.confirmPassword)) neutral_100 else secondary_red,
                )
            }
        }

        private fun confirmPassword(confirmPassword: String) {
            if (confirmPassword == _registerState.value.password) {
                _registerState.update {
                    it.copy(
                        confirmPassword = confirmPassword,
                        passwordError = null,
                        passwordBorder = neutral_100,
                    )
                }
            } else if (!_registerState.value.isPasswordValid) {
                _registerState.update {
                    it.copy(
                        confirmPassword = confirmPassword,
                        passwordError = "Fulfill password requirements",
                        passwordBorder = secondary_red,
                    )
                }
            } else {
                _registerState.update {
                    it.copy(
                        confirmPassword = confirmPassword,
                        passwordError = "Password not matched",
                        passwordBorder = secondary_red,
                    )
                }
            }
        }

        @SuppressLint("NewApi")
        private fun registration(regData: RegistrationRequest) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val response = registrationRepository.registerAccount(regData)
                    Log.i("YOWKEY", "RESPONSE: $response DATA: $regData")
                } catch (
                    @SuppressLint("NewApi") e: HttpException,
                ) {
                    Log.e("YOWKEY", "HTTP Exception: ${e.message}", e)
                } catch (e: IOException) {
                    Log.e("YOWKEY", "Network Exception: ${e.message}", e)
                } catch (e: Exception) {
                    Log.e("YOWKEY", "Unexpected Exception: ${e.message}", e)
                }
            }
        }

        private fun String.hasSpecialCharacter(): Boolean {
            val specialCharacters = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/\\`~"
            return any { it in specialCharacters }
        }

        private fun focusHandler(
            textFieldName: String,
            focused: Boolean,
        ) {
            when (textFieldName.lowercase()) {
                "firstname" -> {
                    _registerState.value = _registerState.value.copy(isFirstnameFocus = focused)
                }
                "lastname" -> {
                    _registerState.value = _registerState.value.copy(isLastnameFocus = focused)
                }
            }
        }
    }
