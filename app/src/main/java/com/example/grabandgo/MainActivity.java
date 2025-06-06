package com.example.grabandgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private Button signInButton;
    private Button goback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In to show all accounts each time
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton = findViewById(R.id.btn_login);
        goback = findViewById(R.id.btnGoBack);

        signInButton.setOnClickListener(v -> signIn());
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to LoginActivity
                Intent intent = new Intent(MainActivity.this, firstpage.class); // Make sure the class name is correct
                startActivity(intent);
                finish(); // Optional: close HomeActivity so the user can't return to it
            }
        });
    }

    private void signIn() {
        // Prompt the user to choose an account every time
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String email = account.getEmail();
                    String name = account.getDisplayName();

                    // Check the email domain
                    if (email != null && !email.endsWith("@klu.ac.in")) {
                        showProceedDialog(account, name, email);
                    } else {
                        authenticateWithFirebase(account, name);
                    }
                }
            } catch (ApiException e) {
                Log.w("MainActivity", "Google sign-in failed", e);
                Toast.makeText(this, "Sign in failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProceedDialog(GoogleSignInAccount account, String name, String email) {
        new AlertDialog.Builder(this)
                .setTitle("Proceed with Login")
                .setMessage("You are logging in with a personal email: " + email + ". klu Mail is recommended. Do you want to proceed?")
                .setPositiveButton("Yes", (dialog, which) -> authenticateWithFirebase(account, name))
                .setNegativeButton("No", (dialog, which) -> signIn()) // Restart sign-in if declined
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void authenticateWithFirebase(GoogleSignInAccount account, String name) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Login successful
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.d("MainActivity", "signInWithCredential:success, User: " + user.getEmail());
                    navigateToShopListActivity(name);
                }
            } else {
                Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToShopListActivity(String name) {
        Intent intent = new Intent(MainActivity.this, ShopListActivity.class);
        intent.putExtra("username", name);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToShopListActivity(currentUser.getDisplayName()); // User is already signed in
        }
    }
}
