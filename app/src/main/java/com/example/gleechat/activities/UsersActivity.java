
/**

 The activity that displays the list of users.
 It retrieves user data from Firebase Firestore and populates the user list.
 Allows the user to click on a user to start a chat.
 */

package com.example.gleechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.gleechat.adapters.userAdapter;
import com.example.gleechat.databinding.ActivityUsersBinding;
import com.example.gleechat.listeners.UserListener;
import com.example.gleechat.models.user;
import com.example.gleechat.utilities.Constants;
import com.example.gleechat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * Sets click listeners for Back button.
     */
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Retrieves the list of users from Firebase Firestore.
     * Populates the user list and displays it in the RecyclerView.
     */
    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<user> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            user user = new user();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0){
                            userAdapter userAdapters = new userAdapter(users, this);
                            binding.usersRecycleView.setAdapter(userAdapters);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });

    }

    /**
     * Displays an error message when no users are available.
     */
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Handles the loading state of the activity.
     * Shows or hides the progress bar based on the provided loading state.
     * @param isLoading True if the activity is in a loading state, False otherwise.
     */
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    /**

     Called when a user is clicked in the user list.
     Starts the ChatActivity to initiate a chat with the selected user.
     @param user The user object representing the selected user.
     */
    @Override
    public void onUserClicked(user user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}