package com.example.grabandgo;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class orderuser extends AppCompatActivity {

    private ImageView logoutImage;
    private ImageView BackImage;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView welcomeText;
    private TextView regNoText;
    private TextView mobileText;
    private ImageView profileImage;
    private ImageView homeIcon, userIcon;
    private Button verifyMobileButton;
    private Button previousOrdersButton, currentOrdersButton, totalIncomeButton;
    private TextView totalIncomeText;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderuser);
        totalIncomeText = findViewById(R.id.total_income_text);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("OrderUsers").child(currentUserId);

        // Real-time listener to fetch incomeEarned
        databaseReference.child("incomeEarned").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String income = String.valueOf(dataSnapshot.getValue());
                    totalIncomeText.setText("INCOME EARNED ₹ " + income);
                } else {
                    totalIncomeText.setText("₹ 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(orderuser.this, "Failed to load data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        logoutImage = findViewById(R.id.logout_image);
        BackImage = findViewById(R.id.back_image);
        welcomeText = findViewById(R.id.welcome_text);
        regNoText = findViewById(R.id.reg_no_text);
        mobileText = findViewById(R.id.mobile_text);
        profileImage = findViewById(R.id.profile_image);
        homeIcon = findViewById(R.id.home_icon);
        userIcon = findViewById(R.id.user_icon);
        verifyMobileButton = findViewById(R.id.verify_mobile_button);
        previousOrdersButton = findViewById(R.id.previous_orders_button);  // New reference
        currentOrdersButton = findViewById(R.id.current_orders_button);    // New reference
        // Handle logout when clicking on logout image
        logoutImage.setOnClickListener(v -> signOut());
        BackImage.setOnClickListener(v -> {
            Intent intent = new Intent(orderuser.this, firstpage.class);
            startActivity(intent);
        });
        previousOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(orderuser.this, previous_orders.class);
            startActivity(intent);
        });
        currentOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(orderuser.this, current_orders.class);
            startActivity(intent);
        });

        // Handle the click event for the verify mobile number button
        verifyMobileButton.setOnClickListener(v -> showMobileInputDialog());

        // Display user details
        displayUserDetails();
    }

    private void displayUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Display name and profile picture
            displayUserNameAndProfilePicture();

            // Extract and display Reg No from email
            displayRegNo(user);

            // Display mobile number
            displayMobileNumber(user);
            previousOrdersButton.setVisibility(View.VISIBLE);
            currentOrdersButton.setVisibility(View.VISIBLE);
        }
    }

    private void displayUserNameAndProfilePicture() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Get the display name
            String displayName = user.getDisplayName();
            if (displayName != null) {
                // Split the display name based on the format you've mentioned (BUSA DEEPAK 2022-IT)
                // Only consider the part before the year or last part with a hyphen
                String[] nameParts = displayName.split(" ");
                StringBuilder firstName = new StringBuilder();

                // Loop through the parts and add to firstName until we encounter the year part or last name
                for (String part : nameParts) {
                    if (part.matches(".*\\d{4}.*")) {  // If it contains a year (e.g., 2022)
                        break;
                    }
                    firstName.append(part).append(" ");
                }

                // Trim and capitalize the result
                String formattedName = capitalize(firstName.toString().trim());

                // Set the welcome message
                welcomeText.setText("NAME: " + formattedName);
            }

            // Load profile image using Glide with CircleCrop transformation
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .apply(RequestOptions.circleCropTransform()) // Apply circular crop
                        .into(profileImage);
            }
        }
    }

    // Capitalize the first letter of each word in the name


    private void displayRegNo(FirebaseUser user) {
        String email = user.getEmail();
        if (email != null && email.endsWith("@klu.ac.in")) {
            String regNo = email.split("@")[0];
            regNoText.setText("Reg No: " + regNo);
        } else {
            regNoText.setText("Reg No: Not Available");
        }
    }

    private void displayMobileNumber(FirebaseUser user) {
        String mobileNumber = user.getPhoneNumber();
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            mobileText.setText("Mobile Number: " + mobileNumber);
            verifyMobileButton.setVisibility(View.GONE);
        } else {
            mobileText.setText("Mobile Number: Not Available");
            verifyMobileButton.setVisibility(View.VISIBLE);
            verifyMobileButton.setOnClickListener(v -> showMobileInputDialog());
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task1 -> {
                Intent intent = new Intent(orderuser.this, orderlogin.class);
                startActivity(intent);
                finish();
            });
        });
    }

    public void onHomeClick(View view) {
        Intent intent = new Intent(orderuser.this, orderhome.class);
        startActivity(intent);
        finish();
    }

    private void showMobileInputDialog() {
        final Dialog mobileInputDialog = new Dialog(this);
        mobileInputDialog.setContentView(R.layout.dialog_mobile_input);
        mobileInputDialog.setCancelable(true);

        EditText mobileNumberEditText = mobileInputDialog.findViewById(R.id.mobile_number_edit_text);
        Button nextButton = mobileInputDialog.findViewById(R.id.next_button);

        nextButton.setOnClickListener(v -> {
            String mobileNumber = mobileNumberEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(mobileNumber)) {
                mobileInputDialog.dismiss(); // Dismiss dialog only after ensuring input is valid
                sendOtp(mobileNumber); // Send OTP
            } else {
                mobileNumberEditText.setError("Please enter a mobile number");
            }
        });

        mobileInputDialog.show();
        mobileInputDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }


    private void sendOtp(String mobileNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber("+91" + mobileNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                linkPhoneWithGoogleAccount(credential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                Toast.makeText(orderuser.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                showOtpInputDialog(verificationId); // Show OTP input dialog
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options); // Start verification
    }


    private void showOtpInputDialog(String verificationId) {
        final Dialog otpInputDialog = new Dialog(this);
        otpInputDialog.setContentView(R.layout.dialog_otp_input);
        otpInputDialog.setCancelable(true);

        EditText otpEditText = otpInputDialog.findViewById(R.id.otp_edit_text);
        Button verifyButton = otpInputDialog.findViewById(R.id.verify_button);

        verifyButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(otp)) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                linkPhoneWithGoogleAccount(credential);
                otpInputDialog.dismiss();
            } else {
                otpEditText.setError("Please enter OTP");
            }
        });

        otpInputDialog.show();
        otpInputDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void linkPhoneWithGoogleAccount(PhoneAuthCredential credential) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.linkWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(orderuser.this, "Phone linked with Google account", Toast.LENGTH_SHORT).show();
                    displayUserDetails();
                    previousOrdersButton.setVisibility(View.VISIBLE);
                    currentOrdersButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(orderuser.this, "Linking failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
