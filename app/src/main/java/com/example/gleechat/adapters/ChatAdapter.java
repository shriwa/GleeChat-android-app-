/**
 * Adapter class for displaying chat messages in a RecyclerView.
 */

package com.example.gleechat.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gleechat.databinding.ItemContainerRecievedMessageBinding;
import com.example.gleechat.databinding.ItemContainerSentMessageBinding;
import com.example.gleechat.models.ChatMessage;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages; // List of chat messages
    private Bitmap receiverProfileImage; // Profile image of the message receiver
    private final String senderId; // ID of the message sender


    public static final int VIEW_TYPE_SENT = 1; // View type constant for sent messages
    public static final int VIEW_TYPE_RECEIVED = 2; // View type constant for received messages


    /**
     * Sets the receiver's profile image.
     *
     * @param bitmap The receiver's profile image
     */
    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    /**
     * Constructor for the ChatAdapter class.
     *
     * @param chatMessages         List of chat messages
     * @param receiverProfileImage Profile image of the message receiver
     * @param senderId             ID of the message sender
     */

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @return A new ViewHolder that holds a View of the given view type
     */

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            // Inflate and return SentMessageViewHolder
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            // Inflate and return ReceivedMessageViewHolder
            return new ReceivedMessageViewHolder(
                    ItemContainerRecievedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     */

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_SENT){
                // Bind data to SentMessageViewHolder
                ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
            }else {
                // Bind data to ReceivedMessageViewHolder
                ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
            }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items
     */

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public int getItemViewType(int position){
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }


    /**
     * ViewHolder class for displaying sent chat messages in the RecyclerView.
     */

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        /**
         * Constructor for the SentMessageViewHolder.
         *
         * @param itemContainerSentMessageBinding The binding object for the SentMessageViewHolder
         */

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        /**
         * Sets the data of the SentMessageViewHolder.
         *
         * @param chatMessage The chat message data to be displayed
         */
        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dataTime);
        }
    }

    /**
     * ViewHolder class for displaying received chat messages in the RecyclerView.
     */
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerRecievedMessageBinding binding;

        /**
         * Constructor for the ReceivedMessageViewHolder.
         *
         * @param itemContainerRecievedMessageBinding The binding object for the ReceivedMessageViewHolder
         */
        ReceivedMessageViewHolder(ItemContainerRecievedMessageBinding itemContainerRecievedMessageBinding){
            super(itemContainerRecievedMessageBinding.getRoot());
            binding = itemContainerRecievedMessageBinding;
        }

        /**
         * Sets the data of the ReceivedMessageViewHolder.
         *
         * @param chatMessage           The chat message data to be displayed
         * @param receiverProfileImage  The profile image of the message receiver
         */

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dataTime);
            if (receiverProfileImage != null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
