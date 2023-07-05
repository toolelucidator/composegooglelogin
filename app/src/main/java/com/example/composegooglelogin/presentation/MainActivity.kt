/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.composegooglelogin.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.composegooglelogin.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.SignInButton
import com.example.composegooglelogin.presentation.theme.ComposegoogleloginTheme

class MainActivity : ComponentActivity() {
    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var googleSignInAccount by remember {
                mutableStateOf(GoogleSignIn.getLastSignedInAccount(this))
            }
            val signInRequestLauncher = rememberLauncherForActivityResult(
                contract = GoogleSigninContract(googleSignInClient)
            )
            {//TODO
                googleSignInAccount = it
                if (googleSignInAccount != null) {
                    Log.d("MainActivity", "onCreate: ${googleSignInAccount?.displayName}")
                    Toast.makeText(
                        this,
                        "Signed in as  ${googleSignInAccount?.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            val coroutineScope = rememberCoroutineScope()
            GoogleSignInScreen(
                googleSignInAccount = googleSignInAccount,
                onSignInClicked = { signInRequestLauncher.launch(Unit) },
                onSignOutClicked = {
                    try {
                        googleSignInClient.signOut()
                        googleSignInAccount = null
                        Toast.makeText(
                            this@MainActivity, "Signed Out",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (apiException: ApiException) {
                        Log.w(
                            "GoogleSignInActivity",
                            "Sign Out Failed: $apiException"
                        )
                    }
                }
            )
            //WearApp()
        }
    }
}

@Composable
fun GoogleSignInScreen(
    googleSignInAccount: GoogleSignInAccount?,
    onSignInClicked: () -> Unit,
    onSignOutClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        if (googleSignInAccount == null) {
            AndroidView(::SignInButton) { signInButton ->
                signInButton.setOnClickListener {
                    onSignInClicked()
                }
            }
        } else {
            Chip(
                onClick = onSignOutClicked,
                label = { Text("Logout") }
            )
        }
    }
}


private class GoogleSigninContract(
    private val googleSignInClient: GoogleSignInClient,
) : ActivityResultContract<Unit, GoogleSignInAccount?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        googleSignInClient.signInIntent

    override fun parseResult(resultCode: Int, intent: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        check(task.isComplete)
        return if (task.isSuccessful) {
            task.result
        } else {
            val exception = task.exception
            check(exception is ApiException)
            Log.w(
                "GoogleSigninContract",
                "signInResult:failed code=${exception.statusCode}, " +
                        "message = $ {GoogleSignInStatusCodes.getStatusCodeString(exception.statusCode)}"
            )
            null

        }
    }

}


