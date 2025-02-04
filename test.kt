package com.bployaltyapp.compose.viewmodel

import android.annotation.SuppressLint
import android.net.http.HttpException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bployaltyapp.compose.model.register.PasswordState
import com.bployaltyapp.compose.model.register.RegisterIntent
import com.bployaltyapp.compose.model.register.RegisterIntentState
import com.bployaltyapp.compose.model.register.RegisterState
import com.bployaltyapp.compose.model.register.RegistrationStatus
import com.bployaltyapp.compose.model.register.UIState
import com.bployaltyapp.compose.model.register.ValidationState
import com.bployaltyapp.compose.utils.toTitleCase
import com.bployaltyapp.compose.values.neutral_100
import com.bployaltyapp.data.repository.registration.RegistrationRepository
import com.bployaltyapp.data.source.remote.request.RegistrationRequest
import com.ldd.foundation.designsystem.secondary_red
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel
    @Inject
    constructor(
        private val registrationRepository: RegistrationRepository,
    ) : ViewModel() {
        private val _registerState = MutableStateFlow(RegisterState())
        val registerState: StateFlow<RegisterState> = _registerState

        private val _state = MutableStateFlow<RegisterIntentState>(RegisterIntentState.Idle)
        val state: StateFlow<RegisterIntentState> get() = _state

        private var currentMobileNumber: String = ""

        fun processRegisterIntent(intent: RegisterIntent) {
            when (intent) {
                is RegisterIntent.EnterMobileNumber -> numberValidation(intent.mobileNumber)
                is RegisterIntent.RegMobileNumber -> phoneNumberVerification(intent.mobileNumber)
                is RegisterIntent.AgreedToPolicy -> validatePolicy(intent.isChecked)
                RegisterIntent.ValidationSuccess -> successValidation()
                RegisterIntent.ValidationFailed -> failedValidation()
                RegisterIntent.CredentialsReset -> resetCredentials()
                is RegisterIntent.EnterOtp -> handleOtpInput(intent.otp)
                is RegisterIntent.ResendOtp -> resendOtp(intent.phoneNumber)
                is RegisterIntent.SubmitOtp -> submitOtp()
                is RegisterIntent.EnterConfirmPassword -> confirmPassword(intent.confirmPassword)
                is RegisterIntent.EnterFirstName -> updateFirstName(intent.firstName)
                is RegisterIntent.EnterLastName -> updateLastName(intent.lastName)
                is RegisterIntent.EnterPassword -> validatePassword(intent.password)
                is RegisterIntent.TextFieldFocus -> focusHandler(intent.tfName, intent.focused)
                is RegisterIntent.Register -> registration(intent.reg)
                is RegisterIntent.GetPhoneNumber -> updatePhoneNumber(intent.phoneNumber)
            }
        }

        private fun numberValidation(mobileNumber: String) {
            _registerState.update { it.copy(mobileNumber = "0$mobileNumber") }
            _registerState.value =
                when {
                    mobileNumber.isEmpty() -> _registerState.value.copy(
                        validationState = ValidationState(isMobileNumValid = false))
                    !mobileNumber.isMobileNumberValid() ->
                        _registerState.value.copy(
                            validationState = ValidationState(isMobileNumValid = false,
                            mobileNumberError = "Please enter a valid mobile number",)
                        )
                    else -> _registerState.value.copy(validationState = ValidationState(
                        isMobileNumValid = true, mobileNumberError = null))
                }
        }

        private fun validatePolicy(policy: Boolean) {
            _registerState.update { it.copy(uiState = UIState(isPolicyBoxChecked = policy)) }
        }

        private fun successValidation() {
            _registerState.update { it.copy(
                uiState = UIState(isPolicyBoxChecked = false,
                isBottomSheetShow = false),
                registrationStatus = RegistrationStatus(isRegistrationSuccess = false)) }
        }

        private fun resetCredentials() {
            _registerState.update { it.copy(uiState = UIState(isPolicyBoxChecked = false, isBottomSheetShow = false)) }
        }

        private fun failedValidation() {
            _registerState.update { it.copy(uiState = UIState(isBottomSheetShow = true)) }
        }

        private fun phoneNumberVerification(mobileNumber: String) {
            viewModelScope.launch {
                _state.value = RegisterIntentState.Loading
                try {
                    val response = registrationRepository.sendOtp(mobileNumber)
                    if (response.code == 1) {
                        _state.value = RegisterIntentState.OtpSent(response.data)
                    } else {
                        _registerState.update {
                            it.copy(
                                validationState = ValidationState(mobileNumberError = "Number Already Used",
                                isMobileNumValid = false,)
                            )
                        }

                        _state.value = RegisterIntentState.Error("Sending Failed")
                    }
                } catch (e: Exception) {
                    _state.value = RegisterIntentState.Error(e.message ?: "Unknown Error")
                }
            }
        }

        private fun handleOtpInput(otp: String) {
            currentMobileNumber = registerState.value.mobileNumber
            _registerState.update { it.copy(otp = otp) }
            _state.value = RegisterIntentState.OtpInput(otp = otp, mobileNumber = currentMobileNumber)
        }

        private fun submitOtp() {
            viewModelScope.launch {
                val currentState = _state.value
                if (currentState is RegisterIntentState.OtpInput) {
                    _state.value = RegisterIntentState.Loading
                    try {
                        val response =
                            registrationRepository.verifyOtp(
                                _registerState.value.mobileNumber,
                                _registerState.value.otp,
                            )
                        if (response.code == 1) {
                            _state.value = RegisterIntentState.OtpVerified(response.data)
                        } else {
                            _state.value = RegisterIntentState.Error("Verification Failed")
                        }
                    } catch (e: Exception) {
                        _state.value = RegisterIntentState.Error(e.message ?: "Unknown Error")
                    }
                } else {
                    _state.value = RegisterIntentState.Error("Invalid state for submitting OTP")
                }
            }
        }

        private fun resendOtp(phoneNumber: String) {
            viewModelScope.launch {
                val currentState = _state.value
                if (currentState is RegisterIntentState.OtpInput) {
                    _state.value = RegisterIntentState.Loading
                    try {
                        val response = registrationRepository.sendOtp(phoneNumber)
                        if (response.code == 1) {
                            _state.value = currentState.copy(showResendSection = true)
                        } else {
                            _state.value = RegisterIntentState.Error("Resending Failed")
                        }
                    } catch (e: Exception) {
                        _state.value = RegisterIntentState.Error(e.message ?: "Unknown Error")
                    }
                } else {
                    _state.value = RegisterIntentState.Error("Invalid state for resending OTP")
                }
            }
        }

        private fun updateFirstName(firstName: String) {
            _registerState.update { it.copy(firstName = firstName.toTitleCase()) }
            _registerState.value =
                when {
                    _registerState.value.firstName.isEmpty() ->
                        _registerState.value.copy(
                            validationState = ValidationState(isFirstNameValid = false,
                            firstNameError = "First name can't be empty."),
                            uiState = UIState(firstNameBorder = secondary_red,)
                        )
                    else ->
                        _registerState.value.copy(
                            validationState = ValidationState(isFirstNameValid = true,
                            firstNameError = null),
                            uiState = UIState(firstNameBorder = neutral_100)
                        )
                }
        }

        private fun updateLastName(lastName: String) {
            _registerState.update { it.copy(lastName = lastName.toTitleCase()) }
            _registerState.value =
                when {
                    _registerState.value.lastName.isEmpty() ->
                        _registerState.value.copy(
                            validationState = ValidationState(isLastNameValid = false,
                            lastNameError = "Last name can't be empty."),
                            uiState = UIState(lastNameBorder = secondary_red)
                        )
                    else ->
                        _registerState.value.copy(
                            validationState = ValidationState(isLastNameValid = true,
                            lastNameError = null),
                            uiState = UIState(lastNameBorder = neutral_100)
                        )
                }
        }

    private fun validatePassword(password: String) {
        _registerState.update { it.copy(passwordState = PasswordState(password = password)) }
        var isValid = true
        var errorMessage: String? = null

        if (password.isEmpty()) {
            isValid = false
            errorMessage = "Fulfill password requirements"
        } else {
            if (password.length < 6) {
                isValid = false
                errorMessage = "Fulfill password requirements"
                _registerState.update { it.copy(passwordState = PasswordState(isPassSixCharChecked = false) ) }
            } else {
                _registerState.update { it.copy(passwordState = PasswordState(isPassSixCharChecked = true)) }
            }

            if (!password.any { it.isUpperCase() }) {
                isValid = false
                errorMessage = "Fulfill password requirements"
                _registerState.update { it.copy(passwordState = PasswordState(isPassUpperCaseChecked = false)) }
            } else {
                _registerState.update { it.copy(passwordState = PasswordState(isPassUpperCaseChecked = true)) }
            }

            if (!password.any { it.isLowerCase() }) {
                isValid = false
                errorMessage = "Fulfill password requirements"
                _registerState.update { it.copy(passwordState = PasswordState(isPassLowerCaseChecked = false)) }
            } else {
                _registerState.update { it.copy(passwordState = PasswordState(isPassLowerCaseChecked = true)) }
            }

            if (!password.any { it.isDigit() }) {
                isValid = false
                errorMessage = "Fulfill password requirements"
                _registerState.update { it.copy(passwordState = PasswordState(isPassNumberChecked = false)) }
            } else {
                _registerState.update { it.copy(passwordState = PasswordState(isPassNumberChecked = true)) }
            }

            if (!password.hasSpecialCharacter()) {
                isValid = false
                errorMessage = "Fulfill password requirements"
                _registerState.update { it.copy(passwordState = PasswordState(isPassSpecialChecked = false)) }
            } else {
                _registerState.update { it.copy(passwordState = PasswordState(isPassSpecialChecked = true)) }
            }
        }

        _registerState.update {
            it.copy(
                passwordState = PasswordState(isPasswordValid = isValid,
                passwordError = if (isValid) null else errorMessage)
            )
        }
    }

        @SuppressLint("NewApi")
        private fun registration(regData: RegistrationRequest) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val response = registrationRepository.registerAccount(regData)
                    if (response.code == 1) {
                        _registerState.update { it.copy(registrationStatus = RegistrationStatus( isRegistrationSuccess = true),response = response.toString()) }
                    }
                } catch (
                    @SuppressLint("NewApi") e: HttpException,
                ) {
                    Log.e("ERROR", "HTTP Exception: ${e.message}", e)
                } catch (e: IOException) {
                    Log.e("ERROR", "Network Exception: ${e.message}", e)
                } catch (e: Exception) {
                    Log.i("ERROR", "Unexpected Exception: ${e.message} for ${_registerState.value.response}")
                }
            }
        }

        private fun focusHandler(
            textFieldName: String,
            focused: Boolean,
        ) {
            when (textFieldName.lowercase()) {
                "firstname" -> {
                    _registerState.value = _registerState.value.copy(uiState = UIState(isFirstnameFocus = focused))
                }
                "lastname" -> {
                    _registerState.value = _registerState.value.copy(uiState = UIState(isLastnameFocus = focused))
                }
            }
        }

        private fun updatePhoneNumber(phoneNumber: String) {
            _registerState.update { it.copy(mobileNumber = phoneNumber) }
        }

        private fun String.isMobileNumberValid(): Boolean = length == 10

        private fun String.hasSpecialCharacter(): Boolean = any { !it.isLetterOrDigit() }

        private fun confirmPassword(confirmPassword: String) {
            if (confirmPassword == _registerState.value.passwordState.password) {
                _registerState.update {
                    it.copy(
                        passwordState = PasswordState(
                            confirmPassword = confirmPassword,
                            passwordError = null,
                            passwordBorder = neutral_100)

                    )
                }
            } else if (!_registerState.value.passwordState.isPasswordValid) {
                _registerState.update {
                    it.copy(
                        passwordState = PasswordState(confirmPassword = confirmPassword,
                        passwordError = "Fulfill password requirements",
                        passwordBorder = secondary_red)
                    )
                }
            } else {
                _registerState.update {
                    it.copy(
                        passwordState = PasswordState(
                        confirmPassword = confirmPassword,
                        passwordError = "Password not matched",
                        passwordBorder = secondary_red)
                    )
                }
            }
        }
    }
