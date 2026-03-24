package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.repository.BookmarkRepository
import com.google.firebase.auth.FirebaseAuth
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
        onError: (String) -> Unit
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
                        onError("User ID not found")
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Login failed")
                }
            }
    }

    fun signup(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "Signup failed")
                }
            }
    }
}