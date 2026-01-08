package com.example.skillflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillflow.ui.theme.SkillFlowTheme
import com.google.firebase.auth.FirebaseAuth

class LoginPageActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            SkillFlowTheme {
                LoginScreen(auth)
            }
        }
    }
}

private const val GDPR_PREFS = "gdpr_prefs"
private const val GDPR_ACCEPTED = "gdpr_accepted"

private fun isGdprAccepted(context: Context): Boolean {
    return context.getSharedPreferences(GDPR_PREFS, Context.MODE_PRIVATE)
        .getBoolean(GDPR_ACCEPTED, false)
}

private fun setGdprAccepted(context: Context, accepted: Boolean) {
    context.getSharedPreferences(GDPR_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(GDPR_ACCEPTED, accepted)
        .apply()
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // GDPR
    var gdprAccepted by remember { mutableStateOf(isGdprAccepted(context)) }
    var showGdprDialog by remember { mutableStateOf(!gdprAccepted) }

    if (showGdprDialog) {
        AlertDialog(
            onDismissRequest = {}, // force a choice
            title = { Text("GDPR Consent") },
            text = {
                Text(
                    "SkillFlow uses your email to sign you in and store your learning progress. " +
                            "We do not sell your data. By accepting, you agree to data processing for app features."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        setGdprAccepted(context, true)
                        gdprAccepted = true
                        showGdprDialog = false
                    }
                ) { Text("Accept") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        setGdprAccepted(context, false)
                        gdprAccepted = false
                        Toast.makeText(
                            context,
                            "You must accept GDPR to use login.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                ) { Text("Decline") }
            }
        )
    }

    // Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4C6EF5), Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "SkillFlow",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Login to continue learning",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    val icon =
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (!gdprAccepted) {
                        showGdprDialog = true
                        return@Button
                    }
                    if (isLoading) return@Button

                    when {
                        email.isBlank() || password.isBlank() -> {
                            Toast.makeText(
                                context,
                                "Please enter email and password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                        context.startActivity(Intent(context, DashboardActivity::class.java))
                                    } else {
                                        Toast.makeText(
                                            context,
                                            task.exception?.message ?: "Login failed",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Login", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "Don't have an account? Sign Up",
                color = Color.White,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, SignupPageActivity::class.java))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "GDPR: You can review consent by reinstalling the app.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}
