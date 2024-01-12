package com.example.gleechat.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.gleechat.R;
import com.example.gleechat.adapters.ChatAdapter;
import com.example.gleechat.adapters.userAdapter;
import com.example.gleechat.databinding.ActivityChatBinding;
import com.example.gleechat.models.ChatMessage;
import com.example.gleechat.models.user;
import com.example.gleechat.network.ApiClient;
import com.example.gleechat.network.ApiService;
import com.example.gleechat.utilities.Constants;
import com.example.gleechat.utilities.MapsActivity;
import com.example.gleechat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements View.OnClickListener{

    private ActivityChatBinding binding;
    private user receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    private String encodedImage;

    // Floating Action Button variables for the FAB menu
    FloatingActionButton layoutShare, location, document, video, image, contact;
    Float translationY = 100f;
    boolean isMenuOpen = false;
    OvershootInterpolator interpolator = new OvershootInterpolator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
        initFabMenu();
    }


    private void init(){
        // Initialize the preference manager
        preferenceManager = new PreferenceManager(getApplicationContext());
        // Initialize the chatMessages list and chatAdapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        // Set the chatAdapter to the chatRecyclerViewer
        binding.chatRecyclerViewer.setAdapter(chatAdapter);
        // Initialize the FirebaseFirestore database
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        // Send a chat message
        // Create a HashMap to store the message details
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_IMAGE,encodedImage);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable){
            try {
                JSONArray tokens  = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception){
                showToast((exception.getMessage()));
            }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Sending a push notification using the Firebase Cloud Messaging (FCM) service.
    // It makes an API call to a server to send the notification
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()){
                        try {
                            if (response.body() != null){
                                JSONObject responseJson = new JSONObject(response.body());
                                JSONArray results = responseJson.getJSONArray("results");
                                if (responseJson.getInt("failure") == 1){
                                    JSONObject error = (JSONObject) results.get(0);
                                    showToast(error.getString("error"));
                                    return;
                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                        showToast(("Notification sent successfully"));
                        }else{
                            showToast("Error : " + response.code());
                        }
                    }


            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    // listens for the availability of the receiver user by adding a snapshot listener to the Firestore document of the receiver
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this,(value, error) -> {
            if (error != null) {
            return;
        }
        if (value != null) {
            if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                int availability = Objects.requireNonNull(
                        value.getLong(Constants.KEY_AVAILABILITY)
                ).intValue();
                isReceiverAvailable = availability == 1;
            }
            receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
            if (receiverUser.image == null){
                receiverUser.image = value.getString(Constants.KEY_IMAGE);
                chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
            }
        }
        if (isReceiverAvailable){
            binding.textAvailability.setVisibility(View.VISIBLE);
        }else {
            binding.textAvailability.setVisibility(View.GONE);
        }
        });
    }

    //  listens for incoming chat messages by adding snapshot listeners to the Firestore collection for sent and received messages
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    // Updates the chatMessages list and notifies the adapter.
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dataTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            chatMessages.sort(Comparator.comparing(obj -> obj.dateObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerViewer.smoothScrollToPosition(chatMessages.size() -1);
            }
            binding.chatRecyclerViewer.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null){
            checkForConversion();
        }
    };

    // Converting a Base64 encoded image string to a Bitmap object.
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if (encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else {
            return null;
        }
    }
    // Loads the details of the receiver user, such as name, from the intent
    private void loadReceiverDetails(){
        receiverUser = (user) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    // Sets click listeners for various views in the activity.
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }


    // Converting a Date object to a readable date-time format.
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    // Adding a new conversation to the Firestore collection of conversations.
    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    //  Updating an existing conversation with the last message and timestamp.
    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    // Checking if a conversation already exists between the sender and receiver users.
    private void checkForConversion(){
        if (chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    // Queries the Firestore collection of conversations to find a conversation between the sender and receiver users.
    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionCompleteListener);
    }

    // Called when the query for conversations is complete. It sets the conversionId if a conversation exists.
    private final OnCompleteListener<QuerySnapshot> conversionCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    // listening for the availability of the receiver user when the activity resumes.
    protected void onResume(){
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    // initialize the Floating Action Button (FAB) menu.
    private void initFabMenu(){
        layoutShare = findViewById(R.id.layoutShare);
        location = findViewById(R.id.location);
        document = findViewById(R.id.document);
        video = findViewById(R.id.video);
        image = findViewById(R.id.image);
        contact = findViewById(R.id.contact);

        location.setAlpha(0f);
        document.setAlpha(0f);
        video.setAlpha(0f);
        image.setAlpha(0f);
        contact.setAlpha(0f);

        location.setTranslationY(translationY);
        document.setTranslationY(translationY);
        video.setTranslationY(translationY);
        image.setTranslationY(translationY);
        contact.setTranslationY(translationY);

        layoutShare.setOnClickListener(this);
        location.setOnClickListener(this);
        document.setOnClickListener(this);
        video.setOnClickListener(this);
        image.setOnClickListener(this);
        contact.setOnClickListener(this);
    }

    // Animating the FAB menu to open.
    private void openMenu(){
        isMenuOpen = !isMenuOpen;

        layoutShare.animate().setInterpolator(interpolator).rotationBy(0f).setDuration(300).start();

        location.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        document.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        video.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        image.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        contact.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    // Animating the FAB menu to close.
    private void closeMenu(){
        isMenuOpen = !isMenuOpen;

        layoutShare.animate().setInterpolator(interpolator).rotationBy(0f).setDuration(300).start();

        location.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        document.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        video.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        image.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        contact.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    // Actions for opening and closing the FAB menu.
    // selecting location, document, video, image, and contact options.
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.layoutShare:
                Log.i(TAG, "onClick: Layout Share");
                if (isMenuOpen){
                    closeMenu();
                }else {
                    openMenu();
                }
                break;
            case R.id.location:
                Intent intent = new Intent(ChatActivity.this, MapsActivity.class);
            startActivity(intent);
                break;
            case R.id.document:
                Log.i(TAG, "onClick: document ");
                break;
            case R.id.video:
                binding.video.setOnClickListener(v -> {
                    Intent video = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    video.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pickImage.launch(video);
                });
                break;
            case R.id.image:
                binding.image.setOnClickListener(v -> {
                    Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    image.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pickImage.launch(image);
                });
                break;
            case R.id.contact:
                Log.i(TAG, "onClick: contact");
                break;

        }
    }

    // Takes a Bitmap object, resizes it, and converts it to a Base64 encoded string.
    private String encodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = 150;
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->  {
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            //binding.imageProfile.setImageBitmap(bitmap);
                            encodedImage = encodedImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );


}

