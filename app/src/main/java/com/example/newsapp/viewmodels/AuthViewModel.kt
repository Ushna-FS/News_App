package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.R
import com.example.shared.data.repository.BookmarkRepository
import com.example.shared.utils.NetworkMonitor
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.launch

class AuthViewModel(
    private val bookmarkRepository: BookmarkRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val auth = Firebase.auth

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ) {

        if (!networkMonitor.isConnected()) {
            onError(R.string.no_internet_connection)
            return
        }

        viewModelScope.launch {

            try {

                auth.signInWithEmailAndPassword(email, password)

                val userId = auth.currentUser?.uid

                if (userId != null) {

                    // 1️⃣ Fetch bookmarks from Firebase to Room
                    bookmarkRepository.fetchBookmarksFromFirebase(userId)

                    // 2️⃣ Start realtime sync
                    bookmarkRepository.startRealtimeSync(userId)

                    onSuccess()

                } else {
                    onError(R.string.user_id_not_found)
                }

            } catch (e: Exception) {

                val message = when (e) {

                    is FirebaseAuthException -> {
                        val errorMsg = e.message?.lowercase() ?: ""

                        when {
                            "user-not-found" in errorMsg ->
                                R.string.account_does_not_exist

                            "wrong-password" in errorMsg ||
                                    "invalid-credential" in errorMsg ->
                                R.string.incorrect_email_or_password

                            else ->
                                R.string.login_failed_please_try_again
                        }
                    }

                    else -> R.string.login_failed_please_try_again
                }

                onError(message)
            }

//            catch (e: Exception) {
//
//                val message = when (e) {
//
//                    is FirebaseAuthException -> {
//                        when (e.code) {
//
//                            "ERROR_USER_NOT_FOUND" ->
//                                R.string.account_does_not_exist
//
//                            "ERROR_INVALID_CREDENTIAL" ->
//                                R.string.incorrect_email_or_password
//
//                            else ->
//                                R.string.login_failed_please_try_again
//                        }
//                    }
//
//                    else -> R.string.login_failed_please_try_again
//                }
//
//                onError(message)
//            }
        }
    }

    fun signup(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ) {

        if (!networkMonitor.isConnected()) {
            onError(R.string.no_internet_connection)
            return
        }

        viewModelScope.launch {

            try {

                auth.createUserWithEmailAndPassword(email, password)

                val userId = auth.currentUser?.uid

                if (userId != null) {

                    val userMap = mapOf(
                        "username" to username,
                        "email" to email
                    )

                    Firebase.firestore
                        .collection("users")
                        .document(userId)
                        .set(userMap)

                    onSuccess()

                } else {
                    onError(R.string.user_id_not_found)
                }

            } catch (_: Exception) {
                onError(R.string.signup_failed)
            }
        }
    }

//    fun logout() {
//        auth.signOut()
//    }

    fun logout() {
        viewModelScope.launch {
            bookmarkRepository.stopRealtimeSync()
            auth.signOut()
        }
    }
    fun currentUser() = auth.currentUser

    fun getCurrentUsername(onResult: (String?) -> Unit) {

        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(null)
            return
        }

        viewModelScope.launch {

            try {

                val document = Firebase.firestore
                    .collection("users")
                    .document(userId)
                    .get()

                val username = document.data<Map<String, String>>()["username"]

                onResult(username)

            } catch (_: Exception) {
                onResult(null)
            }
        }
    }
}

//package com.example.newsapp.viewmodels
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.shared.data.repository.BookmarkRepository
//import dev.gitlive.firebase.Firebase
//import dev.gitlive.firebase.auth.auth
//import kotlinx.coroutines.launch
//import com.example.newsapp.R
//import com.example.shared.utils.NetworkMonitor
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
//import com.google.firebase.auth.FirebaseAuthInvalidUserException
//
//class AuthViewModel(
//    private val bookmarkRepository: BookmarkRepository,
//    val networkMonitor: NetworkMonitor
//) : ViewModel() {
//
//    private val auth = Firebase.auth
//
//
//    fun login(
//        email: String,
//        password: String,
//        onSuccess: () -> Unit,
//        onError: (Int) -> Unit
//    ) {
//        if (!networkMonitor.isConnected()) {
//            onError(R.string.no_internet_connection)
//            return
//        }
//        viewModelScope.launch {
//            try {
//                //  GitLive suspend call
//                val result = auth.signInWithEmailAndPassword(email, password)
//
//                val userId = result.user?.uid
//
//                if (userId != null) {
//                    // 1️⃣ Fetch bookmarks from Firebase to Room
//                    bookmarkRepository.fetchBookmarksFromFirebase(userId)
//                    // 2️⃣ Start realtime sync
//                    bookmarkRepository.startRealtimeSync(userId)
//                    // 3️⃣ Success callback (same as before)
//                    onSuccess()
//                } else {
//
//                    val message = when (task.exception) {
//
//                        is FirebaseAuthInvalidUserException ->
//                            (R.string.account_does_not_exist)
//
//                        is FirebaseAuthInvalidCredentialsException ->
//                            (R.string.incorrect_email_or_password)
//
//                        else ->
//                            (R.string.login_failed_please_try_again)
//                    }
//
//                    onError(message)
//                }
//            }
//            catch (e: Exception) {
//                onError(e.message ?: "Login failed")
//            }
//            }
//    }
//
//    fun signup(
//        email: String,
//        password: String,
//        username: String,
//        onSuccess: () -> Unit,
//        onError: (Int) -> Unit
//    ) {
//
//        if (!networkMonitor.isConnected()) {
//            onError(R.string.no_internet_connection)
//            return
//        }
//        viewModelScope.launch {
//            try {
//                //  GitLive suspend call
//                auth.createUserWithEmailAndPassword(email, password)
//
//                onSuccess()
//
//            }
//
//
//                    } else {
//                        onError(R.string.user_id_not_found)
//                    }
//
//                } else {
//                    onError(R.string.signup_failed)
//                }
//    catch (e: Exception) {
//        onError(e.message ?: "Signup failed")
//    }
//            }
//
//    }
//}