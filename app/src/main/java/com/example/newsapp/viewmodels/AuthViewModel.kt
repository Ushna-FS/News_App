package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.R
import com.example.newsapp.data.repository.BookmarkRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        viewModelScope.launch {

                            // 1️⃣ Fetch bookmarks from Firebase to Room
                            bookmarkRepository.fetchBookmarksFromFirebase(userId)
                            bookmarkRepository.startRealtimeSync(userId)
                            // 2️⃣ Call success callback (NewsViewModel will start listener in UI)
                            onSuccess()
                        }
                    } else {
                        onError(R.string.user_id_not_found)
                    }
                } else {

                    val message = when (task.exception) {

                        is FirebaseAuthInvalidUserException ->
                            (R.string.account_does_not_exist)

                        is FirebaseAuthInvalidCredentialsException ->
                            (R.string.incorrect_email_or_password)

                        else ->
                            (R.string.login_failed_please_try_again)
                    }

                    onError(message)
                }
            }
    }

    fun signup(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        val db = FirebaseFirestore.getInstance()

                        val userMap = hashMapOf(
                            "username" to username,
                            "email" to email
                        )

                        db.collection("users")
                            .document(userId)
                            .set(userMap)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener {
                                onError(R.string.failed_to_save_user)
                            }

                    } else {
                        onError(R.string.user_id_not_found)
                    }

                } else {
                    onError(R.string.signup_failed)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    fun getCurrentUsername(onResult: (String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(null)
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onResult(document.getString("username"))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}