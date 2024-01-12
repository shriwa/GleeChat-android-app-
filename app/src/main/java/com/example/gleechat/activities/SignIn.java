/**

 The SignIn class is responsible for handling the sign-in functionality of the application.
 It allows users to sign in using their email and password.
 */


package com.example.gleechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gleechat.databinding.ActivitySignInBinding;
import com.example.gleechat.utilities.Constants;
import com.example.gleechat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignIn extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an instance of the PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        // Check if the user is already signed in
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            // If the user is signed in, redirect to the MainActivity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Inflate the layout using the ActivitySignInBinding
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    /**
     * Sets click listeners for the "Create New Account" text and the "Sign In" button.
     */
    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUp.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()){
                signIn();
            }
        });
    }

    /**
     * Performs the sign-in process.
     * Retrieves user input from the email and password fields,
     * and validates the sign-in details by checking them against the Firestore database.
     */
    private void signIn() {
        loading(true);

        // Get an instance of the FirebaseFirestore database
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Query the collection of users to find a match for the provided email and password
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){

                        // If a match is found, retrieve the first document (user) from the result
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        // Save the sign-in status and user information in the PreferenceManager
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        // Create an intent to open the MainActivity and clear the back stack
                        Intent intent =  new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        // If no match is found, display a toast message and stop loading
                        loading(false);
                        showToast("Sign in failed");
                    }
                });
    }

    /**
     * Shows or hides the loading progress bar and sign-in button.
     * @param isLoading Indicates whether the loading progress bar should be shown or hidden.
     */
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Displays a toast message with the provided message text.
     * @param message The message to be displayed in the toast.
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Validates the sign-in details entered by the user.
     * Checks if the email and password fields are not empty and if the email is in a valid format.
     * @return True if the sign-in details are valid, false otherwise.
     */
    private boolean isValidSignInDetails(){
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }else {
            return true;
        }
    }


}