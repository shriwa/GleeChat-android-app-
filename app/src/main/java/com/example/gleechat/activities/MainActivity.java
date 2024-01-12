/**
 * The MainActivity class represents the main screen of the chat application.
 * It displays the user's recent conversations and provides functionality for signing out,
 * starting a new chat, and opening a conversation.
 */

package com.example.gleechat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.gleechat.adapters.RecentChatsAdapter;
import com.example.gleechat.databinding.ActivityMainBinding;
import com.example.gleechat.listeners.ConversionListener;
import com.example.gleechat.models.ChatMessage;
import com.example.gleechat.models.user;
import com.example.gleechat.utilities.Accelerometer;
import com.example.gleechat.utilities.Constants;
import com.example.gleechat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversation;
    private RecentChatsAdapter conversationAdapter;
    private FirebaseFirestore database;

    private Accelerometer accelerometer;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize objects and variables
        preferenceManager = new PreferenceManager(getApplicationContext());
        accelerometer = new Accelerometer(this);

        // Initialize the activity
        init();
        getToken();
        loadUserDetails();
        setListeners();
        listenConversation();
    }

    /**
     * Initializes the necessary components of the activity.
     */
    private void init(){
        conversation = new ArrayList<>();
        conversationAdapter = new RecentChatsAdapter(conversation, this);
        binding.chatRecyclerViewer.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        accelerometer.register();
    }

    @Override
    public void onClick(View view) {
        // Handle click events
    }

    @Override
    protected void onPause() {
        super.onPause();
        accelerometer.unregister();
    }

    /**
     * Sets click listeners for UI elements.
     */
    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    /**
     * Loads the user's details and displays them on the UI.
     */
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = android.util.Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    /**
     * Displays a toast message.
     * @param message The message to be displayed.
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up listeners to listen for conversation changes in the database.
     */
    private void listenConversation(){
        // Listen for conversation changes where the user is the sender or receiver
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    /**
     * Event listener for conversation changes in the database.
     */
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    // Get details of the conversation and add it to the list
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.ConversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.ConversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.ConversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.ConversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversation.add(chatMessage);
                }else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    // Update the last message of an existing conversation
                    for (int i = 0; i < conversation.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversation.get(i).senderId.equals(senderId) && conversation.get(i).receiverId.equals(receiverId)){
                            conversation.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversation.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }

            // Sort the conversations based on the date and update the UI
            Collections.sort(conversation, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.chatRecyclerViewer.smoothScrollToPosition(0);
            binding.chatRecyclerViewer.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };
    /**
     * Retrieves the FCM token from Firebase messaging service.
     */
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    /**
     * Updates the FCM token in the database for the current user.
     * @param token The FCM token to be updated.
     */

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Token update failed"));
    }

    /**
     * Signs out the user from the application.
     * Clears preferences and navigates to the sign-in activity.
     */
    private void signOut(){
        showToast("Signing out....");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Sign out failed"));
    }

    /**
     * Handles the click event when a conversation is clicked.
     * Opens the ChatActivity for the selected conversation.
     * @param user The user associated with the conversation.
     */
    @Override
    public void onConversionClicked(user user){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }


}