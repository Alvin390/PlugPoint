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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.PlugPoint.plugpoint.utilis.FirestoreCollections
import com.PlugPoint.plugpoint.utilis.ImageCompressUtils
import com.PlugPoint.plugpoint.ui.state.UiStateManager

class AuthViewModel(
    private val imgurViewModel: ImgurViewModel,
    @SuppressLint("StaticFieldLeak")
    private val context: Context
) : ViewModel(){
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _supplierDetails = MutableStateFlow<UserSupplier?>(null)
    val supplierDetails: StateFlow<UserSupplier?> = _supplierDetails

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

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
            firebaseAuth.signOut() // Add this line to sign out from Firebase
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

            val email = formData["email"] ?: ""
            val password = formData["password"] ?: ""

            UiStateManager.setLoading()
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener
                    val updatedFormData = formData.toMutableMap()
                    // Remove password fields before saving to Firestore
                    updatedFormData.remove("password")
                    updatedFormData.remove("confirmPassword")
                    updatedFormData["uid"] = uid

                    if (imageUri != null) {
                        viewModelScope.launch {
                            try {
                                val compressed = ImageCompressUtils.compressUri(context, imageUri)
                                val link = imgurViewModel.uploadImageAndGetUrl(Uri.fromFile(compressed), context, ImgurViewModel.AUTHORIZATION)
                                updatedFormData["imageUrl"] = link
                            } catch (e: Exception) {
                                    UiStateManager.setError(e.message ?: "Image upload failed")
                                } finally {
                                    UiStateManager.setIdle()
                                }
                            saveUserDataWithUid(userType, uid, updatedFormData, onNavigateToProfile)
                        }
                    } else {
                        saveUserDataWithUid(userType, uid, updatedFormData, onNavigateToProfile)
                    }
                }
                .addOnFailureListener { exception ->
                    _registrationState.value = RegistrationState.Failure(exception.message ?: "Registration failed.")
                }
        }
    }

    private fun saveUserDataWithUid(
        userType: String,
        uid: String,
        formData: Map<String, String>,
        onNavigateToProfile: (String) -> Unit
    ) {
        val collection = if (userType == "supplier") FirestoreCollections.SUPPLIERS else FirestoreCollections.CONSUMERS
        firestore.collection(collection).document(uid).set(formData)
            .addOnSuccessListener {
                saveLoginState(uid, userType)
                _registrationState.value = RegistrationState.Success(userType)
                val profileRoute = if (userType == "supplier") {
                    "$ROUTE_PROFILE_SUPPLIER/$uid"
                } else {
                    "$ROUTE_PROFILE_CONSUMER/$uid"
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
        onNavigateToProfile: (String) -> Unit,
        onLoginError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (email.isEmpty() || password.isEmpty()) {
                onLoginError("Email and password are required.")
                return@launch
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener
                    // Try to find user in suppliers first, then consumers
                    firestore.collection(FirestoreCollections.SUPPLIERS).document(uid).get()
                        .addOnSuccessListener { supplierDoc ->
                            if (supplierDoc.exists()) {
                                saveLoginState(uid, "supplier")
                                fetchProfileDetails(uid, "supplier")
                                onNavigateToProfile("$ROUTE_PROFILE_SUPPLIER/$uid")
                            } else {
                                firestore.collection(FirestoreCollections.CONSUMERS).document(uid).get()
                                    .addOnSuccessListener { consumerDoc ->
                                        if (consumerDoc.exists()) {
                                            saveLoginState(uid, "consumer")
                                            fetchProfileDetails(uid, "consumer")
                                            onNavigateToProfile("$ROUTE_PROFILE_CONSUMER/$uid")
                                        } else {
                                            onLoginError("User profile not found.")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        onLoginError("Error: ${e.message}")
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            onLoginError("Error: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    onLoginError("Invalid email or password.")
                }
        }
    }

    fun fetchProfileDetails(userId: String, userType: String) {
        val collection = if (userType == "supplier") FirestoreCollections.SUPPLIERS else FirestoreCollections.CONSUMERS

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
            val collection = if (userType == "supplier") FirestoreCollections.SUPPLIERS else FirestoreCollections.CONSUMERS

            UiStateManager.setLoading()
            val updatedDataWithImage = updatedData.toMutableMap()
            if (imageUri != null) {
                try {
                    val compressed = ImageCompressUtils.compressUri(context, imageUri)
                    val link = imgurViewModel.uploadImageAndGetUrl(Uri.fromFile(compressed), context, authorization = ImgurViewModel.AUTHORIZATION)
                    updatedDataWithImage["imageUrl"] = link
                } catch (e: Exception) {
                    onUpdateFailure(e.message ?: "Image upload failed")
                    return@launch
                }
            }

            firestore.collection(collection).document(userId).update(updatedDataWithImage as Map<String, Any>)
                .addOnSuccessListener { UiStateManager.setIdle()
                onUpdateSuccess() }
                .addOnFailureListener { e -> UiStateManager.setError(e.message ?: "Update failed.")
                 onUpdateFailure(e.message ?: "Update failed.") }
        }
    }

    sealed class RegistrationState {
        object Idle : RegistrationState()
        data class Success(val userType: String) : RegistrationState()
        data class Failure(val errorMessage: String) : RegistrationState()
    }
}