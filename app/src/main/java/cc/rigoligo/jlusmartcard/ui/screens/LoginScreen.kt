package cc.rigoligo.jlusmartcard.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.rigoligo.jlusmartcard.R
import cc.rigoligo.jlusmartcard.ui.activities.LocalNavController
import cc.rigoligo.jlusmartcard.viewmodel.LoginScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen() {
    val scaffoldState = rememberScaffoldState()
    val model by remember { mutableStateOf(LoginScreenViewModel(scaffoldState)) }
    val uiState by model.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    val navController = LocalNavController.current

    LaunchedEffect(Unit) {
        // Toast request from ViewModel
        coroutineScope.launch {
            uiState.loginSuccessToast.collect { message ->
                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
                delay(1000)
                navController.navigateUp()
            }
        }

        // Snackbar request from ViewModel
        coroutineScope.launch {
            uiState.loginSnackbarInfo.collect { message ->
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message
                    )
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.login_login_smartcard)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cdes_login_back)
                        )
                    }
                })
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.login_login_smartcard),
                style = MaterialTheme.typography.h5
            )
            Divider()
            TextField(
                value = uiState.currentCardNumber,
                onValueChange = { str -> model.updateCardNumber(str) },
                label = { Text(stringResource(id = R.string.login_card_number)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // https://stackoverflow.com/a/66998457
            TextField(value = uiState.currentPassword,
                onValueChange = { str -> model.updatePassword(str) },
                label = { Text(stringResource(id = R.string.login_password)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description =
                        if (passwordVisible) stringResource(id = R.string.cdes_login_hide_password)
                        else stringResource(id = R.string.cdes_login_show_password)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                })
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = uiState.currentVerificationCode,
                    onValueChange = { str -> model.updateVerificationCode(str) },
                    label = { Text(stringResource(id = R.string.login_verification_code)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                AnimatedContent(
                    targetState = uiState.verificationCodeState
                ) { verificationCodeState ->
                    when (verificationCodeState) {
                        LoginScreenViewModel.VerificationCodeState.LoadFailed -> IconButton(modifier = Modifier
                            .height(
                                TextFieldDefaults.MinHeight
                            )
                            .background(
                                MaterialTheme.colors.primary, MaterialTheme.shapes.small
                            )
                            .aspectRatio(1.0f, true), onClick = {
                            model.refreshVerificationCodeImage()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(id = R.string.cdes_reload_verification_code_img),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        LoginScreenViewModel.VerificationCodeState.Loading -> CircularProgressIndicator()
                        LoginScreenViewModel.VerificationCodeState.Loaded -> Box(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Image(
                                modifier = Modifier
                                    .height(TextFieldDefaults.MinHeight - 16.dp) // TODO: Find a better way for constaining the height
                                    .clickable {
                                        model.refreshVerificationCodeImage()
                                    },
                                bitmap = uiState.verificationCodePicture!!.asImageBitmap(),
                                contentDescription = stringResource(id = R.string.cdes_login_verification_code_img),
                                contentScale = ContentScale.FillHeight,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Button(onClick = {
                model.performLogin()
            }) {
                Text(stringResource(id = R.string.login_login))
            }
        }
    }
}

@Preview
@Composable
fun PreviewLoginScreen() {
    LoginScreen()
}