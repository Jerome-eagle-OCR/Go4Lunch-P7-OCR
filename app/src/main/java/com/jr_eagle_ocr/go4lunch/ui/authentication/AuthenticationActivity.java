package com.jr_eagle_ocr.go4lunch.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityAuthenticationBinding;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.MainActivity;

import java.util.Arrays;
import java.util.List;

/**
 * @author jrigault
 */
public class AuthenticationActivity extends AppCompatActivity {
    // See: https://developer.android.com/training/basics/intents/result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    private ActivityAuthenticationBinding binding;
    private final UserRepository userRepository = Go4LunchApplication.getDependencyContainer().getUserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initAuthentication();
    }


    private void initAuthentication() {
        FirebaseUser firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        if (firebaseUser == null) {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build(),
                    new AuthUI.IdpConfig.TwitterBuilder().build(),
                    new AuthUI.IdpConfig.EmailBuilder().build());
//                    new AuthUI.IdpConfig.AnonymousBuilder().build());

            // Create custom layout
            AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                    .Builder(R.layout.activity_authentication)
                    .setGoogleButtonId(R.id.fui_google)
                    .setFacebookButtonId(R.id.fui_facebook)
                    .setTwitterButtonId(R.id.fui_twitter)
                    .setEmailButtonId(R.id.fui_email)
//                    .setAnonymousButtonId(R.id.fui_anonymous)
                    .build();

            // Create and launch sign-in intent
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.Theme_Go4Lunch_FirebaseUI)
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(true, true)
                    .setAuthMethodPickerLayout(customLayout)
                    .setLockOrientation(true)
                    .build();
            signInLauncher.launch(signInIntent);
        } else {
            goToMainActivity();
        }
    }

    private void createUserAndGoToMainActivity() {
        userRepository.createUser().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                toastThis(getString(R.string.connection_succeed));
                goToMainActivity();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = MainActivity.navigate(this, false);
        startActivity(intent);
        finish();
    }

    // Show Snack Bar with a message
    private void toastThis(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            // ...
            createUserAndGoToMainActivity();
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            if (response == null) {
                toastThis(getString(R.string.error_authentication_canceled));
            } else if (response.getError() != null) {
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    toastThis(getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    toastThis(getString(R.string.error_unknown_error));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}