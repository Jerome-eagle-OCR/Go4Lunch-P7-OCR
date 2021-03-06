package com.jr_eagle_ocr.go4lunch.ui.authentication;

import static com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel.AUTHENTICATE;
import static com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel.NAVIGATE_TO_MAIN;
import static com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel.TOAST_AUTH_SUCCESS;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.databinding.ActivityAuthenticationBinding;
import com.jr_eagle_ocr.go4lunch.ui.MainActivity;
import com.jr_eagle_ocr.go4lunch.ui.ViewModelFactory;

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

    private AuthenticationViewModel viewModel;
    private ActivityAuthenticationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(AuthenticationViewModel.class);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setDoActionEventObserver();
    }

    /**
     * Set doAction event observer to perform appropriated action
     */
    private void setDoActionEventObserver() {
        viewModel.doActionEvent().observe(this, event -> {
            if (event != null) {
                String action = event.getContentIfNotHandled();
                if (action != null) {
                    switch (action) {
                        case AUTHENTICATE:
                            initAuthentication();
                            break;
                        case TOAST_AUTH_SUCCESS:
                            toastThis(getString(R.string.connection_succeed));
                            break;
                        case NAVIGATE_TO_MAIN:
                            navigateToMainActivity();
                            break;
                    }
                }
            }
        });
    }

    /**
     * Build and start authentication activity
     */
    private void initAuthentication() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create custom layout
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_authentication)
                .setGoogleButtonId(R.id.fui_google)
                .setFacebookButtonId(R.id.fui_facebook)
                .setTwitterButtonId(R.id.fui_twitter)
                .setEmailButtonId(R.id.fui_email)
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
    }

    /**
     * Start main activity
     */
    private void navigateToMainActivity() {
        Intent intent = MainActivity.navigate(this, false);
        startActivity(intent);
        finish();
    }

    /**
     * Toast a message
     *
     * @param message the message to toast
     */
    private void toastThis(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Sign in result callback
     *
     * @param result hte Firebase authentication result
     */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            // ...
            viewModel.setAuthenticationSuccessful();
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