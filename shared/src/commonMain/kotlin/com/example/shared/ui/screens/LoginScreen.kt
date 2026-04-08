package com.example.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shared.navigation.Routes
import com.example.shared.viewmodels.AuthViewModel
import com.example.shared.viewmodels.NewsViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import me.sample.library.resources.Res
import me.sample.library.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(navController: NavController) {

    val viewModel: AuthViewModel = koinViewModel()
    val newsViewModel: NewsViewModel = koinViewModel()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(Res.string.welcome_back),
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = stringResource(Res.string.login_to_continue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text(stringResource(Res.string.email)) },
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let { Text(it) }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = { Text(stringResource(Res.string.password)) },
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = {
                        passwordError?.let { Text(it) }
                    },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        emailError = null
                        passwordError = null

                        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                        when {
                            email.isBlank() -> {
                                emailError = "Email cannot be empty"
                                return@Button
                            }

                            !emailRegex.matches(email) -> {
                                emailError = "Invalid email format"
                                return@Button
                            }

                            password.isBlank() -> {
                                passwordError = "Password cannot be empty"
                                return@Button
                            }

                            password.length < 6 -> {
                                passwordError = "Password must be at least 6 characters"
                                return@Button
                            }
                        }
                        isLoading = true
                        viewModel.login(
                            email,
                            password,
                            onSuccess = {
                                isLoading = false
                                val userId = Firebase.auth.currentUser?.uid ?: ""
                                newsViewModel.setCurrentUser(userId)

                                navController.navigate(Routes.MainTabs) {
                                    popUpTo(Routes.Login) { inclusive = true }
                                }
                            },
                            onError = { message ->
                                isLoading = false
                                loginError = message.toString()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(Res.string.login))
                    }
                }
                loginError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    enabled = !isLoading,
                    onClick = { navController.navigate(Routes.Signup) }
                ) {
                    Text(stringResource(Res.string.no_account_sign_up))
                }
            }
        }
    }
}