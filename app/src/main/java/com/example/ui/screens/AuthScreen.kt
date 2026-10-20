package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VcCard
import com.example.ui.components.VcGradientButton
import com.example.ui.components.VcTextField
import com.example.ui.theme.VcBackground
import com.example.ui.theme.VcBorderSubtle
import com.example.ui.theme.VcErrorRed
import com.example.ui.theme.VcPrimaryPurple
import com.example.ui.theme.VcSecondaryViolet
import com.example.ui.theme.VcSuccessGreen
import com.example.ui.theme.VcTextMuted
import com.example.ui.theme.VcTextPrimary
import com.example.ui.theme.VcTextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var guestDisplayName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(VcPrimaryPurple, VcSecondaryViolet)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "VC Connect Logo",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "VC Connect",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = VcTextPrimary
            )

            Text(
                text = "Your people. One connection.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = VcPrimaryPurple,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Meet anywhere, instantly.",
                fontSize = 14.sp,
                color = VcTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            VcCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            if (context is Activity) {
                                viewModel.signInWithGoogle(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "G",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VcSecondaryViolet
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                color = VcTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(VcBorderSubtle)
                        )
                        Text(
                            text = "  OR  ",
                            color = VcTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(VcBorderSubtle)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    var guestUsername by remember { mutableStateOf("") }

                    VcTextField(
                        value = guestDisplayName,
                        onValueChange = { guestDisplayName = it },
                        label = "Display Name (e.g. Shadow Gamer)",
                        leadingIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    VcTextField(
                        value = guestUsername,
                        onValueChange = { guestUsername = it },
                        label = "Unique Gamer Username (e.g. @shadow_blade)",
                        leadingIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(
                            color = VcPrimaryPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        VcGradientButton(
                            text = "Continue as Guest",
                            icon = Icons.Default.Person,
                            onClick = {
                                viewModel.signInAsGuest(guestDisplayName, guestUsername)
                            }
                        )
                    }

                    // UI Status feedback
                    when (val state = uiState) {
                        is UiState.Error -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.message,
                                color = VcErrorRed,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        is UiState.Success -> {
                            state.message?.let { msg ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = msg,
                                    color = VcSuccessGreen,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            onAuthSuccess()
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By continuing, you agree to our Terms of Service & Privacy Policy",
                fontSize = 12.sp,
                color = VcTextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
