package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.repository.BookmarkRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

class AuthViewModel(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val auth = Firebase.auth

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                //  GitLive suspend call
                val result = auth.signInWithEmailAndPassword(email, password)

                val userId = result.user?.uid

                if (userId != null) {
                    // 1️⃣ Fetch bookmarks from Firebase to Room
                    bookmarkRepository.fetchBookmarksFromFirebase(userId)
                    // 2️⃣ Start realtime sync
                    bookmarkRepository.startRealtimeSync(userId)
                    // 3️⃣ Success callback (same as before)
                    onSuccess()
                } else {
                    onError("User ID not found")
                }

            } catch (e: Exception) {
                onError(e.message ?: "Login failed")
            }
        }
    }

    fun signup(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                //  GitLive suspend call
                auth.createUserWithEmailAndPassword(email, password)

                onSuccess()

            } catch (e: Exception) {
                onError(e.message ?: "Signup failed")
            }
        }
    }
}