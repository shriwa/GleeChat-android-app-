/**
 * This is the adapter class for the RecentChats RecyclerView.
 * It is responsible for binding the data to the views in the RecyclerView.
 */

package com.example.gleechat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gleechat.databinding.ItemContainerRecentChatBinding;
import com.example.gleechat.listeners.ConversionListener;
import com.example.gleechat.models.ChatMessage;
import com.example.gleechat.models.user;

import java.util.List;

public class RecentChatsAdapter extends RecyclerView.Adapter<RecentChatsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    // Constructor for the adapter class
    public RecentChatsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    // Inflates the item view layout and returns a new instance of the ViewHolder
    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentChatBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    // Binds the data to the views in the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull RecentChatsAdapter.ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    // Returns the total number of items in the data set
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder class that holds references to the views in the item view layout
    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentChatBinding binding;

        // Constructor for the ViewHolder class
        ConversionViewHolder(ItemContainerRecentChatBinding itemContainerRecentChatBinding){
            super(itemContainerRecentChatBinding.getRoot());
            binding = itemContainerRecentChatBinding;
        }

        // Sets the data to the views in the item view layout
        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.ConversionImage));
            binding.textName.setText(chatMessage.ConversionName);
            binding.textRecentMessage.setText(chatMessage.message);

            // Sets a click listener for the item view
            binding.getRoot().setOnClickListener(v -> {
                user user = new user();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.ConversionName;
                user.image = chatMessage.ConversionImage;
                conversionListener.onConversionClicked(user);
            });
        }
    }


    // Decodes the Base64 encoded image and returns a Bitmap
    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
