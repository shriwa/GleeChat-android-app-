/**
 BaseActivity is an abstract class that serves as a base for other activities in the Gleechat app.
 It extends AppCompatActivity to provide basic activity functionality.
 It also interacts with Firebase Firestore to update the user's availability status.
 The class contains methods for handling activity lifecycle events and an abstract onClick method.
 */

package com.example.gleechat.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gleechat.utilities.Constants;
import com.example.gleechat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize PreferenceManager to manage user preferences
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        // Get an instance of Firebase Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // Get the user's document reference in the Firestore collection
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Update the user's availability status to 0 (unavailable) when the activity is paused
        documentReference.update(Constants.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the user's availability status to 1 (available) when the activity is resumed
        documentReference.update(Constants.KEY_AVAILABILITY,1);
    }

    /**
     * Abstract method to be implemented by subclasses to handle click events.
     */
    public abstract void onClick(View view);
}
