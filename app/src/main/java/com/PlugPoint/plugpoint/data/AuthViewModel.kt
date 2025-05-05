package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.text.get

class AuthViewModel : ViewModel() {
    private val _supplierDetails = MutableStateFlow<UserSupplier?>(null)
    val supplierDetails: StateFlow<UserSupplier?> = _supplierDetails

    private val _consumerDetails = MutableStateFlow<UserConsumer?>(null)
    val consumerDetails: StateFlow<UserConsumer?> = _consumerDetails


    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun registerUser(
        userType: String, // "supplier" or "consumer"
        formData: Map<String, String>, // Key-value pairs of form data
        onNavigateToProfile: (String) -> Unit // Callback for navigation
    ) {
        viewModelScope.launch {
            // Validate form data
            val validationError = validateFormData(formData)
            if (validationError != null) {
                _registrationState.value = RegistrationState.Failure(validationError)
                return@launch
            }

            val database = FirebaseDatabase.getInstance().reference
            val node = if (userType == "supplier") "suppliers" else "consumers"

            // Push data to Firebase
            database.child(node).push().setValue(formData)
                .addOnSuccessListener {
                    _registrationState.value = RegistrationState.Success(userType)
                    val profileRoute = if (userType == "supplier") {
                        "profile_supplier"
                    } else {
                        "profile_consumer"
                    }
                    onNavigateToProfile(profileRoute) // Trigger navigation
                }
                .addOnFailureListener { exception ->
                    val errorMessage = exception.message ?: "An unknown error occurred."
                    _registrationState.value = RegistrationState.Failure(errorMessage)
                }
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

            val database = FirebaseDatabase.getInstance().reference

            // Check for supplier
            database.child("suppliers").get().addOnSuccessListener { snapshot ->
                val user = snapshot.children.find {
                    it.child("email").value == email && it.child("password").value == password
                }
                if (user != null) {
                    val userId = user.key ?: ""
                    onNavigateToProfile("$ROUTE_PROFILE_SUPPLIER/$userId")
                    return@addOnSuccessListener
                }

                // Check for consumer
                database.child("consumers").get().addOnSuccessListener { consumerSnapshot ->
                    val consumer = consumerSnapshot.children.find {
                        it.child("email").value == email && it.child("password").value == password
                    }
                    if (consumer != null) {
                        val consumerId = consumer.key ?: ""
                        onNavigateToProfile("$ROUTE_PROFILE_CONSUMER/$consumerId")// Navigate to consumer profile
                    } else {
                        onLoginError("Invalid email or password.")
                    }
                }.addOnFailureListener {
                    onLoginError("Failed to fetch consumer data: ${it.message}")
                }
            }.addOnFailureListener {
                onLoginError("Failed to fetch supplier data: ${it.message}")
            }
        }
    }

    fun fetchProfileDetails(userId: String, userType: String) {
        val database = FirebaseDatabase.getInstance().reference
        val node = if (userType == "supplier") "suppliers" else "consumers"

        database.child(node).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (userType == "supplier") {
                    val supplier = snapshot.getValue(UserSupplier::class.java)
                    _supplierDetails.value = supplier
                } else {
                    val consumer = snapshot.getValue(UserConsumer::class.java)
                    _consumerDetails.value = consumer
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (userType == "supplier") {
                    _supplierDetails.value = null
                } else {
                    _consumerDetails.value = null
                }
            }
        })
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