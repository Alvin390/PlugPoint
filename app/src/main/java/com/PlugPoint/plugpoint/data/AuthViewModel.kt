package com.PlugPoint.plugpoint.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val imgurViewModel: ImgurViewModel,
    @SuppressLint("StaticFieldLeak") private val context: Context
) : ViewModel(){
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _supplierDetails = MutableStateFlow<UserSupplier?>(null)
    val supplierDetails: StateFlow<UserSupplier?> = _supplierDetails

    private val _consumerDetails = MutableStateFlow<UserConsumer?>(null)
    val consumerDetails: StateFlow<UserConsumer?> = _consumerDetails

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PlugPointPrefs", Context.MODE_PRIVATE)

    fun saveLoginState(userId: String, userType: String) {
        sharedPreferences.edit {
            putBoolean("isLoggedIn", true)
            putString("userId", userId)
            putString("userType", userType)
        }
    }

    fun clearLoginState() {
        sharedPreferences.edit {
            remove("isLoggedIn")
            remove("userId")
            remove("userType")
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
            _supplierDetails.value = null
            _consumerDetails.value = null
            clearLoginState()
            onNavigateToLogin()
        }
    }

    fun registerUser(
        userType: String,
        formData: Map<String, String>,
        imageUri: Uri?,
        onNavigateToProfile: (String) -> Unit,
        context: Context
    ) {
        viewModelScope.launch {
            val validationError = validateFormData(formData)
            if (validationError != null) {
                _registrationState.value = RegistrationState.Failure(validationError)
                return@launch
            }

            val updatedFormData = formData.toMutableMap()
            if (imageUri != null) {
                imgurViewModel.uploadImage(imageUri, context, authorization = "511479d0432ec58")
                val uploadState = imgurViewModel.uploadState.first()
                when (uploadState) {
                    is ImgurUploadState.Success -> {
                        updatedFormData["imageUrl"] = uploadState.imageUrl
                    }
                    is ImgurUploadState.Error -> {
                        _registrationState.value = RegistrationState.Failure(uploadState.message)
                        return@launch
                    }
                    else -> {}
                }
            }

            saveUserData(userType, updatedFormData, onNavigateToProfile)
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
                val userId = documentReference.id
                saveLoginState(userId, userType)
                _registrationState.value = RegistrationState.Success(userType)
                val profileRoute = if (userType == "supplier") {
                    "$ROUTE_PROFILE_SUPPLIER/$userId"
                } else {
                    "$ROUTE_PROFILE_CONSUMER/$userId"
                }
                onNavigateToProfile(profileRoute)
            }
            .addOnFailureListener { exception ->
                _registrationState.value = RegistrationState.Failure(
                    exception.message ?: "An unknown error occurred."
                )
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onNavigateToProfile: (String) -> Unit,
        onLoginError: (String) -> Unit
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
                        fetchProfileDetails(userId, "supplier") // Fetch profile after login
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
                                fetchProfileDetails(consumerId, "consumer") // Fetch profile after login
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
                            id = userId,
                            imageUrl = document.getString("imageUrl") ?: ""
                        )
                        _supplierDetails.value = supplier
                    } else {
                        val consumer = document.toObject(UserConsumer::class.java)?.copy(
                            id = userId,
                            imageUrl = document.getString("imageUrl") ?: ""
                        )
                        _consumerDetails.value = consumer
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching profile details: ${e.message}")
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

    fun updateUserDetails(
        userId: String,
        userType: String,
        updatedData: Map<String, String>,
        imageUri: Uri?,
        onUpdateSuccess: () -> Unit,
        onUpdateFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val collection = if (userType == "supplier") "suppliers" else "consumers"

            val updatedDataWithImage = updatedData.toMutableMap()
            if (imageUri != null) {
                imgurViewModel.uploadImage(imageUri, context, authorization = "511479d0432ec58")
                val uploadState = imgurViewModel.uploadState.first()
                when (uploadState) {
                    is ImgurUploadState.Success -> {
                        updatedDataWithImage["imageUrl"] = uploadState.imageUrl
                    }
                    is ImgurUploadState.Error -> {
                        onUpdateFailure(uploadState.message)
                        return@launch
                    }
                    else -> {}
                }
            }

            firestore.collection(collection).document(userId).update(updatedDataWithImage as Map<String, Any>)
                .addOnSuccessListener { onUpdateSuccess() }
                .addOnFailureListener { e -> onUpdateFailure(e.message ?: "Update failed.") }
        }
    }

    sealed class RegistrationState {
        object Idle : RegistrationState()
        data class Success(val userType: String) : RegistrationState()
        data class Failure(val errorMessage: String) : RegistrationState()
    }
}