package com.PlugPoint.plugpoint.data

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AuthViewModel(private val imgurViewModel: ImgurViewModel,
                    @SuppressLint("StaticFieldLeak") private val context: Context) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _supplierDetails = MutableStateFlow<UserSupplier?>(null)
    val supplierDetails: StateFlow<UserSupplier?> = _supplierDetails

    private val _consumerDetails = MutableStateFlow<UserConsumer?>(null)
    val consumerDetails: StateFlow<UserConsumer?> = _consumerDetails

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState


        private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("PlugPointPrefs", Context.MODE_PRIVATE)

    fun saveLoginState(userId: String, userType: String) {
        sharedPreferences.edit()
            .putBoolean("isLoggedIn", true)
            .putString("userId", userId)
            .putString("userType", userType)
            .apply()
    }

        @SuppressLint("UseKtx")
        fun clearLoginState() {
            sharedPreferences.edit {
                remove("isLoggedIn")
                    .remove("userId")
            }
        }

        fun isUserLoggedIn(): Boolean {
            return sharedPreferences.getBoolean("isLoggedIn", false)
        }

        fun getLoggedInUserId(): String? {
            return sharedPreferences.getString("userId", null)
        }


    fun logoutUser(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            // Clear any session-related data if needed
            _supplierDetails.value = null
            _consumerDetails.value = null

            // Navigate to the login screen
            onNavigateToLogin()
        }
    }


    fun registerUser(
        userType: String, // "supplier" or "consumer"
        formData: Map<String, String>, // Key-value pairs of form data
        imageUri: Uri?, // Image URI
        onNavigateToProfile: (String) -> Unit,// Callback for navigation
        context: android.content.Context
    ) {
        viewModelScope.launch {
            val validationError = validateFormData(formData)
            if (validationError != null) {
                _registrationState.value = RegistrationState.Failure(validationError)
                return@launch
            }

            if (imageUri != null) {
                imgurViewModel.uploadImage(imageUri, context, authorization = "511479d0432ec58")
                val uploadState = imgurViewModel.uploadState.first()
                if (uploadState is ImgurUploadState.Success) {
                    val updatedFormData = formData.toMutableMap()
                    updatedFormData["imageUrl"] = uploadState.imageUrl

                    saveUserData(userType, updatedFormData, onNavigateToProfile)
                } else if (uploadState is ImgurUploadState.Error) {
                    _registrationState.value = RegistrationState.Failure(uploadState.message)
                }
            } else {
                saveUserData(userType, formData, onNavigateToProfile)
            }
        }
    }

    private fun saveUserData(
        userType: String,
        formData: Map<String, String>,
        onNavigateToProfile: (String) -> Unit
    ) {
        val collection = if (userType == "supplier") "suppliers" else "consumers"

        firestore.collection(collection).add(formData)
            .addOnSuccessListener { documentReference ->
                _registrationState.value = RegistrationState.Success(userType)
                val profileRoute = if (userType == "supplier") {
                    "$ROUTE_PROFILE_SUPPLIER/${documentReference.id}"
                } else {
                    "$ROUTE_PROFILE_CONSUMER/${documentReference.id}"
                }
                onNavigateToProfile(profileRoute)
            }
            .addOnFailureListener { exception ->
                _registrationState.value = RegistrationState.Failure(exception.message ?: "An unknown error occurred.")
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onNavigateToProfile: (String) -> Unit, // Callback for navigation
        onLoginError: (String) -> Unit // Callback for error handling
    ) {
        viewModelScope.launch {
            if (email.isEmpty() || password.isEmpty()) {
                onLoginError("Email and password are required.")
                return@launch
            }

            val supplierQuery = firestore.collection("suppliers")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)

            supplierQuery.get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val userId = snapshot.documents.first().id
                        saveLoginState(userId, "supplier")
                        onNavigateToProfile("$ROUTE_PROFILE_SUPPLIER/$userId")
                        return@addOnSuccessListener
                    }

                    val consumerQuery = firestore.collection("consumers")
                        .whereEqualTo("email", email)
                        .whereEqualTo("password", password)

                    consumerQuery.get()
                        .addOnSuccessListener { consumerSnapshot ->
                            if (!consumerSnapshot.isEmpty) {
                                val consumerId = consumerSnapshot.documents.first().id
                                saveLoginState(consumerId, "consumer")
                                onNavigateToProfile("$ROUTE_PROFILE_CONSUMER/$consumerId")
                            } else {
                                onLoginError("Invalid email or password.")
                            }
                        }
                        .addOnFailureListener { e ->
                            onLoginError("Error: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    onLoginError("Error: ${e.message}")
                }
        }
    }

    fun fetchProfileDetails(userId: String, userType: String) {
        val collection = if (userType == "supplier") "suppliers" else "consumers"

        firestore.collection(collection).document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    if (userType == "supplier") {
                        val supplier = document.toObject(UserSupplier::class.java)?.copy(
                            imageUrl = document.getString("imageUrl") ?: ""
                        )
                        _supplierDetails.value = supplier
                    } else {
                        val consumer = document.toObject(UserConsumer::class.java)?.copy(
                            imageUrl = document.getString("imageUrl") ?: ""
                        )
                        _consumerDetails.value = consumer
                    }
                }
            }
            .addOnFailureListener {
                println("Error fetching profile details: ${it.message}")
            }

    }

    private fun validateFormData(formData: Map<String, String>): String? {
        if (formData["firstName"].isNullOrEmpty()) return "First name is required."
        if (formData["lastName"].isNullOrEmpty()) return "Last name is required."
        if (formData["email"].isNullOrEmpty()) return "Email is required."
        if (formData["password"].isNullOrEmpty()) return "Password is required."
        if (formData["password"] != formData["confirmPassword"]) return "Passwords do not match."
        return null
    }

    sealed class RegistrationState {
        object Idle : RegistrationState()
        data class Success(val userType: String) : RegistrationState()
        data class Failure(val errorMessage: String) : RegistrationState()
    }
}